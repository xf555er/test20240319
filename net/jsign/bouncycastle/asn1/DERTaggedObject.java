package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class DERTaggedObject extends ASN1TaggedObject {
   public DERTaggedObject(boolean var1, int var2, ASN1Encodable var3) {
      super(var1, var2, var3);
   }

   public DERTaggedObject(int var1, ASN1Encodable var2) {
      super(true, var1, var2);
   }

   boolean isConstructed() {
      return this.explicit || this.obj.toASN1Primitive().toDERObject().isConstructed();
   }

   int encodedLength() throws IOException {
      ASN1Primitive var1 = this.obj.toASN1Primitive().toDERObject();
      int var2 = var1.encodedLength();
      if (this.explicit) {
         return StreamUtil.calculateTagLength(this.tagNo) + StreamUtil.calculateBodyLength(var2) + var2;
      } else {
         --var2;
         return StreamUtil.calculateTagLength(this.tagNo) + var2;
      }
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      ASN1Primitive var3 = this.obj.toASN1Primitive().toDERObject();
      int var4 = 128;
      if (this.explicit || var3.isConstructed()) {
         var4 |= 32;
      }

      var1.writeTag(var2, var4, this.tagNo);
      if (this.explicit) {
         var1.writeLength(var3.encodedLength());
      }

      var3.encode(var1.getDERSubStream(), this.explicit);
   }

   ASN1Primitive toDERObject() {
      return this;
   }

   ASN1Primitive toDLObject() {
      return this;
   }
}
