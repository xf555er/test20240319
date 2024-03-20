package net.jsign.bouncycastle.asn1;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

public class ASN1OutputStream {
   private OutputStream os;

   public static ASN1OutputStream create(OutputStream var0) {
      return new ASN1OutputStream(var0);
   }

   public static ASN1OutputStream create(OutputStream var0, String var1) {
      if (var1.equals("DER")) {
         return new DEROutputStream(var0);
      } else {
         return (ASN1OutputStream)(var1.equals("DL") ? new DLOutputStream(var0) : new ASN1OutputStream(var0));
      }
   }

   /** @deprecated */
   public ASN1OutputStream(OutputStream var1) {
      this.os = var1;
   }

   final void writeLength(int var1) throws IOException {
      if (var1 > 127) {
         int var2 = 1;

         for(int var3 = var1; (var3 >>>= 8) != 0; ++var2) {
         }

         this.write((byte)(var2 | 128));

         for(int var4 = (var2 - 1) * 8; var4 >= 0; var4 -= 8) {
            this.write((byte)(var1 >> var4));
         }
      } else {
         this.write((byte)var1);
      }

   }

   final void write(int var1) throws IOException {
      this.os.write(var1);
   }

   final void write(byte[] var1, int var2, int var3) throws IOException {
      this.os.write(var1, var2, var3);
   }

   final void writeElements(ASN1Encodable[] var1) throws IOException {
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         ASN1Primitive var4 = var1[var3].toASN1Primitive();
         this.writePrimitive(var4, true);
      }

   }

   final void writeElements(Enumeration var1) throws IOException {
      while(var1.hasMoreElements()) {
         ASN1Primitive var2 = ((ASN1Encodable)var1.nextElement()).toASN1Primitive();
         this.writePrimitive(var2, true);
      }

   }

   final void writeEncoded(boolean var1, int var2, byte var3) throws IOException {
      if (var1) {
         this.write(var2);
      }

      this.writeLength(1);
      this.write(var3);
   }

   final void writeEncoded(boolean var1, int var2, byte[] var3) throws IOException {
      if (var1) {
         this.write(var2);
      }

      this.writeLength(var3.length);
      this.write(var3, 0, var3.length);
   }

   final void writeEncoded(boolean var1, int var2, byte var3, byte[] var4) throws IOException {
      if (var1) {
         this.write(var2);
      }

      this.writeLength(1 + var4.length);
      this.write(var3);
      this.write(var4, 0, var4.length);
   }

   final void writeEncoded(boolean var1, int var2, byte var3, byte[] var4, int var5, int var6, byte var7) throws IOException {
      if (var1) {
         this.write(var2);
      }

      this.writeLength(2 + var6);
      this.write(var3);
      this.write(var4, var5, var6);
      this.write(var7);
   }

   final void writeEncoded(boolean var1, int var2, int var3, byte[] var4) throws IOException {
      this.writeTag(var1, var2, var3);
      this.writeLength(var4.length);
      this.write(var4, 0, var4.length);
   }

   final void writeEncodedIndef(boolean var1, int var2, int var3, byte[] var4) throws IOException {
      this.writeTag(var1, var2, var3);
      this.write(128);
      this.write(var4, 0, var4.length);
      this.write(0);
      this.write(0);
   }

   final void writeEncodedIndef(boolean var1, int var2, ASN1Encodable[] var3) throws IOException {
      if (var1) {
         this.write(var2);
      }

      this.write(128);
      this.writeElements(var3);
      this.write(0);
      this.write(0);
   }

   final void writeEncodedIndef(boolean var1, int var2, Enumeration var3) throws IOException {
      if (var1) {
         this.write(var2);
      }

      this.write(128);
      this.writeElements(var3);
      this.write(0);
      this.write(0);
   }

   final void writeTag(boolean var1, int var2, int var3) throws IOException {
      if (var1) {
         if (var3 < 31) {
            this.write(var2 | var3);
         } else {
            this.write(var2 | 31);
            if (var3 < 128) {
               this.write(var3);
            } else {
               byte[] var4 = new byte[5];
               int var5 = var4.length;
               --var5;
               var4[var5] = (byte)(var3 & 127);

               do {
                  var3 >>= 7;
                  --var5;
                  var4[var5] = (byte)(var3 & 127 | 128);
               } while(var3 > 127);

               this.write(var4, var5, var4.length - var5);
            }
         }

      }
   }

   public void writeObject(ASN1Encodable var1) throws IOException {
      if (null == var1) {
         throw new IOException("null object detected");
      } else {
         this.writePrimitive(var1.toASN1Primitive(), true);
         this.flushInternal();
      }
   }

   public void writeObject(ASN1Primitive var1) throws IOException {
      if (null == var1) {
         throw new IOException("null object detected");
      } else {
         this.writePrimitive(var1, true);
         this.flushInternal();
      }
   }

   void writePrimitive(ASN1Primitive var1, boolean var2) throws IOException {
      var1.encode(this, var2);
   }

   void flushInternal() throws IOException {
   }

   DEROutputStream getDERSubStream() {
      return new DEROutputStream(this.os);
   }

   ASN1OutputStream getDLSubStream() {
      return new DLOutputStream(this.os);
   }
}
