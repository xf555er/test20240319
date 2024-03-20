package org.apache.batik.gvt.font;

import java.util.Arrays;

public class Kern {
   private int[] firstGlyphCodes;
   private int[] secondGlyphCodes;
   private UnicodeRange[] firstUnicodeRanges;
   private UnicodeRange[] secondUnicodeRanges;
   private float kerningAdjust;

   public Kern(int[] firstGlyphCodes, int[] secondGlyphCodes, UnicodeRange[] firstUnicodeRanges, UnicodeRange[] secondUnicodeRanges, float adjustValue) {
      this.firstGlyphCodes = firstGlyphCodes;
      this.secondGlyphCodes = secondGlyphCodes;
      this.firstUnicodeRanges = firstUnicodeRanges;
      this.secondUnicodeRanges = secondUnicodeRanges;
      this.kerningAdjust = adjustValue;
      if (firstGlyphCodes != null) {
         Arrays.sort(this.firstGlyphCodes);
      }

      if (secondGlyphCodes != null) {
         Arrays.sort(this.secondGlyphCodes);
      }

   }

   public boolean matchesFirstGlyph(int glyphCode, String glyphUnicode) {
      if (this.firstGlyphCodes != null) {
         int pt = Arrays.binarySearch(this.firstGlyphCodes, glyphCode);
         if (pt >= 0) {
            return true;
         }
      }

      if (glyphUnicode.length() < 1) {
         return false;
      } else {
         char glyphChar = glyphUnicode.charAt(0);
         UnicodeRange[] var4 = this.firstUnicodeRanges;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            UnicodeRange firstUnicodeRange = var4[var6];
            if (firstUnicodeRange.contains(glyphChar)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean matchesFirstGlyph(int glyphCode, char glyphUnicode) {
      if (this.firstGlyphCodes != null) {
         int pt = Arrays.binarySearch(this.firstGlyphCodes, glyphCode);
         if (pt >= 0) {
            return true;
         }
      }

      UnicodeRange[] var7 = this.firstUnicodeRanges;
      int var4 = var7.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         UnicodeRange firstUnicodeRange = var7[var5];
         if (firstUnicodeRange.contains(glyphUnicode)) {
            return true;
         }
      }

      return false;
   }

   public boolean matchesSecondGlyph(int glyphCode, String glyphUnicode) {
      if (this.secondGlyphCodes != null) {
         int pt = Arrays.binarySearch(this.secondGlyphCodes, glyphCode);
         if (pt >= 0) {
            return true;
         }
      }

      if (glyphUnicode.length() < 1) {
         return false;
      } else {
         char glyphChar = glyphUnicode.charAt(0);
         UnicodeRange[] var4 = this.secondUnicodeRanges;
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            UnicodeRange secondUnicodeRange = var4[var6];
            if (secondUnicodeRange.contains(glyphChar)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean matchesSecondGlyph(int glyphCode, char glyphUnicode) {
      if (this.secondGlyphCodes != null) {
         int pt = Arrays.binarySearch(this.secondGlyphCodes, glyphCode);
         if (pt >= 0) {
            return true;
         }
      }

      UnicodeRange[] var7 = this.secondUnicodeRanges;
      int var4 = var7.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         UnicodeRange secondUnicodeRange = var7[var5];
         if (secondUnicodeRange.contains(glyphUnicode)) {
            return true;
         }
      }

      return false;
   }

   public float getAdjustValue() {
      return this.kerningAdjust;
   }
}
