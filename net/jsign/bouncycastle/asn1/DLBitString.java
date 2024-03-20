package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class DLBitString extends ASN1BitString {
   public DLBitString(byte[] var1, int var2) {
      super(var1, var2);
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 1 + StreamUtil.calculateBodyLength(this.data.length + 1) + this.data.length + 1;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 3, (byte)((byte)this.padBits), this.data);
   }

   ASN1Primitive toDLObject() {
      return this;
   }
}
