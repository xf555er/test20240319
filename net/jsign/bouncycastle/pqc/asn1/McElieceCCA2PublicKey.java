package net.jsign.bouncycastle.pqc.asn1;

import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1OctetString;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DEROctetString;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.pqc.math.linearalgebra.GF2Matrix;

public class McElieceCCA2PublicKey extends ASN1Object {
   private final int n;
   private final int t;
   private final GF2Matrix g;
   private final AlgorithmIdentifier digest;

   public McElieceCCA2PublicKey(int var1, int var2, GF2Matrix var3, AlgorithmIdentifier var4) {
      this.n = var1;
      this.t = var2;
      this.g = new GF2Matrix(var3.getEncoded());
      this.digest = var4;
   }

   private McElieceCCA2PublicKey(ASN1Sequence var1) {
      this.n = ((ASN1Integer)var1.getObjectAt(0)).intValueExact();
      this.t = ((ASN1Integer)var1.getObjectAt(1)).intValueExact();
      this.g = new GF2Matrix(((ASN1OctetString)var1.getObjectAt(2)).getOctets());
      this.digest = AlgorithmIdentifier.getInstance(var1.getObjectAt(3));
   }

   public int getN() {
      return this.n;
   }

   public int getT() {
      return this.t;
   }

   public GF2Matrix getG() {
      return this.g;
   }

   public AlgorithmIdentifier getDigest() {
      return this.digest;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector();
      var1.add(new ASN1Integer((long)this.n));
      var1.add(new ASN1Integer((long)this.t));
      var1.add(new DEROctetString(this.g.getEncoded()));
      var1.add(this.digest);
      return new DERSequence(var1);
   }

   public static McElieceCCA2PublicKey getInstance(Object var0) {
      if (var0 instanceof McElieceCCA2PublicKey) {
         return (McElieceCCA2PublicKey)var0;
      } else {
         return var0 != null ? new McElieceCCA2PublicKey(ASN1Sequence.getInstance(var0)) : null;
      }
   }
}
