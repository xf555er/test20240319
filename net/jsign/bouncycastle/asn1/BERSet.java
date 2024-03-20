package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class BERSet extends ASN1Set {
   public BERSet() {
   }

   public BERSet(ASN1Encodable var1) {
      super(var1);
   }

   public BERSet(ASN1EncodableVector var1) {
      super(var1, false);
   }

   BERSet(boolean var1, ASN1Encodable[] var2) {
      super(var1, var2);
   }

   int encodedLength() throws IOException {
      int var1 = this.elements.length;
      int var2 = 0;

      for(int var3 = 0; var3 < var1; ++var3) {
         ASN1Primitive var4 = this.elements[var3].toASN1Primitive();
         var2 += var4.encodedLength();
      }

      return 2 + var2 + 2;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncodedIndef(var2, 49, (ASN1Encodable[])this.elements);
   }
}
