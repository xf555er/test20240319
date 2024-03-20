package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public final class PCLByteWriterUtil {
   private PCLByteWriterUtil() {
   }

   public static byte[] padBytes(byte[] in, int length) {
      return padBytes(in, length, 0);
   }

   public static byte[] padBytes(byte[] in, int length, int value) {
      byte[] out = new byte[length];

      for(int i = 0; i < length; ++i) {
         if (i < in.length) {
            out[i] = in[i];
         } else {
            out[i] = (byte)value;
         }
      }

      return out;
   }

   public static byte[] signedInt(int s) {
      byte b1 = (byte)(s >> 8);
      byte b2 = (byte)s;
      return new byte[]{b1, b2};
   }

   public static byte signedByte(int s) {
      return (byte)s;
   }

   public static byte[] unsignedLongInt(int s) {
      return unsignedLongInt((long)s);
   }

   public static byte[] unsignedLongInt(long s) {
      byte b1 = (byte)((int)(s >> 24 & 255L));
      byte b2 = (byte)((int)(s >> 16 & 255L));
      byte b3 = (byte)((int)(s >> 8 & 255L));
      byte b4 = (byte)((int)(s & 255L));
      return new byte[]{b1, b2, b3, b4};
   }

   public static byte[] unsignedInt(int s) {
      byte b1 = (byte)(s >> 8 & 255);
      byte b2 = (byte)(s & 255);
      return new byte[]{b1, b2};
   }

   public static int unsignedByte(int b) {
      return (byte)b & 255;
   }

   public static int maxPower2(int value) {
      int test;
      for(test = 2; test < value; test *= 2) {
      }

      return test;
   }

   public static int log(int x, int base) {
      return (int)(Math.log((double)x) / Math.log((double)base));
   }

   public static byte[] toByteArray(int[] s) {
      byte[] values = new byte[s.length];

      for(int i = 0; i < s.length; ++i) {
         values[i] = (byte)s[i];
      }

      return values;
   }

   public static byte[] insertIntoArray(int index, byte[] insertTo, byte[] data) throws IOException {
      byte[] preBytes = Arrays.copyOf(insertTo, index);
      byte[] postBytes = Arrays.copyOfRange(insertTo, index, insertTo.length);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      baos.write(preBytes);
      baos.write(data);
      baos.write(postBytes);
      return baos.toByteArray();
   }

   public static byte[] updateDataAtLocation(byte[] data, byte[] update, int offset) {
      int count = 0;

      for(int i = offset; i < offset + update.length; ++i) {
         data[i] = update[count++];
      }

      return data;
   }

   public static byte[] writeCommand(String cmd) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      baos.write(27);
      baos.write(cmd.getBytes("US-ASCII"));
      return baos.toByteArray();
   }
}
