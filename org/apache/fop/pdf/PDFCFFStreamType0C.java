package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontType;

public class PDFCFFStreamType0C extends AbstractPDFFontStream {
   private byte[] cffData;
   private String type;

   public PDFCFFStreamType0C(CustomFont font) {
      if (font.getEmbeddingMode() == EmbeddingMode.FULL) {
         this.type = "OpenType";
      } else if (font.getFontType() == FontType.TYPE0) {
         this.type = "CIDFontType0C";
      } else {
         this.type = font.getFontType().getName();
      }

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

   public void setData(byte[] data, int size) throws IOException {
      this.cffData = new byte[size];
      System.arraycopy(data, 0, this.cffData, 0, size);
   }
}
