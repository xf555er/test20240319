package org.apache.fop.fonts;

public enum EncodingMode {
   AUTO("auto"),
   SINGLE_BYTE("single-byte"),
   CID("cid");

   private String name;

   private EncodingMode(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static EncodingMode getValue(String name) {
      EncodingMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EncodingMode em = var1[var3];
         if (name.equalsIgnoreCase(em.getName())) {
            return em;
         }
      }

      throw new IllegalArgumentException("Invalid encoding mode: " + name);
   }

   public String toString() {
      return "EncodingMode: " + this.getName();
   }
}
