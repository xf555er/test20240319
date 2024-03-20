package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

public class BERTaggedObject extends ASN1TaggedObject {
   public BERTaggedObject(int var1, ASN1Encodable var2) {
      super(true, var1, var2);
   }

   public BERTaggedObject(boolean var1, int var2, ASN1Encodable var3) {
      super(var1, var2, var3);
   }

   boolean isConstructed() {
      return this.explicit || this.obj.toASN1Primitive().isConstructed();
   }

   int encodedLength() throws IOException {
      ASN1Primitive var1 = this.obj.toASN1Primitive();
      int var2 = var1.encodedLength();
      if (this.explicit) {
         return StreamUtil.calculateTagLength(this.tagNo) + StreamUtil.calculateBodyLength(var2) + var2;
      } else {
         --var2;
         return StreamUtil.calculateTagLength(this.tagNo) + var2;
      }
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeTag(var2, 160, this.tagNo);
      var1.write(128);
      if (!this.explicit) {
         Enumeration var3;
         if (this.obj instanceof ASN1OctetString) {
            if (this.obj instanceof BEROctetString) {
               var3 = ((BEROctetString)this.obj).getObjects();
            } else {
               ASN1OctetString var4 = (ASN1OctetString)this.obj;
               BEROctetString var5 = new BEROctetString(var4.getOctets());
               var3 = var5.getObjects();
            }
         } else if (this.obj instanceof ASN1Sequence) {
            var3 = ((ASN1Sequence)this.obj).getObjects();
         } else {
            if (!(this.obj instanceof ASN1Set)) {
               throw new ASN1Exception("not implemented: " + this.obj.getClass().getName());
            }

            var3 = ((ASN1Set)this.obj).getObjects();
         }

         var1.writeElements(var3);
      } else {
         var1.writePrimitive(this.obj.toASN1Primitive(), true);
      }

      var1.write(0);
      var1.write(0);
   }
}
