package org.apache.fop.pdf;

public enum PDFXMode {
   DISABLED("PDF/X disabled"),
   PDFX_3_2003("PDF/X-3:2003"),
   PDFX_4("PDF/X-4");

   private String name;

   private PDFXMode(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static PDFXMode getValueOf(String s) {
      PDFXMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFXMode mode = var1[var3];
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
