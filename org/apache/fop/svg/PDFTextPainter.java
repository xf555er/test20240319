package org.apache.fop.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.IOException;
import org.apache.batik.gvt.text.TextPaintInfo;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.font.FOPGVTFont;
import org.apache.fop.svg.font.FOPGVTGlyphVector;

class PDFTextPainter extends NativeTextPainter {
   private PDFGraphics2D pdf;
   private PDFTextUtil textUtil;
   private double prevVisibleGlyphWidth;
   private boolean repositionNextGlyph;
   private static int[] paZero = new int[4];

   public PDFTextPainter(FontInfo fi) {
      super(fi);
   }

   protected boolean isSupported(Graphics2D g2d) {
      return g2d instanceof PDFGraphics2D;
   }

   protected void preparePainting(Graphics2D g2d) {
      this.pdf = (PDFGraphics2D)g2d;
   }

   protected void saveGraphicsState() {
      this.pdf.saveGraphicsState();
   }

   protected void restoreGraphicsState() {
      this.pdf.restoreGraphicsState();
   }

   protected void setInitialTransform(AffineTransform transform) {
      this.createTextUtil();
      this.textUtil.concatMatrix(transform);
   }

   private void createTextUtil() {
      this.textUtil = new PDFTextUtil(this.pdf.fontInfo) {
         protected void write(String code) {
            PDFTextPainter.this.pdf.currentStream.write(code);
         }

         protected void write(StringBuffer code) {
            PDFTextPainter.this.pdf.currentStream.append(code);
         }
      };
   }

   protected void clip(Shape clip) {
      this.pdf.writeClip(clip);
   }

   protected void writeGlyphs(FOPGVTGlyphVector gv, GeneralPath debugShapes) throws IOException {
      if (gv.getGlyphPositionAdjustments() == null) {
         super.writeGlyphs(gv, debugShapes);
      } else {
         FOPGVTFont gvtFont = (FOPGVTFont)gv.getFont();
         String fk = gvtFont.getFontKey();
         Font f = gvtFont.getFont();
         Point2D initialPos = gv.getGlyphPosition(0);
         if (f.isMultiByte()) {
            int fs = f.getFontSize();
            float fsPoints = (float)fs / 1000.0F;
            double xc = 0.0;
            double yc = 0.0;
            double xoLast = 0.0;
            double yoLast = 0.0;
            this.textUtil.writeTextMatrix(new AffineTransform(1.0, 0.0, 0.0, -1.0, initialPos.getX(), initialPos.getY()));
            this.textUtil.updateTf(fk, (double)fsPoints, true, false);
            int[][] dp = gv.getGlyphPositionAdjustments();
            int i = 0;

            for(int n = gv.getNumGlyphs(); i < n; ++i) {
               int gc = gv.getGlyphCode(i);
               int[] pa = i <= dp.length && dp[i] != null ? dp[i] : paZero;
               double xo = xc + (double)pa[0];
               double yo = yc + (double)pa[1];
               double xa = (double)f.getWidth(gc);
               double ya = 0.0;
               double xd = (xo - xoLast) / 1000.0;
               double yd = (yo - yoLast) / 1000.0;
               this.textUtil.writeTd(xd, yd);
               this.textUtil.writeTj((char)gc, true, false);
               xc += xa + (double)pa[2];
               yc += ya + (double)pa[3];
               xoLast = xo;
               yoLast = yo;
            }
         }
      }

   }

   protected void beginTextObject() {
      this.applyColorAndPaint(this.tpi);
      this.textUtil.beginTextObject();
      boolean stroke = this.tpi.strokePaint != null && this.tpi.strokeStroke != null;
      this.textUtil.setTextRenderingMode(this.tpi.fillPaint != null, stroke, false);
   }

   protected void endTextObject() {
      this.textUtil.writeTJ();
      this.textUtil.endTextObject();
   }

   private void applyColorAndPaint(TextPaintInfo tpi) {
      Paint fillPaint = tpi.fillPaint;
      Paint strokePaint = tpi.strokePaint;
      Stroke stroke = tpi.strokeStroke;
      int fillAlpha = 255;
      Color col;
      if (fillPaint instanceof Color) {
         col = (Color)fillPaint;
         this.pdf.applyColor(col, true);
         fillAlpha = col.getAlpha();
      }

      if (strokePaint instanceof Color) {
         col = (Color)strokePaint;
         this.pdf.applyColor(col, false);
      }

      this.pdf.applyPaint(fillPaint, true);
      this.pdf.applyStroke(stroke);
      if (strokePaint != null) {
         this.pdf.applyPaint(strokePaint, false);
      }

      this.pdf.applyAlpha(fillAlpha, 255);
   }

   protected void positionGlyph(Point2D prevPos, Point2D glyphPos, boolean reposition) {
      this.repositionNextGlyph = prevPos == null || prevPos.getY() != glyphPos.getY() || reposition;
      if (!this.repositionNextGlyph) {
         double xdiff = glyphPos.getX() - prevPos.getX();
         double cw = this.prevVisibleGlyphWidth;
         double effxdiff = 1000.0 * xdiff - cw;
         if (effxdiff != 0.0) {
            double adjust = -effxdiff / (double)this.font.getFontSize();
            this.textUtil.adjustGlyphTJ(adjust * 1000.0);
         }
      }

   }

   protected void writeGlyph(char glyph, AffineTransform transform) {
      this.prevVisibleGlyphWidth = (double)this.font.getWidth(glyph);
      boolean encodingChanging = false;
      if (!this.textUtil.isMultiByteFont(this.font.getFontName())) {
         int encoding = glyph / 256;
         glyph = (char)(glyph % 256);
         if (this.textUtil.getCurrentEncoding() != encoding) {
            this.textUtil.setCurrentEncoding(encoding);
            encodingChanging = true;
         }
      }

      if (this.repositionNextGlyph || encodingChanging) {
         this.textUtil.writeTJ();
         if (this.font != this.textUtil.getCurrentFont() || encodingChanging) {
            this.textUtil.setCurrentFont(this.font);
            this.textUtil.writeTf(this.font);
         }

         this.textUtil.writeTextMatrix(transform);
      }

      this.textUtil.writeTJMappedChar(glyph);
   }
}
