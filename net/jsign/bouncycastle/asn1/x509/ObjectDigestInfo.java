package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Enumerated;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERBitString;
import net.jsign.bouncycastle.asn1.DERSequence;

public class ObjectDigestInfo extends ASN1Object {
   ASN1Enumerated digestedObjectType;
   ASN1ObjectIdentifier otherObjectTypeID;
   AlgorithmIdentifier digestAlgorithm;
   DERBitString objectDigest;

   public static ObjectDigestInfo getInstance(Object var0) {
      if (var0 instanceof ObjectDigestInfo) {
         return (ObjectDigestInfo)var0;
      } else {
         return var0 != null ? new ObjectDigestInfo(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public static ObjectDigestInfo getInstance(ASN1TaggedObject var0, boolean var1) {
      return getInstance(ASN1Sequence.getInstance(var0, var1));
   }

   private ObjectDigestInfo(ASN1Sequence var1) {
      if (var1.size() <= 4 && var1.size() >= 3) {
         this.digestedObjectType = ASN1Enumerated.getInstance(var1.getObjectAt(0));
         int var2 = 0;
         if (var1.size() == 4) {
            this.otherObjectTypeID = ASN1ObjectIdentifier.getInstance(var1.getObjectAt(1));
            ++var2;
         }

         this.digestAlgorithm = AlgorithmIdentifier.getInstance(var1.getObjectAt(1 + var2));
         this.objectDigest = DERBitString.getInstance(var1.getObjectAt(2 + var2));
      } else {
         throw new IllegalArgumentException("Bad sequence size: " + var1.size());
      }
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(4);
      var1.add(this.digestedObjectType);
      if (this.otherObjectTypeID != null) {
         var1.add(this.otherObjectTypeID);
      }

      var1.add(this.digestAlgorithm);
      var1.add(this.objectDigest);
      return new DERSequence(var1);
   }
}
