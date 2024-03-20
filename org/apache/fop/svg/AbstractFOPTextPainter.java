package org.apache.fop.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import org.apache.batik.bridge.Mark;
import org.apache.batik.bridge.StrokingTextPainter;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.bridge.TextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.fonts.Font;

public abstract class AbstractFOPTextPainter implements TextPainter {
   protected Log log = LogFactory.getLog(AbstractFOPTextPainter.class);
   private final FOPTextHandler nativeTextHandler;
   private final TextPainter proxyTextPainter;

   public AbstractFOPTextPainter(FOPTextHandler nativeTextHandler, TextPainter proxyTextPainter) {
      this.nativeTextHandler = nativeTextHandler;
      this.proxyTextPainter = proxyTextPainter;
   }

   public void paint(TextNode node, Graphics2D g2d) {
      if (this.isSupportedGraphics2D(g2d)) {
         (new TextRunPainter()).paintTextRuns(node.getTextRuns(), g2d, node.getLocation());
      }

      this.proxyTextPainter.paint(node, g2d);
   }

   protected abstract boolean isSupportedGraphics2D(Graphics2D var1);

   protected String getText(AttributedCharacterIterator aci) {
      StringBuffer sb = new StringBuffer(aci.getEndIndex() - aci.getBeginIndex());

      for(char c = aci.first(); c != '\uffff'; c = aci.next()) {
         sb.append(c);
      }

      return sb.toString();
   }

   private Font getFont(AttributedCharacterIterator aci) {
      Font[] fonts = ACIUtils.findFontsForBatikACI(aci, this.nativeTextHandler.getFontInfo());
      return fonts == null ? null : fonts[0];
   }

   private float getStringWidth(String str, Font font) {
      float wordWidth = 0.0F;
      float whitespaceWidth = (float)font.getWidth(font.mapChar(' '));

      for(int i = 0; i < str.length(); ++i) {
         char c = str.charAt(i);
         float charWidth;
         if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
            charWidth = (float)font.getWidth(font.mapChar(c));
            if (charWidth <= 0.0F) {
               charWidth = whitespaceWidth;
            }
         } else {
            charWidth = whitespaceWidth;
         }

         wordWidth += charWidth;
      }

      return wordWidth / 1000.0F;
   }

   public Shape getOutline(TextNode node) {
      return this.proxyTextPainter.getOutline(node);
   }

   public Rectangle2D getBounds2D(TextNode node) {
      return this.proxyTextPainter.getBounds2D(node);
   }

   public Rectangle2D getGeometryBounds(TextNode node) {
      return this.proxyTextPainter.getGeometryBounds(node);
   }

   public Mark getMark(TextNode node, int pos, boolean all) {
      return null;
   }

   public Mark selectAt(double x, double y, TextNode node) {
      return null;
   }

   public Mark selectTo(double x, double y, Mark beginMark) {
      return null;
   }

   public Mark selectFirst(TextNode node) {
      return null;
   }

   public Mark selectLast(TextNode node) {
      return null;
   }

   public int[] getSelected(Mark start, Mark finish) {
      return null;
   }

   public Shape getHighlightShape(Mark beginMark, Mark endMark) {
      return null;
   }

   private class TextRunPainter {
      private Point2D currentLocation;

      private TextRunPainter() {
      }

      public void paintTextRuns(Iterable textRuns, Graphics2D g2d, Point2D nodeLocation) {
         this.currentLocation = new Point2D.Double(nodeLocation.getX(), nodeLocation.getY());
         Iterator var4 = textRuns.iterator();

         while(var4.hasNext()) {
            StrokingTextPainter.TextRun run = (StrokingTextPainter.TextRun)var4.next();
            this.paintTextRun(run, g2d);
         }

      }

      private void paintTextRun(StrokingTextPainter.TextRun run, Graphics2D g2d) {
         AttributedCharacterIterator aci = run.getACI();
         aci.first();
         this.updateLocationFromACI(aci, this.currentLocation);
         Font font = AbstractFOPTextPainter.this.getFont(aci);
         if (font != null) {
            AbstractFOPTextPainter.this.nativeTextHandler.setOverrideFont(font);
         }

         TextPaintInfo tpi = (TextPaintInfo)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.PAINT_INFO);
         if (tpi != null) {
            Paint foreground = tpi.fillPaint;
            if (foreground instanceof Color) {
               Color col = (Color)foreground;
               g2d.setColor(col);
            }

            g2d.setPaint(foreground);
            TextNode.Anchor anchor = (TextNode.Anchor)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.ANCHOR_TYPE);
            String txt = AbstractFOPTextPainter.this.getText(aci);
            double advance = font == null ? run.getLayout().getAdvance2D().getX() : (double)AbstractFOPTextPainter.this.getStringWidth(txt, font);
            double tx = 0.0;
            if (anchor != null) {
               switch (anchor.getType()) {
                  case 1:
                     tx = -advance / 2.0;
                     break;
                  case 2:
                     tx = -advance;
               }
            }

            Point2D outputLocation = g2d.getTransform().transform(this.currentLocation, (Point2D)null);
            double x = outputLocation.getX();
            double y = outputLocation.getY();

            try {
               AFPGraphics2D afpg2d = (AFPGraphics2D)g2d;
               int fontSize = 0;
               if (font != null) {
                  fontSize = (int)Math.round(afpg2d.convertToAbsoluteLength((double)font.getFontSize()));
               }

               if (fontSize < 6000) {
                  AbstractFOPTextPainter.this.nativeTextHandler.drawString(g2d, txt, (float)(x + tx), (float)y);
               } else {
                  double scaleX = g2d.getTransform().getScaleX();

                  for(int i = 0; i < txt.length(); ++i) {
                     double ad = (double)run.getLayout().getGlyphAdvances()[i] * scaleX;
                     AbstractFOPTextPainter.this.nativeTextHandler.drawString(g2d, txt.charAt(i) + "", (float)(x + tx + ad), (float)y);
                  }
               }
            } catch (IOException var28) {
               if (g2d instanceof AFPGraphics2D) {
                  ((AFPGraphics2D)g2d).handleIOException(var28);
               }
            } finally {
               AbstractFOPTextPainter.this.nativeTextHandler.setOverrideFont((Font)null);
            }

            this.currentLocation.setLocation(this.currentLocation.getX() + advance, this.currentLocation.getY());
         }
      }

      private void updateLocationFromACI(AttributedCharacterIterator aci, Point2D loc) {
         Float xpos = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.X);
         Float ypos = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.Y);
         Float dxpos = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.DX);
         Float dypos = (Float)aci.getAttribute(GVTAttributedCharacterIterator.TextAttribute.DY);
         if (xpos != null) {
            loc.setLocation(xpos.doubleValue(), loc.getY());
         }

         if (ypos != null) {
            loc.setLocation(loc.getX(), ypos.doubleValue());
         }

         if (dxpos != null) {
            loc.setLocation(loc.getX() + dxpos.doubleValue(), loc.getY());
         }

         if (dypos != null) {
            loc.setLocation(loc.getX(), loc.getY() + dypos.doubleValue());
         }

      }

      // $FF: synthetic method
      TextRunPainter(Object x1) {
         this();
      }
   }
}
