package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BERPrivate extends ASN1Private {
   public BERPrivate(int var1, ASN1EncodableVector var2) {
      super(true, var1, getEncodedVector(var2));
   }

   private static byte[] getEncodedVector(ASN1EncodableVector var0) {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();

      for(int var2 = 0; var2 != var0.size(); ++var2) {
         try {
            var1.write(((ASN1Object)var0.get(var2)).getEncoded("BER"));
         } catch (IOException var4) {
            throw new ASN1ParsingException("malformed object: " + var4, var4);
         }
      }

      return var1.toByteArray();
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      int var3 = 192;
      if (this.isConstructed) {
         var3 |= 32;
      }

      var1.writeEncodedIndef(var2, var3, this.tag, this.octets);
   }
}
