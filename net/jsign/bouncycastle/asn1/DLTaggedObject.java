package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class DLTaggedObject extends ASN1TaggedObject {
   public DLTaggedObject(boolean var1, int var2, ASN1Encodable var3) {
      super(var1, var2, var3);
   }

   boolean isConstructed() {
      return this.explicit || this.obj.toASN1Primitive().toDLObject().isConstructed();
   }

   int encodedLength() throws IOException {
      int var1 = this.obj.toASN1Primitive().toDLObject().encodedLength();
      if (this.explicit) {
         return StreamUtil.calculateTagLength(this.tagNo) + StreamUtil.calculateBodyLength(var1) + var1;
      } else {
         --var1;
         return StreamUtil.calculateTagLength(this.tagNo) + var1;
      }
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      ASN1Primitive var3 = this.obj.toASN1Primitive().toDLObject();
      int var4 = 128;
      if (this.explicit || var3.isConstructed()) {
         var4 |= 32;
      }

      var1.writeTag(var2, var4, this.tagNo);
      if (this.explicit) {
         var1.writeLength(var3.encodedLength());
      }

      var1.getDLSubStream().writePrimitive(var3, this.explicit);
   }

   ASN1Primitive toDLObject() {
      return this;
   }
}
