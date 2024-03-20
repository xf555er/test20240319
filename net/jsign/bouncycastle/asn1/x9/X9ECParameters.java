package net.jsign.bouncycastle.asn1.x9;

import java.math.BigInteger;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.math.ec.ECCurve;

public class X9ECParameters extends ASN1Object implements X9ObjectIdentifiers {
   private static final BigInteger ONE = BigInteger.valueOf(1L);
   private X9FieldID fieldID;
   private ECCurve curve;
   private X9ECPoint g;
   private BigInteger n;
   private BigInteger h;
   private byte[] seed;

   private X9ECParameters(ASN1Sequence var1) {
      if (var1.getObjectAt(0) instanceof ASN1Integer && ((ASN1Integer)var1.getObjectAt(0)).hasValue(1)) {
         this.n = ((ASN1Integer)var1.getObjectAt(4)).getValue();
         if (var1.size() == 6) {
            this.h = ((ASN1Integer)var1.getObjectAt(5)).getValue();
         }

         X9Curve var2 = new X9Curve(X9FieldID.getInstance(var1.getObjectAt(1)), this.n, this.h, ASN1Sequence.getInstance(var1.getObjectAt(2)));
         this.curve = var2.getCurve();
         ASN1Encodable var3 = var1.getObjectAt(3);
         if (var3 instanceof X9ECPoint) {
            this.g = (X9ECPoint)var3;
         } else {
            this.g = new X9ECPoint(this.curve, (ASN1OctetString)var3);
         }

         this.seed = var2.getSeed();
      } else {
         throw new IllegalArgumentException("bad version in X9ECParameters");
      }
   }

   public static X9ECParameters getInstance(Object var0) {
      if (var0 instanceof X9ECParameters) {
         return (X9ECParameters)var0;
      } else {
         return var0 != null ? new X9ECParameters(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(6);
      var1.add(new ASN1Integer(ONE));
      var1.add(this.fieldID);
      var1.add(new X9Curve(this.curve, this.seed));
      var1.add(this.g);
      var1.add(new ASN1Integer(this.n));
      if (this.h != null) {
         var1.add(new ASN1Integer(this.h));
      }

      return new DERSequence(var1);
   }
}
