package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;

class DEROutputStream extends ASN1OutputStream {
   DEROutputStream(OutputStream var1) {
      super(var1);
   }

   void writePrimitive(ASN1Primitive var1, boolean var2) throws IOException {
      var1.toDERObject().encode(this, var2);
   }

   DEROutputStream getDERSubStream() {
      return this;
   }

   ASN1OutputStream getDLSubStream() {
      return this;
   }
}
