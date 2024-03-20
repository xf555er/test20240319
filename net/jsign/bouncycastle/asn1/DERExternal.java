package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DERExternal extends ASN1External {
   public DERExternal(ASN1ObjectIdentifier var1, ASN1Integer var2, ASN1Primitive var3, int var4, ASN1Primitive var5) {
      super(var1, var2, var3, var4, var5);
   }

   ASN1Primitive toDERObject() {
      return this;
   }

   ASN1Primitive toDLObject() {
      return this;
   }

   int encodedLength() throws IOException {
      return this.getEncoded().length;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      ByteArrayOutputStream var3 = new ByteArrayOutputStream();
      if (this.directReference != null) {
         var3.write(this.directReference.getEncoded("DER"));
      }

      if (this.indirectReference != null) {
         var3.write(this.indirectReference.getEncoded("DER"));
      }

      if (this.dataValueDescriptor != null) {
         var3.write(this.dataValueDescriptor.getEncoded("DER"));
      }

      DERTaggedObject var4 = new DERTaggedObject(true, this.encoding, this.externalContent);
      var3.write(var4.getEncoded("DER"));
      var1.writeEncoded(var2, 32, (int)8, var3.toByteArray());
   }
}
