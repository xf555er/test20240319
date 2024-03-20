package net.jsign.bouncycastle.asn1;

import java.io.IOException;

public class ASN1Boolean extends ASN1Primitive {
   public static final ASN1Boolean FALSE = new ASN1Boolean((byte)0);
   public static final ASN1Boolean TRUE = new ASN1Boolean((byte)-1);
   private final byte value;

   public static ASN1Boolean getInstance(Object var0) {
      if (var0 != null && !(var0 instanceof ASN1Boolean)) {
         if (var0 instanceof byte[]) {
            byte[] var1 = (byte[])((byte[])var0);

            try {
               return (ASN1Boolean)fromByteArray(var1);
            } catch (IOException var3) {
               throw new IllegalArgumentException("failed to construct boolean from byte[]: " + var3.getMessage());
            }
         } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + var0.getClass().getName());
         }
      } else {
         return (ASN1Boolean)var0;
      }
   }

   public static ASN1Boolean getInstance(boolean var0) {
      return var0 ? TRUE : FALSE;
   }

   public static ASN1Boolean getInstance(ASN1TaggedObject var0, boolean var1) {
      ASN1Primitive var2 = var0.getObject();
      return !var1 && !(var2 instanceof ASN1Boolean) ? fromOctetString(ASN1OctetString.getInstance(var2).getOctets()) : getInstance(var2);
   }

   private ASN1Boolean(byte var1) {
      this.value = var1;
   }

   public boolean isTrue() {
      return this.value != 0;
   }

   boolean isConstructed() {
      return false;
   }

   int encodedLength() {
      return 3;
   }

   void encode(ASN1OutputStream var1, boolean var2) throws IOException {
      var1.writeEncoded(var2, 1, this.value);
   }

   boolean asn1Equals(ASN1Primitive var1) {
      if (!(var1 instanceof ASN1Boolean)) {
         return false;
      } else {
         ASN1Boolean var2 = (ASN1Boolean)var1;
         return this.isTrue() == var2.isTrue();
      }
   }

   public int hashCode() {
      return this.isTrue() ? 1 : 0;
   }

   ASN1Primitive toDERObject() {
      return this.isTrue() ? TRUE : FALSE;
   }

   public String toString() {
      return this.isTrue() ? "TRUE" : "FALSE";
   }

   static ASN1Boolean fromOctetString(byte[] var0) {
      if (var0.length != 1) {
         throw new IllegalArgumentException("BOOLEAN value should have 1 byte in it");
      } else {
         byte var1 = var0[0];
         switch (var1) {
            case -1:
               return TRUE;
            case 0:
               return FALSE;
            default:
               return new ASN1Boolean(var1);
         }
      }
   }
}
