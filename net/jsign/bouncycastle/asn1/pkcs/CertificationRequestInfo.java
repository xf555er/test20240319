package net.jsign.bouncycastle.asn1.pkcs;

import java.util.Enumeration;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.DERTaggedObject;
import net.jsign.bouncycastle.asn1.x500.X500Name;
import net.jsign.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public class CertificationRequestInfo extends ASN1Object {
   ASN1Integer version = new ASN1Integer(0L);
   X500Name subject;
   SubjectPublicKeyInfo subjectPKInfo;
   ASN1Set attributes = null;

   public static CertificationRequestInfo getInstance(Object var0) {
      if (var0 instanceof CertificationRequestInfo) {
         return (CertificationRequestInfo)var0;
      } else {
         return var0 != null ? new CertificationRequestInfo(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   /** @deprecated */
   public CertificationRequestInfo(ASN1Sequence var1) {
      this.version = (ASN1Integer)var1.getObjectAt(0);
      this.subject = X500Name.getInstance(var1.getObjectAt(1));
      this.subjectPKInfo = SubjectPublicKeyInfo.getInstance(var1.getObjectAt(2));
      if (var1.size() > 3) {
         ASN1TaggedObject var2 = (ASN1TaggedObject)var1.getObjectAt(3);
         this.attributes = ASN1Set.getInstance(var2, false);
      }

      validateAttributes(this.attributes);
      if (this.subject == null || this.version == null || this.subjectPKInfo == null) {
         throw new IllegalArgumentException("Not all mandatory fields set in CertificationRequestInfo generator.");
      }
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(4);
      var1.add(this.version);
      var1.add(this.subject);
      var1.add(this.subjectPKInfo);
      if (this.attributes != null) {
         var1.add(new DERTaggedObject(false, 0, this.attributes));
      }

      return new DERSequence(var1);
   }

   private static void validateAttributes(ASN1Set var0) {
      if (var0 != null) {
         Enumeration var1 = var0.getObjects();

         Attribute var2;
         do {
            if (!var1.hasMoreElements()) {
               return;
            }

            var2 = Attribute.getInstance(var1.nextElement());
         } while(!var2.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_challengePassword) || var2.getAttrValues().size() == 1);

         throw new IllegalArgumentException("challengePassword attribute must have one value");
      }
   }
}
