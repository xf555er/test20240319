package net.jsign.bouncycastle.pqc.crypto.lms;

import net.jsign.bouncycastle.crypto.Digest;

class LmsUtils {
   static void u32str(int var0, Digest var1) {
      var1.update((byte)(var0 >>> 24));
      var1.update((byte)(var0 >>> 16));
      var1.update((byte)(var0 >>> 8));
      var1.update((byte)var0);
   }

   static void u16str(short var0, Digest var1) {
      var1.update((byte)(var0 >>> 8));
      var1.update((byte)var0);
   }

   static void byteArray(byte[] var0, Digest var1) {
      var1.update(var0, 0, var0.length);
   }
}
