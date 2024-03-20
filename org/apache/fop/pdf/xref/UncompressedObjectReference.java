package org.apache.fop.pdf.xref;

import java.io.DataOutputStream;
import java.io.IOException;

class UncompressedObjectReference implements ObjectReference {
   final long offset;

   UncompressedObjectReference(long offset) {
      this.offset = offset;
   }

   public void output(DataOutputStream out) throws IOException {
      out.write(1);
      out.writeLong(this.offset);
      out.write(0);
      out.write(0);
   }
}
