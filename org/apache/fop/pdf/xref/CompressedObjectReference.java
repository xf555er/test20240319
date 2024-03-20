package org.apache.fop.pdf.xref;

import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.fop.pdf.PDFObjectNumber;

public class CompressedObjectReference implements ObjectReference {
   private final PDFObjectNumber objectNumber;
   private final PDFObjectNumber objectStreamNumber;
   private final int index;

   public CompressedObjectReference(PDFObjectNumber objectNumber, PDFObjectNumber objectStreamNumber, int index) {
      this.objectNumber = objectNumber;
      this.objectStreamNumber = objectStreamNumber;
      this.index = index;
   }

   public void output(DataOutputStream out) throws IOException {
      out.write(2);
      out.writeLong((long)this.objectStreamNumber.getNumber());
      out.write(0);
      out.write(this.index);
   }

   public PDFObjectNumber getObjectNumber() {
      return this.objectNumber;
   }

   public PDFObjectNumber getObjectStreamNumber() {
      return this.objectStreamNumber;
   }
}
