package org.apache.fop.render.pdf.extensions;

public enum PDFDictionaryType {
   Action("action", true),
   Catalog("catalog"),
   Dictionary("dictionary"),
   Layer("layer", true),
   Navigator("navigator", true),
   Page("page"),
   Info("info"),
   VT("vt"),
   PagePiece("pagepiece");

   private String elementName;
   private boolean usesIDAttribute;

   private PDFDictionaryType(String elementName, boolean usesIDAttribute) {
      this.elementName = elementName;
      this.usesIDAttribute = usesIDAttribute;
   }

   private PDFDictionaryType(String elementName) {
      this(elementName, false);
   }

   public String elementName() {
      return this.elementName;
   }

   public boolean usesIDAttribute() {
      return this.usesIDAttribute;
   }

   static PDFDictionaryType valueOfElementName(String elementName) {
      PDFDictionaryType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         PDFDictionaryType type = var1[var3];
         if (type.elementName.equals(elementName)) {
            return type;
         }
      }

      throw new IllegalArgumentException();
   }

   static boolean hasValueOfElementName(String elementName) {
      try {
         return valueOfElementName(elementName) != null;
      } catch (Exception var2) {
         return false;
      }
   }
}
