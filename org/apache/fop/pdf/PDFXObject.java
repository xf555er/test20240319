package org.apache.fop.pdf;

import java.io.IOException;

public abstract class PDFXObject extends AbstractPDFStream {
   public PDFXObject() {
   }

   protected PDFXObject(PDFDictionary dictionary) {
      super(dictionary);
   }

   public PDFName getName() {
      return (PDFName)this.get("Name");
   }

   protected void populateStreamDict(Object lengthEntry) {
      this.put("Type", new PDFName("XObject"));
      super.populateStreamDict(lengthEntry);
   }

   protected int getSizeHint() throws IOException {
      return 0;
   }
}
