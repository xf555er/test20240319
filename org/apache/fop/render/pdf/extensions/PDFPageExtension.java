package org.apache.fop.render.pdf.extensions;

public class PDFPageExtension extends PDFDictionaryExtension {
   public static final String PROPERTY_PAGE_NUMBERS = "page-numbers";

   PDFPageExtension() {
      super(PDFDictionaryType.Page);
   }

   public boolean matchesPageNumber(int pageNumber) {
      String pageNumbers = this.getProperty("page-numbers");
      if (pageNumbers != null && pageNumbers.length() != 0) {
         if (pageNumbers.equals("*")) {
            return true;
         } else {
            String[] var3 = pageNumbers.split("\\s*,\\s*");
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               String interval = var3[var5];
               String[] components = interval.split("\\s*-\\s*");
               if (components.length >= 1) {
                  try {
                     int start = Integer.parseInt(components[0]);
                     int end = 0;
                     if (components.length > 1 && !components[1].equals("LAST")) {
                        end = Integer.parseInt(components[1]);
                     }

                     if (end == 0 && pageNumber == start) {
                        return true;
                     }

                     if (end > start && pageNumber >= start && pageNumber < end) {
                        return true;
                     }
                  } catch (NumberFormatException var10) {
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }
}
