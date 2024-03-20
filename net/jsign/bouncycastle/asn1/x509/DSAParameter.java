package net.jsign.bouncycastle.asn1.x509;

import java.math.BigInteger;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.DERSequence;

public class DSAParameter extends ASN1Object {
   ASN1Integer p;
   ASN1Integer q;
   ASN1Integer g;

   public DSAParameter(BigInteger var1, BigInteger var2, BigInteger var3) {
      this.p = new ASN1Integer(var1);
      this.q = new ASN1Integer(var2);
      this.g = new ASN1Integer(var3);
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(3);
      var1.add(this.p);
      var1.add(this.q);
      var1.add(this.g);
      return new DERSequence(var1);
   }
}
