package net.jsign.bouncycastle.util;

public abstract class Pack {
   public static int bigEndianToInt(byte[] var0, int var1) {
      int var2 = var0[var1] << 24;
      ++var1;
      var2 |= (var0[var1] & 255) << 16;
      ++var1;
      var2 |= (var0[var1] & 255) << 8;
      ++var1;
      var2 |= var0[var1] & 255;
      return var2;
   }

   public static void intToBigEndian(int var0, byte[] var1, int var2) {
      var1[var2] = (byte)(var0 >>> 24);
      ++var2;
      var1[var2] = (byte)(var0 >>> 16);
      ++var2;
      var1[var2] = (byte)(var0 >>> 8);
      ++var2;
      var1[var2] = (byte)var0;
   }

   public static long bigEndianToLong(byte[] var0, int var1) {
      int var2 = bigEndianToInt(var0, var1);
      int var3 = bigEndianToInt(var0, var1 + 4);
      return ((long)var2 & 4294967295L) << 32 | (long)var3 & 4294967295L;
   }

   public static void longToBigEndian(long var0, byte[] var2, int var3) {
      intToBigEndian((int)(var0 >>> 32), var2, var3);
      intToBigEndian((int)(var0 & 4294967295L), var2, var3 + 4);
   }

   public static short littleEndianToShort(byte[] var0, int var1) {
      int var2 = var0[var1] & 255;
      ++var1;
      var2 |= (var0[var1] & 255) << 8;
      return (short)var2;
   }

   public static int littleEndianToInt(byte[] var0, int var1) {
      int var2 = var0[var1] & 255;
      ++var1;
      var2 |= (var0[var1] & 255) << 8;
      ++var1;
      var2 |= (var0[var1] & 255) << 16;
      ++var1;
      var2 |= var0[var1] << 24;
      return var2;
   }

   public static void shortToLittleEndian(short var0, byte[] var1, int var2) {
      var1[var2] = (byte)var0;
      ++var2;
      var1[var2] = (byte)(var0 >>> 8);
   }

   public static void shortToBigEndian(short var0, byte[] var1, int var2) {
      var1[var2] = (byte)(var0 >>> 8);
      ++var2;
      var1[var2] = (byte)var0;
   }

   public static void intToLittleEndian(int var0, byte[] var1, int var2) {
      var1[var2] = (byte)var0;
      ++var2;
      var1[var2] = (byte)(var0 >>> 8);
      ++var2;
      var1[var2] = (byte)(var0 >>> 16);
      ++var2;
      var1[var2] = (byte)(var0 >>> 24);
   }

   public static long littleEndianToLong(byte[] var0, int var1) {
      int var2 = littleEndianToInt(var0, var1);
      int var3 = littleEndianToInt(var0, var1 + 4);
      return ((long)var3 & 4294967295L) << 32 | (long)var2 & 4294967295L;
   }

   public static void longToLittleEndian(long var0, byte[] var2, int var3) {
      intToLittleEndian((int)(var0 & 4294967295L), var2, var3);
      intToLittleEndian((int)(var0 >>> 32), var2, var3 + 4);
   }

   public static void longToLittleEndian(long[] var0, int var1, int var2, byte[] var3, int var4) {
      for(int var5 = 0; var5 < var2; ++var5) {
         longToLittleEndian(var0[var1 + var5], var3, var4);
         var4 += 8;
      }

   }
}
