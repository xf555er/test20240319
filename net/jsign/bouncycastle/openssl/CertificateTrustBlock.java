package net.jsign.bouncycastle.openssl;

import java.util.Enumeration;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERUTF8String;

public class CertificateTrustBlock {
   private ASN1Sequence uses;
   private ASN1Sequence prohibitions;
   private String alias;

   CertificateTrustBlock(byte[] var1) {
      ASN1Sequence var2 = ASN1Sequence.getInstance(var1);
      Enumeration var3 = var2.getObjects();

      while(var3.hasMoreElements()) {
         ASN1Encodable var4 = (ASN1Encodable)var3.nextElement();
         if (var4 instanceof ASN1Sequence) {
            this.uses = ASN1Sequence.getInstance(var4);
         } else if (var4 instanceof ASN1TaggedObject) {
            this.prohibitions = ASN1Sequence.getInstance((ASN1TaggedObject)var4, false);
         } else if (var4 instanceof DERUTF8String) {
            this.alias = DERUTF8String.getInstance(var4).getString();
         }
      }

   }
}
