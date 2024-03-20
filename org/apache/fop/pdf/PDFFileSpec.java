package org.apache.fop.pdf;

public class PDFFileSpec extends PDFDictionary {
   public PDFFileSpec(String filename) {
      this(filename, filename);
   }

   public PDFFileSpec(String filename, String unicodeFilename) {
      this.put("Type", new PDFName("Filespec"));
      this.put("F", filename);
      this.put("UF", unicodeFilename);
   }

   public String getFilename() {
      return (String)this.get("F");
   }

   public String getUnicodeFilename() {
      return (String)this.get("UF");
   }

   public void setEmbeddedFile(PDFDictionary embeddedFileDict) {
      this.put("EF", embeddedFileDict);
   }

   public void setDescription(String description) {
      this.put("Desc", description);
   }

   protected boolean contentEquals(PDFObject obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj instanceof PDFFileSpec) {
         PDFFileSpec spec = (PDFFileSpec)obj;
         return spec.getFilename().equals(this.getFilename());
      } else {
         return false;
      }
   }
}
