package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;

public class ASN1Enumerated extends ASN1Primitive {
   private final byte[] bytes;
   private final int start;
   private static ASN1Enumerated[] cache = new ASN1Enumerated[12];

   public static ASN1Enumerated getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1Enumerated)) {
         if (var0 instanceof byte[]) {
            try {
               return (ASN1Enumerated)fromByteArray((byte[])((byte[])var0));
            } catch (Exception var2) {
               throw new IllegalArgumentException("encoding error in getInstance: " + var2.toString());
            }
         } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1Enumerated)var0;
      }
   }

   public ASN1Enumerated(byte[] var1) {
      if (ASN1Integer.isMalformed(var1)) {
         throw new IllegalArgumentException("malformed enumerated");
      } else if (0 != (var1[0] & 128)) {
         throw new IllegalArgumentException("enumerated must be non-negative");
      } else {
         this.bytes = Arrays.clone(var1);
         this.start = ASN1Integer.signBytesToSkip(var1);
      }
   }

   public int intValueExact() {
      int var1 = this.bytes.length - this.start;
      if (var1 > 4) {
         throw new ArithmeticException("ASN.1 Enumerated out of int range");
      } else {
         return ASN1Integer.intValue(this.bytes, this.start, -1);
      }
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 1 + StreamUtil.calculateBodyLength(this.bytes.length) + this.bytes.length;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 10, this.bytes);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1Enumerated)) {
         return false;
      } else {
         ASN1Enumerated var2 = (ASN1Enumerated)var1;
         return Arrays.areEqual(this.bytes, var2.bytes);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }

   static ASN1Enumerated fromOctetString(byte[] var0) {
      if (var0.length > 1) {
         return new ASN1Enumerated(var0);
      } else if (var0.length == 0) {
         throw new IllegalArgumentException("ENUMERATED has zero length");
      } else {
         int var1 = var0[0] & 255;
         if (var1 >= cache.length) {
            return new ASN1Enumerated(var0);
         } else {
            ASN1Enumerated var2 = cache[var1];
            if (var2 == null) {
               var2 = cache[var1] = new ASN1Enumerated(var0);
            }

            return var2;
         }
      }
   }
}
