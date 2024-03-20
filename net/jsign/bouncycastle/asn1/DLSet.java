package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class DLSet extends ASN1Set {
   private int bodyLength = -1;

   public DLSet() {
   }

   public DLSet(ASN1Encodable var1) {
      super(var1);
   }

   public DLSet(ASN1EncodableVector var1) {
      super(var1, false);
   }

   public DLSet(ASN1Encodable[] var1) {
      super(var1, false);
   }

   DLSet(boolean var1, ASN1Encodable[] var2) {
      super(var1, var2);
   }

   private int getBodyLength() throws IOException {
      if (this.bodyLength < 0) {
         int var1 = this.elements.length;
         int var2 = 0;

         for(int var3 = 0; var3 < var1; ++var3) {
            ASN1Primitive var4 = this.elements[var3].toASN1Primitive().toDLObject();
            var2 += var4.encodedLength();
         }

         this.bodyLength = var2;
      }

      return this.bodyLength;
   }

   int encodedLength() throws IOException {
      int var1 = this.getBodyLength();
      return 1 + StreamUtil.calculateBodyLength(var1) + var1;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      if (var2) {
         var1.write(49);
      }

      ASN1OutputStream var3 = var1.getDLSubStream();
      int var4 = this.elements.length;
      int var5;
      if (this.bodyLength < 0 && var4 <= 16) {
         var5 = 0;
         ASN1Primitive[] var6 = new ASN1Primitive[var4];

         int var7;
         for(var7 = 0; var7 < var4; ++var7) {
            ASN1Primitive var8 = this.elements[var7].toASN1Primitive().toDLObject();
            var6[var7] = var8;
            var5 += var8.encodedLength();
         }

         this.bodyLength = var5;
         var1.writeLength(var5);

         for(var7 = 0; var7 < var4; ++var7) {
            var3.writePrimitive(var6[var7], true);
         }
      } else {
         var1.writeLength(this.getBodyLength());

         for(var5 = 0; var5 < var4; ++var5) {
            var3.writePrimitive(this.elements[var5].toASN1Primitive(), true);
         }
      }

   }

   ASN1Primitive toDLObject() {
      return this;
   }
}
