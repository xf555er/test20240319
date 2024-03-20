package org.apache.fop.pdf;

public enum PDFUAMode {
   DISABLED("PDF/UA disabled"),
   PDFUA_1(1);

   private final String name;
   private final int part;

   private PDFUAMode(String name) {
      this.name = name;
      this.part = 0;
   }

   private PDFUAMode(int part) {
      this.name = "PDF/UA-" + part;
      this.part = part;
   }

   public String getName() {
      return this.name;
   }

   public int getPart() {
      return this.part;
   }

   public boolean isEnabled() {
      return this != DISABLED;
   }

   public static PDFUAMode getValueOf(String s) {
      PDFUAMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFUAMode mode = var1[var3];
         if (mode.name.equalsIgnoreCase(s)) {
            return mode;
         }
      }

      return DISABLED;
   }

   public String toString() {
      return this.name;
   }
}
