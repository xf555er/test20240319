package org.apache.fop.pdf;

public enum PDFVTMode {
   DISABLED("PDF/VT disabled"),
   PDFVT_1("PDF/VT-1");

   private String name;

   private PDFVTMode(String s) {
      this.name = s;
   }

   public static PDFVTMode getValueOf(String s) {
      PDFVTMode[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFVTMode mode = var1[var3];
         if (mode.name.equalsIgnoreCase(s)) {
            return mode;
         }
      }

      return DISABLED;
   }
}
