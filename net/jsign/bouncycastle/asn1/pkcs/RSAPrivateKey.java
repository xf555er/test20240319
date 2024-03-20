package net.jsign.bouncycastle.asn1.pkcs;

import java.math.BigInteger;
import java.util.Enumeration;
import net.jsign.bouncycastle.asn1.ASN1EncodableVector;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERSequence;

public class RSAPrivateKey extends ASN1Object {
   private BigInteger version;
   private BigInteger modulus;
   private BigInteger publicExponent;
   private BigInteger privateExponent;
   private BigInteger prime1;
   private BigInteger prime2;
   private BigInteger exponent1;
   private BigInteger exponent2;
   private BigInteger coefficient;
   private ASN1Sequence otherPrimeInfos = null;

   public static RSAPrivateKey getInstance(Object var0) {
      if (var0 instanceof RSAPrivateKey) {
         return (RSAPrivateKey)var0;
      } else {
         return var0 != null ? new RSAPrivateKey(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   private RSAPrivateKey(ASN1Sequence var1) {
      Enumeration var2 = var1.getObjects();
      ASN1Integer var3 = (ASN1Integer)var2.nextElement();
      int var4 = var3.intValueExact();
      if (var4 >= 0 && var4 <= 1) {
         this.version = var3.getValue();
         this.modulus = ((ASN1Integer)var2.nextElement()).getValue();
         this.publicExponent = ((ASN1Integer)var2.nextElement()).getValue();
         this.privateExponent = ((ASN1Integer)var2.nextElement()).getValue();
         this.prime1 = ((ASN1Integer)var2.nextElement()).getValue();
         this.prime2 = ((ASN1Integer)var2.nextElement()).getValue();
         this.exponent1 = ((ASN1Integer)var2.nextElement()).getValue();
         this.exponent2 = ((ASN1Integer)var2.nextElement()).getValue();
         this.coefficient = ((ASN1Integer)var2.nextElement()).getValue();
         if (var2.hasMoreElements()) {
            this.otherPrimeInfos = (ASN1Sequence)var2.nextElement();
         }

      } else {
         throw new IllegalArgumentException("wrong version for RSA private key");
      }
   }

   public BigInteger getModulus() {
      return this.modulus;
   }

   public BigInteger getPublicExponent() {
      return this.publicExponent;
   }

   public BigInteger getPrivateExponent() {
      return this.privateExponent;
   }

   public BigInteger getPrime1() {
      return this.prime1;
   }

   public BigInteger getPrime2() {
      return this.prime2;
   }

   public BigInteger getExponent1() {
      return this.exponent1;
   }

   public BigInteger getExponent2() {
      return this.exponent2;
   }

   public BigInteger getCoefficient() {
      return this.coefficient;
   }

   public ASN1Primitive toASN1Primitive() {
      ASN1EncodableVector var1 = new ASN1EncodableVector(10);
      var1.add(new ASN1Integer(this.version));
      var1.add(new ASN1Integer(this.getModulus()));
      var1.add(new ASN1Integer(this.getPublicExponent()));
      var1.add(new ASN1Integer(this.getPrivateExponent()));
      var1.add(new ASN1Integer(this.getPrime1()));
      var1.add(new ASN1Integer(this.getPrime2()));
      var1.add(new ASN1Integer(this.getExponent1()));
      var1.add(new ASN1Integer(this.getExponent2()));
      var1.add(new ASN1Integer(this.getCoefficient()));
      if (this.otherPrimeInfos != null) {
         var1.add(this.otherPrimeInfos);
      }

      return new DERSequence(var1);
   }
}
