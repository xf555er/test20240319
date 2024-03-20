package net.jsign.bouncycastle.asn1.x9;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERSequence;

public class X9FieldID extends ASN1Object implements X9ObjectIdentifiers {
   private ASN1ObjectIdentifier id;
   private ASN1Primitive parameters;

   private X9FieldID(ASN1Sequence var1) {
      this.id = ASN1ObjectIdentifier.getInstance(var1.getObjectAt(0));
      this.parameters = var1.getObjectAt(1).toASN1Primitive();
   }

   public static X9FieldID getInstance(Object var0) {
      if (var0 instanceof X9FieldID) {
         return (X9FieldID)var0;
      } else {
         return var0 != null ? new X9FieldID(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public ASN1ObjectIdentifier getIdentifier() {
      return this.id;
   }

   public ASN1Primitive getParameters() {
      return this.parameters;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(2);
      var1.add(this.id);
      var1.add(this.parameters);
      return new DERSequence(var1);
   }
}
