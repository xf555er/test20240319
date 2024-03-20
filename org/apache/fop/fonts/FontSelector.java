package org.apache.fop.fonts;

import java.util.Iterator;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.util.CharUtilities;

public final class FontSelector {
   private FontSelector() {
   }

   private static Font selectFontForCharacter(char c, FONode fonode, CommonFont commonFont, PercentBaseContext context) {
      FontInfo fi = fonode.getFOEventHandler().getFontInfo();
      FontTriplet[] fontkeys = commonFont.getFontState(fi);
      FontTriplet[] var6 = fontkeys;
      int var7 = fontkeys.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         FontTriplet fontkey = var6[var8];
         Font font = fi.getFontInstance(fontkey, commonFont.fontSize.getValue(context));
         if (font.hasChar(c)) {
            return font;
         }
      }

      return fi.getFontInstance(fontkeys[0], commonFont.fontSize.getValue(context));
   }

   public static Font selectFontForCharacter(Character fobj, PercentBaseContext context) {
      return selectFontForCharacter(fobj.getCharacter(), fobj, fobj.getCommonFont(), context);
   }

   public static Font selectFontForCharacterInText(char c, FOText text, PercentBaseContext context) {
      return selectFontForCharacter(c, text, text.getCommonFont(), context);
   }

   public static Font selectFontForCharactersInText(CharSequence charSeq, int firstIndex, int breakIndex, FOText text, PercentBaseContext context) {
      FontInfo fi = text.getFOEventHandler().getFontInfo();
      CommonFont commonFont = text.getCommonFont();
      FontTriplet[] fontkeys = commonFont.getFontState(fi);
      int numFonts = fontkeys.length;
      Font[] fonts = new Font[numFonts];
      int[] fontCount = new int[numFonts];

      int fontnum;
      for(int fontnum = 0; fontnum < numFonts; ++fontnum) {
         Font font = fi.getFontInstance(fontkeys[fontnum], commonFont.fontSize.getValue(context));
         fonts[fontnum] = font;
         fontnum = 0;
         Iterator var14 = CharUtilities.codepointsIter(charSeq, firstIndex, breakIndex).iterator();

         while(var14.hasNext()) {
            int cp = (Integer)var14.next();
            ++fontnum;
            if (font.hasCodePoint(cp)) {
               int var10002 = fontCount[fontnum]++;
            }
         }

         if (fontCount[fontnum] == fontnum) {
            return font;
         }
      }

      Font font = fonts[0];
      int max = fontCount[0];

      for(fontnum = 1; fontnum < numFonts; ++fontnum) {
         int curCount = fontCount[fontnum];
         if (curCount > max) {
            font = fonts[fontnum];
            max = curCount;
         }
      }

      return font;
   }
}
