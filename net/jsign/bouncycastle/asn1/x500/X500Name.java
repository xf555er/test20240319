package net.jsign.bouncycastle.asn1.x500;

import java.util.Enumeration;
import net.jsign.bouncycastle.asn1.ASN1Choice;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Object;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1TaggedObject;
import net.jsign.bouncycastle.asn1.DERSequence;
import net.jsign.bouncycastle.asn1.x500.style.BCStyle;

public class X500Name extends ASN1Object implements ASN1Choice {
   private static X500NameStyle defaultStyle;
   private boolean isHashCodeCalculated;
   private int hashCodeValue;
   private X500NameStyle style;
   private RDN[] rdns;
   private DERSequence rdnSeq;

   public static X500Name getInstance(ASN1TaggedObject var0, boolean var1) {
      return getInstance(ASN1Sequence.getInstance(var0, true));
   }

   public static X500Name getInstance(Object var0) {
      if (var0 instanceof X500Name) {
         return (X500Name)var0;
      } else {
         return var0 != null ? new X500Name(ASN1Sequence.getInstance(var0)) : null;
      }
   }

   private X500Name(ASN1Sequence var1) {
      this(defaultStyle, var1);
   }

   private X500Name(X500NameStyle var1, ASN1Sequence var2) {
      this.style = var1;
      this.rdns = new RDN[var2.size()];
      boolean var3 = true;
      int var4 = 0;

      RDN var7;
      for(Enumeration var5 = var2.getObjects(); var5.hasMoreElements(); this.rdns[var4++] = var7) {
         Object var6 = var5.nextElement();
         var7 = RDN.getInstance(var6);
         var3 &= var7 == var6;
      }

      if (var3) {
         this.rdnSeq = DERSequence.convert(var2);
      } else {
         this.rdnSeq = new DERSequence(this.rdns);
      }

   }

   public RDN[] getRDNs() {
      return (RDN[])((RDN[])this.rdns.clone());
   }

   public RDN[] getRDNs(ASN1ObjectIdentifier var1) {
      RDN[] var2 = new RDN[this.rdns.length];
      int var3 = 0;

      for(int var4 = 0; var4 != this.rdns.length; ++var4) {
         RDN var5 = this.rdns[var4];
         if (var5.containsAttributeType(var1)) {
            var2[var3++] = var5;
         }
      }

      if (var3 < var2.length) {
         RDN[] var6 = new RDN[var3];
         System.arraycopy(var2, 0, var6, 0, var6.length);
         var2 = var6;
      }

      return var2;
   }

   public ASN1Primitive toASN1Primitive() {
      return this.rdnSeq;
   }

   public int hashCode() {
      if (this.isHashCodeCalculated) {
         return this.hashCodeValue;
      } else {
         this.isHashCodeCalculated = true;
         this.hashCodeValue = this.style.calculateHashCode(this);
         return this.hashCodeValue;
      }
   }

   public boolean equals(Object var1) {
      if (var1 == this) {
         return true;
      } else if (!(var1 instanceof X500Name) && !(var1 instanceof ASN1Sequence)) {
         return false;
      } else {
         ASN1Primitive var2 = ((ASN1Encodable)var1).toASN1Primitive();
         if (this.toASN1Primitive().equals(var2)) {
            return true;
         } else {
            try {
               return this.style.areEqual(this, new X500Name(ASN1Sequence.getInstance(((ASN1Encodable)var1).toASN1Primitive())));
            } catch (Exception var4) {
               return false;
            }
         }
      }
   }

   public String toString() {
      return this.style.toString(this);
   }

   static {
      defaultStyle = BCStyle.INSTANCE;
   }
}
