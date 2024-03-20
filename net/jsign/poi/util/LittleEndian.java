package net.jsign.poi.util;

public final class LittleEndian {
   public static int getInt(byte[] data) {
      return getInt(data, 0);
   }

   public static int getInt(byte[] data, int offset) {
      int i = offset + 1;
      int b0 = data[offset] & 255;
      int b1 = data[i++] & 255;
      int b2 = data[i++] & 255;
      int b3 = data[i] & 255;
      return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
   }

   public static long getLong(byte[] data, int offset) {
      long result = (long)(255 & data[offset + 7]);

      for(int j = offset + 8 - 1; j >= offset; --j) {
         result <<= 8;
         result |= (long)(255 & data[j]);
      }

      return result;
   }

   public static short getShort(byte[] data, int offset) {
      int b0 = data[offset] & 255;
      int b1 = data[offset + 1] & 255;
      return (short)((b1 << 8) + b0);
   }

   public static void putInt(byte[] data, int offset, int value) {
      int i = offset + 1;
      data[offset] = (byte)(value & 255);
      data[i++] = (byte)(value >>> 8 & 255);
      data[i++] = (byte)(value >>> 16 & 255);
      data[i] = (byte)(value >>> 24 & 255);
   }

   public static void putLong(byte[] data, int offset, long value) {
      data[offset] = (byte)((int)(value & 255L));
      data[offset + 1] = (byte)((int)(value >>> 8 & 255L));
      data[offset + 2] = (byte)((int)(value >>> 16 & 255L));
      data[offset + 3] = (byte)((int)(value >>> 24 & 255L));
      data[offset + 4] = (byte)((int)(value >>> 32 & 255L));
      data[offset + 5] = (byte)((int)(value >>> 40 & 255L));
      data[offset + 6] = (byte)((int)(value >>> 48 & 255L));
      data[offset + 7] = (byte)((int)(value >>> 56 & 255L));
   }

   public static void putShort(byte[] data, int offset, short value) {
      int i = offset + 1;
      data[offset] = (byte)(value & 255);
      data[i] = (byte)(value >>> 8 & 255);
   }
}
