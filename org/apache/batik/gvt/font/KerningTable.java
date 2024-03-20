package org.apache.batik.gvt.font;

public class KerningTable {
   private Kern[] entries;

   public KerningTable(Kern[] entries) {
      this.entries = entries;
   }

   public float getKerningValue(int glyphCode1, int glyphCode2, String glyphUnicode1, String glyphUnicode2) {
      Kern[] var5 = this.entries;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         Kern entry = var5[var7];
         if (entry.matchesFirstGlyph(glyphCode1, glyphUnicode1) && entry.matchesSecondGlyph(glyphCode2, glyphUnicode2)) {
            return entry.getAdjustValue();
         }
      }

      return 0.0F;
   }
}
