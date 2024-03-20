package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1GeneralizedTime;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.ASN1UTCTime;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.DERTaggedObject;
import net.jsign.bouncycastle.asn1.x500.X500Name;

public class TBSCertList extends ASN1Object {
   ASN1Integer version;
   AlgorithmIdentifier signature;
   X500Name issuer;
   Time thisUpdate;
   Time nextUpdate;
   ASN1Sequence revokedCertificates;
   Extensions crlExtensions;

   public static TBSCertList getInstance(Object var0) {
      if (var0 instanceof TBSCertList) {
         return (TBSCertList)var0;
      } else {
         return var0 != null ? new TBSCertList(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public TBSCertList(ASN1Sequence var1) {
      if (var1.size() >= 3 && var1.size() <= 7) {
         int var2 = 0;
         if (var1.getObjectAt(var2) instanceof ASN1Integer) {
            this.version = ASN1Integer.getInstance(var1.getObjectAt(var2++));
         } else {
            this.version = null;
         }

         this.signature = AlgorithmIdentifier.getInstance(var1.getObjectAt(var2++));
         this.issuer = X500Name.getInstance(var1.getObjectAt(var2++));
         this.thisUpdate = Time.getInstance(var1.getObjectAt(var2++));
         if (var2 < var1.size() && (var1.getObjectAt(var2) instanceof ASN1UTCTime || var1.getObjectAt(var2) instanceof ASN1GeneralizedTime || var1.getObjectAt(var2) instanceof Time)) {
            this.nextUpdate = Time.getInstance(var1.getObjectAt(var2++));
         }

         if (var2 < var1.size() && !(var1.getObjectAt(var2) instanceof ASN1TaggedObject)) {
            this.revokedCertificates = ASN1Sequence.getInstance(var1.getObjectAt(var2++));
         }

         if (var2 < var1.size() && var1.getObjectAt(var2) instanceof ASN1TaggedObject) {
            this.crlExtensions = Extensions.getInstance(ASN1Sequence.getInstance((ASN1TaggedObject)var1.getObjectAt(var2), true));
         }

      } else {
         throw new IllegalArgumentException("Bad sequence size: " + var1.size());
      }
   }

   public X500Name getIssuer() {
      return this.issuer;
   }

   public Extensions getExtensions() {
      return this.crlExtensions;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(7);
      if (this.version != null) {
         var1.add(this.version);
      }

      var1.add(this.signature);
      var1.add(this.issuer);
      var1.add(this.thisUpdate);
      if (this.nextUpdate != null) {
         var1.add(this.nextUpdate);
      }

      if (this.revokedCertificates != null) {
         var1.add(this.revokedCertificates);
      }

      if (this.crlExtensions != null) {
         var1.add(new DERTaggedObject(0, this.crlExtensions));
      }

      return new DERSequence(var1);
   }
}
