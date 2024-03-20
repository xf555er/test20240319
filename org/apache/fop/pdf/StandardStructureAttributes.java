package org.apache.fop.pdf;

public final class StandardStructureAttributes {
   private StandardStructureAttributes() {
   }

   public static final class Table {
      public static final PDFName NAME = new PDFName("Table");

      public static enum Scope {
         ROW("Row"),
         COLUMN("Column"),
         BOTH("Both");

         private final PDFName name;

         private Scope(String name) {
            this.name = new PDFName(name);
         }

         public PDFName getName() {
            return this.name;
         }

         static void addScopeAttribute(PDFStructElem th, Scope scope) {
            PDFDictionary scopeAttribute = new PDFDictionary();
            scopeAttribute.put("O", StandardStructureAttributes.Table.NAME);
            scopeAttribute.put("Scope", scope.getName());
            th.put("A", scopeAttribute);
         }
      }
   }
}
