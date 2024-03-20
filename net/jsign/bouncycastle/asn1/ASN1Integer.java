package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.math.BigInteger;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Properties;

public class ASN1Integer extends ASN1Primitive {
   private final byte[] bytes;
   private final int start;

   public static ASN1Integer getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1Integer)) {
         if (var0 instanceof byte[]) {
            try {
               return (ASN1Integer)fromByteArray((byte[])((byte[])var0));
            } catch (Exception var2) {
               throw new IllegalArgumentException("encoding error in getInstance: " + var2.toString());
            }
         } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1Integer)var0;
      }
   }

   public static ASN1Integer getInstance(ASN1TaggedObject var0, boolean var1) {
      ASN1Primitive var2 = var0.getObject();
      return !var1 && !(var2 instanceof ASN1Integer) ? new ASN1Integer(ASN1OctetString.getInstance(var2).getOctets()) : getInstance(var2);
   }

   public ASN1Integer(long var1) {
      this.bytes = BigInteger.valueOf(var1).toByteArray();
      this.start = 0;
   }

   public ASN1Integer(BigInteger var1) {
      this.bytes = var1.toByteArray();
      this.start = 0;
   }

   public ASN1Integer(byte[] var1) {
      this(var1, true);
   }

   ASN1Integer(byte[] var1, boolean var2) {
      if (isMalformed(var1)) {
         throw new IllegalArgumentException("malformed integer");
      } else {
         this.bytes = var2 ? Arrays.clone(var1) : var1;
         this.start = signBytesToSkip(var1);
      }
   }

   public BigInteger getPositiveValue() {
      return new BigInteger(1, this.bytes);
   }

   public BigInteger getValue() {
      return new BigInteger(this.bytes);
   }

   public boolean hasValue(int var1) {
      return this.bytes.length - this.start <= 4 && intValue(this.bytes, this.start, -1) == var1;
   }

   public boolean hasValue(BigInteger var1) {
      return null != var1 && intValue(this.bytes, this.start, -1) == var1.intValue() && this.getValue().equals(var1);
   }

   public int intValueExact() {
      int var1 = this.bytes.length - this.start;
      if (var1 > 4) {
         throw new ArithmeticException("ASN.1 Integer out of int range");
      } else {
         return intValue(this.bytes, this.start, -1);
      }
   }

   public long longValueExact() {
      int var1 = this.bytes.length - this.start;
      if (var1 > 8) {
         throw new ArithmeticException("ASN.1 Integer out of long range");
      } else {
         return longValue(this.bytes, this.start, -1);
      }
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 1 + StreamUtil.calculateBodyLength(this.bytes.length) + this.bytes.length;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 2, this.bytes);
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1Integer)) {
         return false;
      } else {
         ASN1Integer var2 = (ASN1Integer)var1;
         return Arrays.areEqual(this.bytes, var2.bytes);
      }
   }

   public String toString() {
      return this.getValue().toString();
   }

   static int intValue(byte[] var0, int var1, int var2) {
      int var3 = var0.length;
      int var4 = Math.max(var1, var3 - 4);
      int var5 = var0[var4] & var2;

      while(true) {
         ++var4;
         if (var4 >= var3) {
            return var5;
         }

         var5 = var5 << 8 | var0[var4] & 255;
      }
   }

   static long longValue(byte[] var0, int var1, int var2) {
      int var3 = var0.length;
      int var4 = Math.max(var1, var3 - 8);
      long var5 = (long)(var0[var4] & var2);

      while(true) {
         ++var4;
         if (var4 >= var3) {
            return var5;
         }

         var5 = var5 << 8 | (long)(var0[var4] & 255);
      }
   }

   static boolean isMalformed(byte[] var0) {
      switch (var0.length) {
         case 0:
            return true;
         case 1:
            return false;
         default:
            return var0[0] == var0[1] >> 7 && !Properties.isOverrideSet("net.jsign.bouncycastle.asn1.allow_unsafe_integer");
      }
   }

   static int signBytesToSkip(byte[] var0) {
      int var1 = 0;

      for(int var2 = var0.length - 1; var1 < var2 && var0[var1] == var0[var1 + 1] >> 7; ++var1) {
      }

      return var1;
   }
}
