package net.jsign.bouncycastle.util.encoders;

import java.io.ByteArrayOutputStream;
import net.jsign.bouncycastle.util.Strings;

public class Hex {
   private static final HexEncoder encoder = new HexEncoder();

   public static String toHexString(byte[] var0) {
      return toHexString(var0, 0, var0.length);
   }

   public static String toHexString(byte[] var0, int var1, int var2) {
      byte[] var3 = encode(var0, var1, var2);
      return Strings.fromByteArray(var3);
   }

   public static byte[] encode(byte[] var0) {
      return encode(var0, 0, var0.length);
   }

   public static byte[] encode(byte[] var0, int var1, int var2) {
      ByteArrayOutputStream var3 = new ByteArrayOutputStream();

      try {
         encoder.encode(var0, var1, var2, var3);
      } catch (Exception var5) {
         throw new EncoderException("exception encoding Hex string: " + var5.getMessage(), var5);
      }

      return var3.toByteArray();
   }

   public static byte[] decode(String var0) {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();

      try {
         encoder.decode(var0, var1);
      } catch (Exception var3) {
         throw new DecoderException("exception decoding Hex string: " + var3.getMessage(), var3);
      }

      return var1.toByteArray();
   }

   public static byte[] decodeStrict(String var0) {
      try {
         return encoder.decodeStrict(var0, 0, var0.length());
      } catch (Exception var2) {
         throw new DecoderException("exception decoding Hex string: " + var2.getMessage(), var2);
      }
   }

   public static byte[] decodeStrict(String var0, int var1, int var2) {
      try {
         return encoder.decodeStrict(var0, var1, var2);
      } catch (Exception var4) {
         throw new DecoderException("exception decoding Hex string: " + var4.getMessage(), var4);
      }
   }
}
