package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class DLApplicationSpecific extends ASN1ApplicationSpecific {
   DLApplicationSpecific(boolean var1, int var2, byte[] var3) {
      super(var1, var2, var3);
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      int var3 = 64;
      if (this.isConstructed) {
         var3 |= 32;
      }

      var1.writeEncoded(var2, var3, this.tag, this.octets);
   }
}
