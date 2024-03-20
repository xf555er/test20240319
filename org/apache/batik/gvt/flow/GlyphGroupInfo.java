package org.apache.batik.gvt.flow;

import org.apache.batik.gvt.font.GVTGlyphVector;

public class GlyphGroupInfo {
   int start;
   int end;
   int glyphCount;
   int lastGlyphCount;
   boolean hideLast;
   float advance;
   float lastAdvance;
   int range;
   GVTGlyphVector gv;
   boolean[] hide;

   public GlyphGroupInfo(GVTGlyphVector gv, int start, int end, boolean[] glyphHide, boolean glyphGroupHideLast, float[] glyphPos, float[] advAdj, float[] lastAdvAdj, boolean[] space) {
      this.gv = gv;
      this.start = start;
      this.end = end;
      this.hide = new boolean[this.end - this.start + 1];
      this.hideLast = glyphGroupHideLast;
      System.arraycopy(glyphHide, this.start, this.hide, 0, this.hide.length);
      float adv = glyphPos[2 * end + 2] - glyphPos[2 * start];
      float ladv = adv;
      adv += advAdj[end];
      int glyphCount = end - start + 1;

      int lastGlyphCount;
      for(lastGlyphCount = start; lastGlyphCount < end; ++lastGlyphCount) {
         if (glyphHide[lastGlyphCount]) {
            --glyphCount;
         }
      }

      lastGlyphCount = glyphCount;

      for(int g = end; g >= start; --g) {
         ladv += lastAdvAdj[g];
         if (!space[g]) {
            break;
         }

         --lastGlyphCount;
      }

      if (this.hideLast) {
         --lastGlyphCount;
      }

      this.glyphCount = glyphCount;
      this.lastGlyphCount = lastGlyphCount;
      this.advance = adv;
      this.lastAdvance = ladv;
   }

   public GVTGlyphVector getGlyphVector() {
      return this.gv;
   }

   public int getStart() {
      return this.start;
   }

   public int getEnd() {
      return this.end;
   }

   public int getGlyphCount() {
      return this.glyphCount;
   }

   public int getLastGlyphCount() {
      return this.lastGlyphCount;
   }

   public boolean[] getHide() {
      return this.hide;
   }

   public boolean getHideLast() {
      return this.hideLast;
   }

   public float getAdvance() {
      return this.advance;
   }

   public float getLastAdvance() {
      return this.lastAdvance;
   }

   public void setRange(int range) {
      this.range = range;
   }

   public int getRange() {
      return this.range;
   }
}
