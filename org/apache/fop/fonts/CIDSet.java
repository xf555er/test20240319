package org.apache.fop.fonts;

import java.util.BitSet;
import java.util.Map;

public interface CIDSet {
   int getOriginalGlyphIndex(int var1);

   int getUnicode(int var1);

   char getUnicodeFromGID(int var1);

   int getGIDFromChar(char var1);

   int mapChar(int var1, char var2);

   int mapCodePoint(int var1, int var2);

   Map getGlyphs();

   char[] getChars();

   int getNumberOfGlyphs();

   BitSet getGlyphIndices();

   int[] getWidths();
}
