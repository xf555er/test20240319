package net.jsign.bouncycastle.asn1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.io.Streams;

public abstract class ASN1BitString extends ASN1Primitive implements ASN1String {
   private static final char[] table = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
   protected final byte[] data;
   protected final int padBits;

   public ASN1BitString(byte[] var1, int var2) {
      if (var1 == null) {
         throw new NullPointerException("'data' cannot be null");
      } else if (var1.length == 0 && var2 != 0) {
         throw new IllegalArgumentException("zero length data with non-zero pad bits");
      } else if (var2 <= 7 && var2 >= 0) {
         this.data = Arrays.clone(var1);
         this.padBits = var2;
      } else {
         throw new IllegalArgumentException("pad bits cannot be greater than 7 or less than 0");
      }
   }

   public String getString() {
      StringBuffer var1 = new StringBuffer("#");

      byte[] var2;
      try {
         var2 = this.getEncoded();
      } catch (IOException var4) {
         throw new ASN1ParsingException("Internal error encoding BitString: " + var4.getMessage(), var4);
      }

      for(int var3 = 0; var3 != var2.length; ++var3) {
         var1.append(table[var2[var3] >>> 4 & 15]);
         var1.append(table[var2[var3] & 15]);
      }

      return var1.toString();
   }

   public byte[] getOctets() {
      if (this.padBits != 0) {
         throw new IllegalStateException("attempt to get non-octet aligned data from BIT STRING");
      } else {
         return Arrays.clone(this.data);
      }
   }

   public byte[] getBytes() {
      if (0 == this.data.length) {
         return this.data;
      } else {
         byte[] var1 = Arrays.clone(this.data);
         int var10001 = this.data.length - 1;
         var1[var10001] = (byte)(var1[var10001] & 255 << this.padBits);
         return var1;
      }
   }

   public int getPadBits() {
      return this.padBits;
   }

   public String toString() {
      return this.getString();
   }

   public int hashCode() {
      int var1 = this.data.length;
      --var1;
      if (var1 < 0) {
         return 1;
      } else {
         byte var2 = (byte)(this.data[var1] & 255 << this.padBits);
         int var3 = Arrays.hashCode(this.data, 0, var1);
         var3 *= 257;
         var3 ^= var2;
         return var3 ^ this.padBits;
      }
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1BitString)) {
         return false;
      } else {
         ASN1BitString var2 = (ASN1BitString)var1;
         if (this.padBits != var2.padBits) {
            return false;
         } else {
            byte[] var3 = this.data;
            byte[] var4 = var2.data;
            int var5 = var3.length;
            if (var5 != var4.length) {
               return false;
            } else {
               --var5;
               if (var5 < 0) {
                  return true;
               } else {
                  for(int var6 = 0; var6 < var5; ++var6) {
                     if (var3[var6] != var4[var6]) {
                        return false;
                     }
                  }

                  byte var8 = (byte)(var3[var5] & 255 << this.padBits);
                  byte var7 = (byte)(var4[var5] & 255 << this.padBits);
                  return var8 == var7;
               }
            }
         }
      }
   }

   static ASN1BitString fromInputStream(int var0, InputStream var1) throws IOException {
      if (var0 < 1) {
         throw new IllegalArgumentException("truncated BIT STRING detected");
      } else {
         int var2 = var1.read();
         byte[] var3 = new byte[var0 - 1];
         if (var3.length != 0) {
            if (Streams.readFully(var1, var3) != var3.length) {
               throw new EOFException("EOF encountered in middle of BIT STRING");
            }

            if (var2 > 0 && var2 < 8 && var3[var3.length - 1] != (byte)(var3[var3.length - 1] & 255 << var2)) {
               return new DLBitString(var3, var2);
            }
         }

         return new DERBitString(var3, var2);
      }
   }

   ASN1Primitive toDERObject() {
      return new DERBitString(this.data, this.padBits);
   }

   ASN1Primitive toDLObject() {
      return new DLBitString(this.data, this.padBits);
   }

   abstract void encode(ASN1OutputStream var1, boolean var2) throws IOException;
}
