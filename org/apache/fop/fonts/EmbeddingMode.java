package org.apache.fop.fonts;

import java.util.Locale;

public enum EmbeddingMode {
   AUTO,
   FULL,
   SUBSET;

   public String getName() {
      return this.toString().toLowerCase(Locale.ENGLISH);
   }

   public static EmbeddingMode getValue(String value) {
      EmbeddingMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EmbeddingMode mode = var1[var3];
         if (mode.toString().equalsIgnoreCase(value)) {
            return mode;
         }
      }

      throw new IllegalArgumentException("Invalid embedding-mode: " + value);
   }
}
