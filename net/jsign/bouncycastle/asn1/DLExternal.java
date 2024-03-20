package net.jsign.bouncycastle.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DLExternal extends ASN1External {
   public DLExternal(ASN1EncodableVector var1) {
      super(var1);
   }

   public DLExternal(ASN1ObjectIdentifier var1, ASN1Integer var2, ASN1Primitive var3, int var4, ASN1Primitive var5) {
      super(var1, var2, var3, var4, var5);
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
         var3.write(this.directReference.getEncoded("DL"));
      }

      if (this.indirectReference != null) {
         var3.write(this.indirectReference.getEncoded("DL"));
      }

      if (this.dataValueDescriptor != null) {
         var3.write(this.dataValueDescriptor.getEncoded("DL"));
      }

      DLTaggedObject var4 = new DLTaggedObject(true, this.encoding, this.externalContent);
      var3.write(var4.getEncoded("DL"));
      var1.writeEncoded(var2, 32, (int)8, var3.toByteArray());
   }
}
