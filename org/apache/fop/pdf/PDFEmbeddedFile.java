package org.apache.fop.pdf;

import java.io.IOException;
import java.util.Date;

public class PDFEmbeddedFile extends PDFStream {
   public PDFEmbeddedFile() {
      this.put("Type", new PDFName("EmbeddedFile"));
      this.put("Subtype", new PDFName("application/octet-stream"));
      PDFDictionary params = new PDFDictionary();
      params.put("CreationDate", PDFInfo.formatDateTime(new Date()));
      params.put("ModDate", PDFInfo.formatDateTime(new Date()));
      this.put("Params", params);
   }

   protected boolean isEncodingOnTheFly() {
      return false;
   }

   protected void populateStreamDict(Object lengthEntry) {
      super.populateStreamDict(lengthEntry);

      try {
         PDFDictionary dict = (PDFDictionary)this.get("Params");
         dict.put("Size", this.data.getSize());
      } catch (IOException var3) {
      }

   }
}
