package net.jsign.bouncycastle.pqc.math.linearalgebra;

public final class LittleEndianConversions {
   public static int OS2IP(byte[] var0) {
      return var0[0] & 255 | (var0[1] & 255) << 8 | (var0[2] & 255) << 16 | (var0[3] & 255) << 24;
   }

   public static int OS2IP(byte[] var0, int var1) {
      int var2 = var0[var1++] & 255;
      var2 |= (var0[var1++] & 255) << 8;
      var2 |= (var0[var1++] & 255) << 16;
      var2 |= (var0[var1] & 255) << 24;
      return var2;
   }

   public static int OS2IP(byte[] var0, int var1, int var2) {
      int var3 = 0;

      for(int var4 = var2 - 1; var4 >= 0; --var4) {
         var3 |= (var0[var1 + var4] & 255) << 8 * var4;
      }

      return var3;
   }

   public static byte[] I2OSP(int var0) {
      byte[] var1 = new byte[]{(byte)var0, (byte)(var0 >>> 8), (byte)(var0 >>> 16), (byte)(var0 >>> 24)};
      return var1;
   }

   public static void I2OSP(int var0, byte[] var1, int var2) {
      var1[var2++] = (byte)var0;
      var1[var2++] = (byte)(var0 >>> 8);
      var1[var2++] = (byte)(var0 >>> 16);
      var1[var2++] = (byte)(var0 >>> 24);
   }

   public static void I2OSP(int var0, byte[] var1, int var2, int var3) {
      for(int var4 = var3 - 1; var4 >= 0; --var4) {
         var1[var2 + var4] = (byte)(var0 >>> 8 * var4);
      }

   }
}
