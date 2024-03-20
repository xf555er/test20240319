package net.jsign.bouncycastle.asn1.sec;

import java.util.Enumeration;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERBitString;

public class ECPrivateKey extends ASN1Object {
   private ASN1Sequence seq;

   private ECPrivateKey(ASN1Sequence var1) {
      this.seq = var1;
   }

   public static ECPrivateKey getInstance(Object var0) {
      if (var0 instanceof ECPrivateKey) {
         return (ECPrivateKey)var0;
      } else {
         return var0 != null ? new ECPrivateKey(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public DERBitString getPublicKey() {
      return (DERBitString)this.getObjectInTag(1);
   }

   public ASN1Primitive getParameters() {
      return this.getObjectInTag(0);
   }

   private ASN1Primitive getObjectInTag(int var1) {
      Enumeration var2 = this.seq.getObjects();

      while(var2.hasMoreElements()) {
         ASN1Encodable var3 = (ASN1Encodable)var2.nextElement();
         if (var3 instanceof ASN1TaggedObject) {
            ASN1TaggedObject var4 = (ASN1TaggedObject)var3;
            if (var4.getTagNo() == var1) {
               return var4.getObject().toASN1Primitive();
            }
         }
      }

      return null;
   }

   public ASN1Primitive toASN1Primitive() {
      return this.seq;
   }
}
