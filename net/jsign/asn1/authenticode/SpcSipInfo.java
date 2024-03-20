package net.jsign.asn1.authenticode;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.BERSequence;

public class SpcSipInfo extends ASN1Object {
   private int version;
   private SpcUuid uuid;
   private int reserved1;
   private int reserved2;
   private int reserved3;
   private int reserved4;
   private int reserved5;

   public SpcSipInfo(int version, SpcUuid uuid) {
      this.version = version;
      this.uuid = uuid;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector v = new ASN1EncodableVector();
      v.add(new ASN1Integer((long)this.version));
      v.add(this.uuid);
      v.add(new ASN1Integer((long)this.reserved1));
      v.add(new ASN1Integer((long)this.reserved2));
      v.add(new ASN1Integer((long)this.reserved3));
      v.add(new ASN1Integer((long)this.reserved4));
      v.add(new ASN1Integer((long)this.reserved5));
      return new BERSequence(v);
   }
}
