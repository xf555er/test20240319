package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFCFFStream extends AbstractPDFFontStream {
   private byte[] cffData;
   private String type;

   public PDFCFFStream(String type) {
      this.type = type;
   }

   protected int getSizeHint() throws IOException {
      return this.cffData != null ? this.cffData.length : 0;
   }

   protected void outputRawStreamData(OutputStream out) throws IOException {
      out.write(this.cffData);
   }

   protected void populateStreamDict(Object lengthEntry) {
      this.put("Subtype", new PDFName(this.type));
      super.populateStreamDict(lengthEntry);
   }

   public void setData(byte[] data) throws IOException {
      this.cffData = data;
   }
}
