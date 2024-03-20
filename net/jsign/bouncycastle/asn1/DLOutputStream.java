package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

class DLOutputStream extends ASN1OutputStream {
   DLOutputStream(OutputStream var1) {
      super(var1);
   }

   void writePrimitive(ASN1Primitive var1, boolean var2) throws IOException {
      var1.toDLObject().encode(this, var2);
   }

   ASN1OutputStream getDLSubStream() {
      return this;
   }
}
