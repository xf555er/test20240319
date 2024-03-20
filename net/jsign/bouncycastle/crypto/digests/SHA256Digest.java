package net.jsign.bouncycastle.crypto.digests;

import net.jsign.bouncycastle.util.Pack;

public class SHA256Digest extends GeneralDigest {
   private int H1;
   private int H2;
   private int H3;
   private int H4;
   private int H5;
   private int H6;
   private int H7;
   private int H8;
   private int[] X = new int[64];
   private int xOff;
   static final int[] K = new int[]{1116352408, 1899447441, -1245643825, -373957723, 961987163, 1508970993, -1841331548, -1424204075, -670586216, 310598401, 607225278, 1426881987, 1925078388, -2132889090, -1680079193, -1046744716, -459576895, -272742522, 264347078, 604807628, 770255983, 1249150122, 1555081692, 1996064986, -1740746414, -1473132947, -1341970488, -1084653625, -958395405, -710438585, 113926993, 338241895, 666307205, 773529912, 1294757372, 1396182291, 1695183700, 1986661051, -2117940946, -1838011259, -1564481375, -1474664885, -1035236496, -949202525, -778901479, -694614492, -200395387, 275423344, 430227734, 506948616, 659060556, 883997877, 958139571, 1322822218, 1537002063, 1747873779, 1955562222, 2024104815, -2067236844, -1933114872, -1866530822, -1538233109, -1090935817, -965641998};

   public SHA256Digest() {
      this.reset();
   }

   public String getAlgorithmName() {
      return "SHA-256";
   }

   public int getDigestSize() {
      return 32;
   }

   protected void processWord(byte[] var1, int var2) {
      int var3 = var1[var2] << 24;
      ++var2;
      var3 |= (var1[var2] & 255) << 16;
      ++var2;
      var3 |= (var1[var2] & 255) << 8;
      ++var2;
      var3 |= var1[var2] & 255;
      this.X[this.xOff] = var3;
      if (++this.xOff == 16) {
         this.processBlock();
      }

   }

   protected void processLength(long var1) {
      if (this.xOff > 14) {
         this.processBlock();
      }

      this.X[14] = (int)(var1 >>> 32);
      this.X[15] = (int)(var1 & -1L);
   }

   public int doFinal(byte[] var1, int var2) {
      this.finish();
      Pack.intToBigEndian(this.H1, var1, var2);
      Pack.intToBigEndian(this.H2, var1, var2 + 4);
      Pack.intToBigEndian(this.H3, var1, var2 + 8);
      Pack.intToBigEndian(this.H4, var1, var2 + 12);
      Pack.intToBigEndian(this.H5, var1, var2 + 16);
      Pack.intToBigEndian(this.H6, var1, var2 + 20);
      Pack.intToBigEndian(this.H7, var1, var2 + 24);
      Pack.intToBigEndian(this.H8, var1, var2 + 28);
      this.reset();
      return 32;
   }

   public void reset() {
      super.reset();
      this.H1 = 1779033703;
      this.H2 = -1150833019;
      this.H3 = 1013904242;
      this.H4 = -1521486534;
      this.H5 = 1359893119;
      this.H6 = -1694144372;
      this.H7 = 528734635;
      this.H8 = 1541459225;
      this.xOff = 0;

      for(int var1 = 0; var1 != this.X.length; ++var1) {
         this.X[var1] = 0;
      }

   }

   protected void processBlock() {
      int var1;
      for(var1 = 16; var1 <= 63; ++var1) {
         this.X[var1] = Theta1(this.X[var1 - 2]) + this.X[var1 - 7] + Theta0(this.X[var1 - 15]) + this.X[var1 - 16];
      }

      var1 = this.H1;
      int var2 = this.H2;
      int var3 = this.H3;
      int var4 = this.H4;
      int var5 = this.H5;
      int var6 = this.H6;
      int var7 = this.H7;
      int var8 = this.H8;
      int var9 = 0;

      int var10;
      for(var10 = 0; var10 < 8; ++var10) {
         var8 += Sum1(var5) + Ch(var5, var6, var7) + K[var9] + this.X[var9];
         var4 += var8;
         var8 += Sum0(var1) + Maj(var1, var2, var3);
         ++var9;
         var7 += Sum1(var4) + Ch(var4, var5, var6) + K[var9] + this.X[var9];
         var3 += var7;
         var7 += Sum0(var8) + Maj(var8, var1, var2);
         ++var9;
         var6 += Sum1(var3) + Ch(var3, var4, var5) + K[var9] + this.X[var9];
         var2 += var6;
         var6 += Sum0(var7) + Maj(var7, var8, var1);
         ++var9;
         var5 += Sum1(var2) + Ch(var2, var3, var4) + K[var9] + this.X[var9];
         var1 += var5;
         var5 += Sum0(var6) + Maj(var6, var7, var8);
         ++var9;
         var4 += Sum1(var1) + Ch(var1, var2, var3) + K[var9] + this.X[var9];
         var8 += var4;
         var4 += Sum0(var5) + Maj(var5, var6, var7);
         ++var9;
         var3 += Sum1(var8) + Ch(var8, var1, var2) + K[var9] + this.X[var9];
         var7 += var3;
         var3 += Sum0(var4) + Maj(var4, var5, var6);
         ++var9;
         var2 += Sum1(var7) + Ch(var7, var8, var1) + K[var9] + this.X[var9];
         var6 += var2;
         var2 += Sum0(var3) + Maj(var3, var4, var5);
         ++var9;
         var1 += Sum1(var6) + Ch(var6, var7, var8) + K[var9] + this.X[var9];
         var5 += var1;
         var1 += Sum0(var2) + Maj(var2, var3, var4);
         ++var9;
      }

      this.H1 += var1;
      this.H2 += var2;
      this.H3 += var3;
      this.H4 += var4;
      this.H5 += var5;
      this.H6 += var6;
      this.H7 += var7;
      this.H8 += var8;
      this.xOff = 0;

      for(var10 = 0; var10 < 16; ++var10) {
         this.X[var10] = 0;
      }

   }

   private static int Ch(int var0, int var1, int var2) {
      return var0 & var1 ^ ~var0 & var2;
   }

   private static int Maj(int var0, int var1, int var2) {
      return var0 & var1 | var2 & (var0 ^ var1);
   }

   private static int Sum0(int var0) {
      return (var0 >>> 2 | var0 << 30) ^ (var0 >>> 13 | var0 << 19) ^ (var0 >>> 22 | var0 << 10);
   }

   private static int Sum1(int var0) {
      return (var0 >>> 6 | var0 << 26) ^ (var0 >>> 11 | var0 << 21) ^ (var0 >>> 25 | var0 << 7);
   }

   private static int Theta0(int var0) {
      return (var0 >>> 7 | var0 << 25) ^ (var0 >>> 18 | var0 << 14) ^ var0 >>> 3;
   }

   private static int Theta1(int var0) {
      return (var0 >>> 17 | var0 << 15) ^ (var0 >>> 19 | var0 << 13) ^ var0 >>> 10;
   }
}
