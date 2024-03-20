package org.apache.fop.render.ps;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.svg.NativeTextPainter;
import org.apache.fop.util.HexEncoder;
import org.apache.xmlgraphics.java2d.ps.PSGraphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

public class PSTextPainter extends NativeTextPainter {
   private FontResourceCache fontResources;
   private PSGraphics2D ps;
   private PSGenerator gen;
   private TextUtil textUtil;
   private boolean flushCurrentRun;
   private PSTextRun psRun;
   private Point2D.Double relPos;
   private static final AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

   public PSTextPainter(FontInfo fontInfo) {
      super(fontInfo);
      this.fontResources = new FontResourceCache(fontInfo);
   }

   protected boolean isSupported(Graphics2D g2d) {
      return g2d instanceof PSGraphics2D;
   }

   protected void preparePainting(Graphics2D g2d) {
      this.ps = (PSGraphics2D)g2d;
      this.gen = this.ps.getPSGenerator();
      this.ps.preparePainting();
   }

   protected void saveGraphicsState() throws IOException {
      this.gen.saveGraphicsState();
   }

   protected void restoreGraphicsState() throws IOException {
      this.gen.restoreGraphicsState();
   }

   protected void setInitialTransform(AffineTransform transform) throws IOException {
      this.gen.concatMatrix(transform);
   }

   private PSFontResource getResourceForFont(Font f, String postfix) {
      String key = postfix != null ? f.getFontName() + '_' + postfix : f.getFontName();
      return this.fontResources.getFontResourceForFontKey(key);
   }

   protected void clip(Shape shape) throws IOException {
      if (shape != null) {
         this.ps.getPSGenerator().writeln("newpath");
         PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
         this.ps.processPathIterator(iter);
         this.ps.getPSGenerator().writeln("clip");
      }
   }

   protected void beginTextObject() throws IOException {
      this.gen.writeln("BT");
      this.textUtil = new TextUtil();
      this.psRun = new PSTextRun();
   }

   protected void endTextObject() throws IOException {
      this.psRun.paint(this.ps, this.textUtil, this.tpi);
      this.gen.writeln("ET");
   }

   protected void positionGlyph(Point2D prevPos, Point2D glyphPos, boolean reposition) {
      this.flushCurrentRun = false;
      if (reposition) {
         this.flushCurrentRun = true;
      }

      if (this.psRun.getRunLength() >= 128) {
         this.flushCurrentRun = true;
      }

      if (prevPos == null) {
         this.relPos = new Point2D.Double(0.0, 0.0);
      } else {
         this.relPos = new Point2D.Double(glyphPos.getX() - prevPos.getX(), glyphPos.getY() - prevPos.getY());
      }

      if (this.psRun.vertChanges == 0 && this.psRun.getHorizRunLength() > 2 && this.relPos.getY() != 0.0) {
         this.flushCurrentRun = true;
      }

   }

   protected void writeGlyph(char glyph, AffineTransform localTransform) throws IOException {
      boolean fontChanging = this.textUtil.isFontChanging(this.font, glyph);
      if (fontChanging) {
         this.flushCurrentRun = true;
      }

      if (this.flushCurrentRun) {
         this.psRun.paint(this.ps, this.textUtil, this.tpi);
         this.psRun.reset();
      }

      this.psRun.addGlyph(glyph, this.relPos);
      this.psRun.noteStartingTransformation(localTransform);
      if (fontChanging) {
         this.textUtil.setCurrentFont(this.font, glyph);
      }

   }

   private class PSTextRun {
      private AffineTransform textTransform;
      private List relativePositions;
      private StringBuffer currentGlyphs;
      private int horizChanges;
      private int vertChanges;

      private PSTextRun() {
         this.relativePositions = new LinkedList();
         this.currentGlyphs = new StringBuffer();
      }

      public void reset() {
         this.textTransform = null;
         this.currentGlyphs.setLength(0);
         this.horizChanges = 0;
         this.vertChanges = 0;
         this.relativePositions.clear();
      }

      public int getHorizRunLength() {
         return this.vertChanges == 0 && this.getRunLength() > 0 ? this.getRunLength() : 0;
      }

      public void addGlyph(char glyph, Point2D relPos) {
         this.addRelativePosition(relPos);
         this.currentGlyphs.append(glyph);
      }

      private void addRelativePosition(Point2D relPos) {
         if (this.getRunLength() > 0) {
            if (relPos.getX() != 0.0) {
               ++this.horizChanges;
            }

            if (relPos.getY() != 0.0) {
               ++this.vertChanges;
            }
         }

         this.relativePositions.add(relPos);
      }

      public void noteStartingTransformation(AffineTransform transform) {
         if (this.textTransform == null) {
            this.textTransform = new AffineTransform(transform);
         }

      }

      public int getRunLength() {
         return this.currentGlyphs.length();
      }

      private boolean isXShow() {
         return this.vertChanges == 0;
      }

      private boolean isYShow() {
         return this.horizChanges == 0;
      }

      public void paint(PSGraphics2D g2d, TextUtil textUtil, TextPaintInfo tpi) throws IOException {
         if (this.getRunLength() > 0) {
            textUtil.writeTextMatrix(this.textTransform);
            if (this.isXShow()) {
               PSTextPainter.log.debug("Horizontal text: xshow");
               this.paintXYShow(g2d, textUtil, tpi.fillPaint, true, false);
            } else if (this.isYShow()) {
               PSTextPainter.log.debug("Vertical text: yshow");
               this.paintXYShow(g2d, textUtil, tpi.fillPaint, false, true);
            } else {
               PSTextPainter.log.debug("Arbitrary text: xyshow");
               this.paintXYShow(g2d, textUtil, tpi.fillPaint, true, true);
            }

            boolean stroke = tpi.strokePaint != null && tpi.strokeStroke != null;
            if (stroke) {
               PSTextPainter.log.debug("Stroked glyph outlines");
               this.paintStrokedGlyphs(g2d, textUtil, tpi.strokePaint, tpi.strokeStroke);
            }
         }

      }

      private void paintXYShow(PSGraphics2D g2d, TextUtil textUtil, Paint paint, boolean x, boolean y) throws IOException {
         char glyph = this.currentGlyphs.charAt(0);
         textUtil.selectFont(PSTextPainter.this.font, glyph);
         textUtil.setCurrentFont(PSTextPainter.this.font, glyph);
         this.applyColor(paint);
         boolean multiByte = textUtil.isMultiByte(PSTextPainter.this.font);
         StringBuffer sb = new StringBuffer();
         sb.append((char)(multiByte ? '<' : '('));
         int idx = 0;

         for(int c = this.currentGlyphs.length(); idx < c; ++idx) {
            glyph = this.currentGlyphs.charAt(idx);
            if (multiByte) {
               sb.append(HexEncoder.encode(glyph));
            } else {
               char codepoint = (char)(glyph % 256);
               PSGenerator.escapeChar(codepoint, sb);
            }
         }

         sb.append((char)(multiByte ? '>' : ')'));
         if (x || y) {
            sb.append("\n[");
            idx = 0;

            for(Iterator var12 = this.relativePositions.iterator(); var12.hasNext(); ++idx) {
               Point2D pt = (Point2D)var12.next();
               if (idx > 0) {
                  if (x) {
                     sb.append(this.format(PSTextPainter.this.gen, pt.getX()));
                  }

                  if (y) {
                     if (x) {
                        sb.append(' ');
                     }

                     sb.append(this.format(PSTextPainter.this.gen, -pt.getY()));
                  }

                  if (idx % 8 == 0) {
                     sb.append('\n');
                  } else {
                     sb.append(' ');
                  }
               }
            }

            if (x) {
               sb.append('0');
            }

            if (y) {
               if (x) {
                  sb.append(' ');
               }

               sb.append('0');
            }

            sb.append(']');
         }

         sb.append(' ');
         if (x) {
            sb.append('x');
         }

         if (y) {
            sb.append('y');
         }

         sb.append("show");
         PSTextPainter.this.gen.writeln(sb.toString());
      }

      private void applyColor(Paint paint) throws IOException {
         if (paint != null) {
            if (paint instanceof Color) {
               Color col = (Color)paint;
               PSTextPainter.this.gen.useColor(col);
            } else {
               PSTextPainter.log.warn("Paint not supported: " + paint.toString());
            }

         }
      }

      private String format(PSGenerator gen, double coord) {
         return Math.abs(coord) < 1.0E-5 ? "0" : gen.formatDouble5(coord);
      }

      private void paintStrokedGlyphs(PSGraphics2D g2d, TextUtil textUtil, Paint strokePaint, Stroke stroke) throws IOException {
         if (!this.currentGlyphs.toString().trim().isEmpty()) {
            this.applyColor(strokePaint);
            PSGraphics2D.applyStroke(stroke, PSTextPainter.this.gen);
            Iterator iter = this.relativePositions.iterator();
            iter.next();
            Point2D pos = new Point2D.Double(0.0, 0.0);
            PSTextPainter.this.gen.writeln("0 0 M");
            int i = 0;

            for(int c = this.currentGlyphs.length(); i < c; ++i) {
               char mapped = this.currentGlyphs.charAt(i);
               if (i == 0) {
                  textUtil.selectFont(PSTextPainter.this.font, mapped);
                  textUtil.setCurrentFont(PSTextPainter.this.font, mapped);
               }

               FontMetrics metrics = PSTextPainter.this.font.getFontMetrics();
               boolean multiByte = metrics instanceof MultiByteFont || metrics instanceof LazyFont && ((LazyFont)metrics).getRealFont() instanceof MultiByteFont;
               if (multiByte) {
                  PSTextPainter.this.gen.write("<");
                  PSTextPainter.this.gen.write(HexEncoder.encode(mapped));
                  PSTextPainter.this.gen.write(">");
               } else {
                  char codepoint = (char)(mapped % 256);
                  PSTextPainter.this.gen.write("(" + codepoint + ")");
               }

               PSTextPainter.this.gen.writeln(" false charpath");
               if (iter.hasNext()) {
                  Point2D pt = (Point2D)iter.next();
                  pos.setLocation(pos.getX() + pt.getX(), pos.getY() - pt.getY());
                  PSTextPainter.this.gen.writeln(PSTextPainter.this.gen.formatDouble5(pos.getX()) + " " + PSTextPainter.this.gen.formatDouble5(pos.getY()) + " M");
               }
            }

            PSTextPainter.this.gen.writeln("stroke");
         }
      }

      // $FF: synthetic method
      PSTextRun(Object x1) {
         this();
      }
   }

   private class TextUtil {
      private Font currentFont;
      private int currentEncoding;

      private TextUtil() {
         this.currentEncoding = -1;
      }

      public boolean isMultiByte(Font f) {
         FontMetrics metrics = f.getFontMetrics();
         boolean multiByte = metrics instanceof MultiByteFont || metrics instanceof LazyFont && ((LazyFont)metrics).getRealFont() instanceof MultiByteFont;
         return multiByte;
      }

      public void writeTextMatrix(AffineTransform transform) throws IOException {
         double[] matrix = new double[6];
         transform.getMatrix(matrix);
         PSTextPainter.this.gen.writeln(PSTextPainter.this.gen.formatDouble5(matrix[0]) + " " + PSTextPainter.this.gen.formatDouble5(matrix[1]) + " " + PSTextPainter.this.gen.formatDouble5(matrix[2]) + " " + PSTextPainter.this.gen.formatDouble5(matrix[3]) + " " + PSTextPainter.this.gen.formatDouble5(matrix[4]) + " " + PSTextPainter.this.gen.formatDouble5(matrix[5]) + " Tm");
      }

      public boolean isFontChanging(Font f, char mapped) {
         if (!this.isMultiByte(f)) {
            if (f != this.getCurrentFont()) {
               return true;
            }

            if (mapped / 256 != this.getCurrentFontEncoding()) {
               return true;
            }
         }

         return false;
      }

      public void selectFont(Font f, char mapped) throws IOException {
         int encoding = mapped / 256;
         String postfix = !this.isMultiByte(f) && encoding > 0 ? Integer.toString(encoding) : null;
         PSFontResource res = PSTextPainter.this.getResourceForFont(f, postfix);
         PSTextPainter.this.gen.useFont("/" + res.getName(), (float)f.getFontSize() / 1000.0F);
         res.notifyResourceUsageOnPage(PSTextPainter.this.gen.getResourceTracker());
      }

      public Font getCurrentFont() {
         return this.currentFont;
      }

      public int getCurrentFontEncoding() {
         return this.currentEncoding;
      }

      public void setCurrentFont(Font font, int encoding) {
         this.currentFont = font;
         this.currentEncoding = encoding;
      }

      public void setCurrentFont(Font font, char mapped) {
         int encoding = mapped / 256;
         this.setCurrentFont(font, encoding);
      }

      // $FF: synthetic method
      TextUtil(Object x1) {
         this();
      }
   }
}
