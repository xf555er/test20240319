package org.apache.batik.gvt.font;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphJustificationInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.gvt.text.AttributedCharacterSpanIterator;

public class MultiGlyphVector implements GVTGlyphVector {
   GVTGlyphVector[] gvs;
   int[] nGlyphs;
   int[] off;
   int nGlyph;

   public MultiGlyphVector(List gvs) {
      int nSlots = gvs.size();
      this.gvs = new GVTGlyphVector[nSlots];
      this.nGlyphs = new int[nSlots];
      this.off = new int[nSlots];
      Iterator iter = gvs.iterator();

      int i;
      for(i = 0; iter.hasNext(); ++i) {
         this.off[i] = this.nGlyph;
         GVTGlyphVector gv = (GVTGlyphVector)iter.next();
         this.gvs[i] = gv;
         this.nGlyphs[i] = gv.getNumGlyphs();
         this.nGlyph += this.nGlyphs[i];
      }

      int var10002 = this.nGlyphs[i - 1]++;
   }

   public int getNumGlyphs() {
      return this.nGlyph;
   }

   int getGVIdx(int glyphIdx) {
      if (glyphIdx > this.nGlyph) {
         return -1;
      } else if (glyphIdx == this.nGlyph) {
         return this.gvs.length - 1;
      } else {
         for(int i = 0; i < this.nGlyphs.length; ++i) {
            if (glyphIdx - this.off[i] < this.nGlyphs[i]) {
               return i;
            }
         }

         return -1;
      }
   }

   public GVTFont getFont() {
      throw new IllegalArgumentException("Can't be correctly Implemented");
   }

   public FontRenderContext getFontRenderContext() {
      return this.gvs[0].getFontRenderContext();
   }

   public int getGlyphCode(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphCode(glyphIndex - this.off[idx]);
   }

   public GlyphJustificationInfo getGlyphJustificationInfo(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphJustificationInfo(glyphIndex - this.off[idx]);
   }

   public Shape getGlyphLogicalBounds(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphLogicalBounds(glyphIndex - this.off[idx]);
   }

   public GVTGlyphMetrics getGlyphMetrics(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphMetrics(glyphIndex - this.off[idx]);
   }

   public Shape getGlyphOutline(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphOutline(glyphIndex - this.off[idx]);
   }

   public Rectangle2D getGlyphCellBounds(int glyphIndex) {
      return this.getGlyphLogicalBounds(glyphIndex).getBounds2D();
   }

   public Point2D getGlyphPosition(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphPosition(glyphIndex - this.off[idx]);
   }

   public AffineTransform getGlyphTransform(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphTransform(glyphIndex - this.off[idx]);
   }

   public Shape getGlyphVisualBounds(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].getGlyphVisualBounds(glyphIndex - this.off[idx]);
   }

   public void setGlyphPosition(int glyphIndex, Point2D newPos) {
      int idx = this.getGVIdx(glyphIndex);
      this.gvs[idx].setGlyphPosition(glyphIndex - this.off[idx], newPos);
   }

   public void setGlyphTransform(int glyphIndex, AffineTransform newTX) {
      int idx = this.getGVIdx(glyphIndex);
      this.gvs[idx].setGlyphTransform(glyphIndex - this.off[idx], newTX);
   }

   public void setGlyphVisible(int glyphIndex, boolean visible) {
      int idx = this.getGVIdx(glyphIndex);
      this.gvs[idx].setGlyphVisible(glyphIndex - this.off[idx], visible);
   }

   public boolean isGlyphVisible(int glyphIndex) {
      int idx = this.getGVIdx(glyphIndex);
      return this.gvs[idx].isGlyphVisible(glyphIndex - this.off[idx]);
   }

   public int[] getGlyphCodes(int beginGlyphIndex, int numEntries, int[] codeReturn) {
      int[] ret = codeReturn;
      if (codeReturn == null) {
         ret = new int[numEntries];
      }

      int[] tmp = null;
      int gvIdx = this.getGVIdx(beginGlyphIndex);
      int gi = beginGlyphIndex - this.off[gvIdx];

      int len;
      for(int i = 0; numEntries != 0; i += len) {
         len = numEntries;
         if (gi + numEntries > this.nGlyphs[gvIdx]) {
            len = this.nGlyphs[gvIdx] - gi;
         }

         GVTGlyphVector gv = this.gvs[gvIdx];
         if (i == 0) {
            gv.getGlyphCodes(gi, len, ret);
         } else {
            if (tmp == null || tmp.length < len) {
               tmp = new int[len];
            }

            gv.getGlyphCodes(gi, len, tmp);
            System.arraycopy(tmp, 0, ret, i, len);
         }

         gi = 0;
         ++gvIdx;
         numEntries -= len;
      }

      return ret;
   }

   public float[] getGlyphPositions(int beginGlyphIndex, int numEntries, float[] positionReturn) {
      float[] ret = positionReturn;
      if (positionReturn == null) {
         ret = new float[numEntries * 2];
      }

      float[] tmp = null;
      int gvIdx = this.getGVIdx(beginGlyphIndex);
      int gi = beginGlyphIndex - this.off[gvIdx];

      int len;
      for(int i = 0; numEntries != 0; i += len * 2) {
         len = numEntries;
         if (gi + numEntries > this.nGlyphs[gvIdx]) {
            len = this.nGlyphs[gvIdx] - gi;
         }

         GVTGlyphVector gv = this.gvs[gvIdx];
         if (i == 0) {
            gv.getGlyphPositions(gi, len, ret);
         } else {
            if (tmp == null || tmp.length < len * 2) {
               tmp = new float[len * 2];
            }

            gv.getGlyphPositions(gi, len, tmp);
            System.arraycopy(tmp, 0, ret, i, len * 2);
         }

         gi = 0;
         ++gvIdx;
         numEntries -= len;
      }

      return ret;
   }

   public Rectangle2D getLogicalBounds() {
      Rectangle2D ret = null;
      GVTGlyphVector[] var2 = this.gvs;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         GVTGlyphVector gv = var2[var4];
         Rectangle2D b = gv.getLogicalBounds();
         if (ret == null) {
            ret = b;
         } else {
            ret.add(b);
         }
      }

      return ret;
   }

   public Shape getOutline() {
      GeneralPath ret = null;
      GVTGlyphVector[] var2 = this.gvs;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         GVTGlyphVector gv = var2[var4];
         Shape s = gv.getOutline();
         if (ret == null) {
            ret = new GeneralPath(s);
         } else {
            ret.append(s, false);
         }
      }

      return ret;
   }

   public Shape getOutline(float x, float y) {
      Shape outline = this.getOutline();
      AffineTransform tr = AffineTransform.getTranslateInstance((double)x, (double)y);
      outline = tr.createTransformedShape(outline);
      return outline;
   }

   public Rectangle2D getBounds2D(AttributedCharacterIterator aci) {
      Rectangle2D ret = null;
      int begin = aci.getBeginIndex();
      GVTGlyphVector[] var4 = this.gvs;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         GVTGlyphVector gv = var4[var6];
         int end = gv.getCharacterCount(0, gv.getNumGlyphs()) + 1;
         Rectangle2D b = gv.getBounds2D(new AttributedCharacterSpanIterator(aci, begin, end));
         if (ret == null) {
            ret = b;
         } else {
            ret.add(b);
         }

         begin = end;
      }

      return ret;
   }

   public Rectangle2D getGeometricBounds() {
      Rectangle2D ret = null;
      GVTGlyphVector[] var2 = this.gvs;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         GVTGlyphVector gv = var2[var4];
         Rectangle2D b = gv.getGeometricBounds();
         if (ret == null) {
            ret = b;
         } else {
            ret.add(b);
         }
      }

      return ret;
   }

   public void performDefaultLayout() {
      GVTGlyphVector[] var1 = this.gvs;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         GVTGlyphVector gv = var1[var3];
         gv.performDefaultLayout();
      }

   }

   public int getCharacterCount(int startGlyphIndex, int endGlyphIndex) {
      int idx1 = this.getGVIdx(startGlyphIndex);
      int idx2 = this.getGVIdx(endGlyphIndex);
      int ret = 0;

      for(int idx = idx1; idx <= idx2; ++idx) {
         int gi1 = startGlyphIndex - this.off[idx];
         int gi2 = endGlyphIndex - this.off[idx];
         if (gi2 >= this.nGlyphs[idx]) {
            gi2 = this.nGlyphs[idx] - 1;
         }

         ret += this.gvs[idx].getCharacterCount(gi1, gi2);
         startGlyphIndex += gi2 - gi1 + 1;
      }

      return ret;
   }

   public boolean isReversed() {
      return false;
   }

   public void maybeReverse(boolean mirror) {
   }

   public void draw(Graphics2D g2d, AttributedCharacterIterator aci) {
      int begin = aci.getBeginIndex();
      GVTGlyphVector[] var4 = this.gvs;
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         GVTGlyphVector gv = var4[var6];
         int end = gv.getCharacterCount(0, gv.getNumGlyphs()) + 1;
         gv.draw(g2d, new AttributedCharacterSpanIterator(aci, begin, end));
         begin = end;
      }

   }
}
