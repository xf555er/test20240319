package net.jsign.bouncycastle.asn1.x509;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERBitString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.x500.X500Name;

public class CertificateList extends ASN1Object {
   TBSCertList tbsCertList;
   AlgorithmIdentifier sigAlgId;
   DERBitString sig;
   boolean isHashCodeSet = false;
   int hashCodeValue;

   public static CertificateList getInstance(Object var0) {
      if (var0 instanceof CertificateList) {
         return (CertificateList)var0;
      } else {
         return var0 != null ? new CertificateList(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   /** @deprecated */
   public CertificateList(ASN1Sequence var1) {
      if (var1.size() == 3) {
         this.tbsCertList = TBSCertList.getInstance(var1.getObjectAt(0));
         this.sigAlgId = AlgorithmIdentifier.getInstance(var1.getObjectAt(1));
         this.sig = DERBitString.getInstance(var1.getObjectAt(2));
      } else {
         throw new IllegalArgumentException("sequence wrong size for CertificateList");
      }
   }

   public TBSCertList getTBSCertList() {
      return this.tbsCertList;
   }

   public X500Name getIssuer() {
      return this.tbsCertList.getIssuer();
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(3);
      var1.add(this.tbsCertList);
      var1.add(this.sigAlgId);
      var1.add(this.sig);
      return new DERSequence(var1);
   }

   public int hashCode() {
      if (!this.isHashCodeSet) {
         this.hashCodeValue = super.hashCode();
         this.isHashCodeSet = true;
      }

      return this.hashCodeValue;
   }
}
