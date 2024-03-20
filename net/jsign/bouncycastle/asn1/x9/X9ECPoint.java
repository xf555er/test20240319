package net.jsign.bouncycastle.asn1.x9;

import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.DEROctetString;
import net.jsign.bouncycastle.math.ec.ECCurve;
import net.jsign.bouncycastle.util.Arrays;

public class X9ECPoint extends ASN1Object {
   private final ASN1OctetString encoding;
   private ECCurve c;

   public X9ECPoint(ECCurve var1, byte[] var2) {
      this.c = var1;
      this.encoding = new DEROctetString(Arrays.clone(var2));
   }

   public X9ECPoint(ECCurve var1, ASN1OctetString var2) {
      this(var1, var2.getOctets());
   }

   public ASN1Primitive toASN1Primitive() {
      return this.encoding;
   }
}
