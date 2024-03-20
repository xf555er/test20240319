package org.apache.fop.pdf;

public class PDFIdentifiedDictionary extends PDFDictionary {
   private final String id;

   public PDFIdentifiedDictionary(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public boolean hasId(String id) {
      return this.id != null && id != null && this.id.equals(id);
   }
}
