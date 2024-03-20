package net.jsign.bouncycastle.asn1.tsp;

import net.jsign.bouncycastle.asn1.ASN1Boolean;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.DERTaggedObject;
import net.jsign.bouncycastle.asn1.x509.Extensions;

public class TimeStampReq extends ASN1Object {
   ASN1Integer version = new ASN1Integer(1L);
   MessageImprint messageImprint;
   ASN1ObjectIdentifier tsaPolicy;
   ASN1Integer nonce;
   ASN1Boolean certReq;
   Extensions extensions;

   public TimeStampReq(MessageImprint var1, ASN1ObjectIdentifier var2, ASN1Integer var3, ASN1Boolean var4, Extensions var5) {
      this.messageImprint = var1;
      this.tsaPolicy = var2;
      this.nonce = var3;
      this.certReq = var4;
      this.extensions = var5;
   }

   public MessageImprint getMessageImprint() {
      return this.messageImprint;
   }

   public ASN1ObjectIdentifier getReqPolicy() {
      return this.tsaPolicy;
   }

   public ASN1Integer getNonce() {
      return this.nonce;
   }

   public Extensions getExtensions() {
      return this.extensions;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(6);
      var1.add(this.version);
      var1.add(this.messageImprint);
      if (this.tsaPolicy != null) {
         var1.add(this.tsaPolicy);
      }

      if (this.nonce != null) {
         var1.add(this.nonce);
      }

      if (this.certReq != null && this.certReq.isTrue()) {
         var1.add(this.certReq);
      }

      if (this.extensions != null) {
         var1.add(new DERTaggedObject(false, 0, this.extensions));
      }

      return new DERSequence(var1);
   }
}
