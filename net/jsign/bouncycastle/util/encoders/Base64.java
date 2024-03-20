package net.jsign.bouncycastle.util.encoders;

import java.io.ByteArrayOutputStream;

public class Base64 {
   private static final Encoder encoder = new Base64Encoder();

   public static byte[] encode(byte[] var0) {
      return encode(var0, 0, var0.length);
   }

   public static byte[] encode(byte[] var0, int var1, int var2) {
      int var3 = encoder.getEncodedLength(var2);
      ByteArrayOutputStream var4 = new ByteArrayOutputStream(var3);

      try {
         encoder.encode(var0, var1, var2, var4);
      } catch (Exception var6) {
         throw new EncoderException("exception encoding base64 string: " + var6.getMessage(), var6);
      }

      return var4.toByteArray();
   }

   public static byte[] decode(byte[] var0) {
      int var1 = var0.length / 4 * 3;
      ByteArrayOutputStream var2 = new ByteArrayOutputStream(var1);

      try {
         encoder.decode(var0, 0, var0.length, var2);
      } catch (Exception var4) {
         throw new DecoderException("unable to decode base64 data: " + var4.getMessage(), var4);
      }

      return var2.toByteArray();
   }

   public static byte[] decode(String var0) {
      int var1 = var0.length() / 4 * 3;
      ByteArrayOutputStream var2 = new ByteArrayOutputStream(var1);

      try {
         encoder.decode(var0, var2);
      } catch (Exception var4) {
         throw new DecoderException("unable to decode base64 string: " + var4.getMessage(), var4);
      }

      return var2.toByteArray();
   }
}
