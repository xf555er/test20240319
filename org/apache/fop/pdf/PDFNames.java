package org.apache.fop.pdf;

public class PDFNames extends PDFDictionary {
   private static final String DESTS = "Dests";
   private static final String EMBEDDED_FILES = "EmbeddedFiles";

   public PDFDests getDests() {
      return (PDFDests)this.get("Dests");
   }

   public void setDests(PDFDests dests) {
      this.put("Dests", dests);
   }

   public PDFEmbeddedFiles getEmbeddedFiles() {
      return (PDFEmbeddedFiles)this.get("EmbeddedFiles");
   }

   public void setEmbeddedFiles(PDFEmbeddedFiles embeddedFiles) {
      this.put("EmbeddedFiles", embeddedFiles);
   }
}
