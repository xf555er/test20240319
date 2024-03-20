package org.apache.fop.fonts;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class CIDSubset implements CIDSet {
   private Map usedGlyphs = new LinkedHashMap();
   private Map usedGlyphsIndex = new HashMap();
   private int usedGlyphsCount;
   private Map usedCharsIndex = new HashMap();
   private Map charToGIDs = new HashMap();
   private final MultiByteFont font;

   public CIDSubset(MultiByteFont mbf) {
      this.font = mbf;
      this.usedGlyphs.put(0, 0);
      this.usedGlyphsIndex.put(0, 0);
      ++this.usedGlyphsCount;
   }

   public int getOriginalGlyphIndex(int index) {
      Integer glyphIndex = (Integer)this.usedGlyphsIndex.get(index);
      return glyphIndex != null ? glyphIndex : -1;
   }

   public int getUnicode(int index) {
      Integer mapValue = (Integer)this.usedCharsIndex.get(index);
      return mapValue != null ? mapValue : '\uffff';
   }

   public int mapChar(int glyphIndex, char unicode) {
      return this.mapCodePoint(glyphIndex, unicode);
   }

   public int mapCodePoint(int glyphIndex, int codePoint) {
      Integer subsetCharSelector = (Integer)this.usedGlyphs.get(glyphIndex);
      if (subsetCharSelector == null) {
         int selector = this.usedGlyphsCount;
         this.usedGlyphs.put(glyphIndex, selector);
         this.usedGlyphsIndex.put(selector, glyphIndex);
         this.usedCharsIndex.put(selector, codePoint);
         this.charToGIDs.put(codePoint, glyphIndex);
         ++this.usedGlyphsCount;
         return selector;
      } else {
         return subsetCharSelector;
      }
   }

   public Map getGlyphs() {
      return Collections.unmodifiableMap(this.usedGlyphs);
   }

   public char getUnicodeFromGID(int glyphIndex) {
      int selector = (Integer)this.usedGlyphs.get(glyphIndex);
      return (char)(Integer)this.usedCharsIndex.get(selector);
   }

   public int getGIDFromChar(char ch) {
      return (Integer)this.charToGIDs.get(Integer.valueOf(ch));
   }

   public char[] getChars() {
      StringBuilder buf = new StringBuilder();

      for(int i = 0; i < this.usedGlyphsCount; ++i) {
         buf.appendCodePoint(this.getUnicode(i));
      }

      return buf.toString().toCharArray();
   }

   public int getNumberOfGlyphs() {
      return this.usedGlyphsCount;
   }

   public BitSet getGlyphIndices() {
      BitSet bitset = new BitSet();
      Iterator var2 = this.usedGlyphs.keySet().iterator();

      while(var2.hasNext()) {
         Integer cid = (Integer)var2.next();
         bitset.set(cid);
      }

      return bitset;
   }

   public int[] getWidths() {
      int[] widths = this.font.getWidths();
      int[] tmpWidth = new int[this.getNumberOfGlyphs()];
      int i = 0;

      for(int c = this.getNumberOfGlyphs(); i < c; ++i) {
         int nwx = Math.max(0, this.getOriginalGlyphIndex(i));
         tmpWidth[i] = widths[nwx];
      }

      return tmpWidth;
   }
}
