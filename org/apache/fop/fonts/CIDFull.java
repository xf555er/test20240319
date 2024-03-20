package org.apache.fop.fonts;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CIDFull implements CIDSet {
   private BitSet glyphIndices;
   private final MultiByteFont font;

   public CIDFull(MultiByteFont mbf) {
      this.font = mbf;
   }

   private void initGlyphIndices() {
      if (this.glyphIndices == null) {
         this.glyphIndices = this.font.getGlyphIndices();
      }

   }

   public int getOriginalGlyphIndex(int index) {
      return index;
   }

   public char getUnicodeFromGID(int glyphIndex) {
      return ' ';
   }

   public int getGIDFromChar(char ch) {
      return ch;
   }

   public int getUnicode(int index) {
      this.initGlyphIndices();
      return this.glyphIndices.get(index) ? index : '\uffff';
   }

   public int mapChar(int glyphIndex, char unicode) {
      return glyphIndex;
   }

   public int mapCodePoint(int glyphIndex, int codePoint) {
      return glyphIndex;
   }

   public Map getGlyphs() {
      this.initGlyphIndices();
      Map glyphs = new HashMap();
      int nextBitSet = 0;

      for(int j = 0; j < this.glyphIndices.cardinality(); ++j) {
         nextBitSet = this.glyphIndices.nextSetBit(nextBitSet);
         glyphs.put(nextBitSet, nextBitSet);
         ++nextBitSet;
      }

      return Collections.unmodifiableMap(glyphs);
   }

   public char[] getChars() {
      return this.font.getChars();
   }

   public int getNumberOfGlyphs() {
      this.initGlyphIndices();
      return this.glyphIndices.length();
   }

   public BitSet getGlyphIndices() {
      this.initGlyphIndices();
      return this.glyphIndices;
   }

   public int[] getWidths() {
      return this.font.getWidths();
   }
}
