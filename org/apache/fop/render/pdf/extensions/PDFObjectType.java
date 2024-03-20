package org.apache.fop.render.pdf.extensions;

public enum PDFObjectType {
   Array("array"),
   Boolean("boolean"),
   Dictionary("dictionary"),
   Name("name"),
   Number("number"),
   Reference("reference"),
   String("string");

   private String elementName;

   private PDFObjectType(String elementName) {
      this.elementName = elementName;
   }

   public String elementName() {
      return this.elementName;
   }

   static PDFObjectType valueOfElementName(String elementName) {
      PDFObjectType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFObjectType type = var1[var3];
         if (type.elementName.equals(elementName)) {
            return type;
         }
      }

      throw new IllegalArgumentException();
   }

   static boolean hasValueOfElementName(String elementName) {
      try {
         valueOfElementName(elementName);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}
