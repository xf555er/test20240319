package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import net.jsign.bouncycastle.util.Arrays;

public class DERBMPString extends ASN1Primitive implements ASN1String {
   private final char[] string;

   DERBMPString(char[] var1) {
      if (var1 == null) {
         throw new NullPointerException("'string' cannot be null");
      } else {
         this.string = var1;
      }
   }

   public DERBMPString(String var1) {
      if (var1 == null) {
         throw new NullPointerException("'string' cannot be null");
      } else {
         this.string = var1.toCharArray();
      }
   }

   public String getString() {
      return new String(this.string);
   }

   public String toString() {
      return this.getString();
   }

   public int hashCode() {
      return Arrays.hashCode(this.string);
   }

   protected boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof DERBMPString)) {
         return false;
      } else {
         DERBMPString var2 = (DERBMPString)var1;
         return Arrays.areEqual(this.string, var2.string);
      }
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 1 + StreamUtil.calculateBodyLength(this.string.length * 2) + this.string.length * 2;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      int var3 = this.string.length;
      if (var2) {
         var1.write(30);
      }

      var1.writeLength(var3 * 2);
      byte[] var4 = new byte[8];
      int var5 = 0;
      int var6 = var3 & -4;

      int var7;
      char var8;
      while(var5 < var6) {
         var7 = this.string[var5];
         var8 = this.string[var5 + 1];
         char var9 = this.string[var5 + 2];
         char var10 = this.string[var5 + 3];
         var5 += 4;
         var4[0] = (byte)(var7 >> 8);
         var4[1] = (byte)var7;
         var4[2] = (byte)(var8 >> 8);
         var4[3] = (byte)var8;
         var4[4] = (byte)(var9 >> 8);
         var4[5] = (byte)var9;
         var4[6] = (byte)(var10 >> 8);
         var4[7] = (byte)var10;
         var1.write(var4, 0, 8);
      }

      if (var5 < var3) {
         var7 = 0;

         do {
            var8 = this.string[var5];
            ++var5;
            var4[var7++] = (byte)(var8 >> 8);
            var4[var7++] = (byte)var8;
         } while(var5 < var3);

         var1.write(var4, 0, var7);
      }

   }
}
