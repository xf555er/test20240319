package net.jsign.bouncycastle.crypto.digests;

import net.jsign.bouncycastle.util.Pack;

public class SHA1Digest extends GeneralDigest {
   private int H1;
   private int H2;
   private int H3;
   private int H4;
   private int H5;
   private int[] X = new int[80];
   private int xOff;

   public SHA1Digest() {
      this.reset();
   }

   public String getAlgorithmName() {
      return "SHA-1";
   }

   public int getDigestSize() {
      return 20;
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
      this.X[15] = (int)var1;
   }

   public int doFinal(byte[] var1, int var2) {
      this.finish();
      Pack.intToBigEndian(this.H1, var1, var2);
      Pack.intToBigEndian(this.H2, var1, var2 + 4);
      Pack.intToBigEndian(this.H3, var1, var2 + 8);
      Pack.intToBigEndian(this.H4, var1, var2 + 12);
      Pack.intToBigEndian(this.H5, var1, var2 + 16);
      this.reset();
      return 20;
   }

   public void reset() {
      super.reset();
      this.H1 = 1732584193;
      this.H2 = -271733879;
      this.H3 = -1732584194;
      this.H4 = 271733878;
      this.H5 = -1009589776;
      this.xOff = 0;

      for(int var1 = 0; var1 != this.X.length; ++var1) {
         this.X[var1] = 0;
      }

   }

   private int f(int var1, int var2, int var3) {
      return var1 & var2 | ~var1 & var3;
   }

   private int h(int var1, int var2, int var3) {
      return var1 ^ var2 ^ var3;
   }

   private int g(int var1, int var2, int var3) {
      return var1 & var2 | var1 & var3 | var2 & var3;
   }

   protected void processBlock() {
      int var1;
      int var2;
      for(var1 = 16; var1 < 80; ++var1) {
         var2 = this.X[var1 - 3] ^ this.X[var1 - 8] ^ this.X[var1 - 14] ^ this.X[var1 - 16];
         this.X[var1] = var2 << 1 | var2 >>> 31;
      }

      var1 = this.H1;
      var2 = this.H2;
      int var3 = this.H3;
      int var4 = this.H4;
      int var5 = this.H5;
      int var6 = 0;

      int var7;
      for(var7 = 0; var7 < 4; ++var7) {
         var5 += (var1 << 5 | var1 >>> 27) + this.f(var2, var3, var4) + this.X[var6++] + 1518500249;
         var2 = var2 << 30 | var2 >>> 2;
         var4 += (var5 << 5 | var5 >>> 27) + this.f(var1, var2, var3) + this.X[var6++] + 1518500249;
         var1 = var1 << 30 | var1 >>> 2;
         var3 += (var4 << 5 | var4 >>> 27) + this.f(var5, var1, var2) + this.X[var6++] + 1518500249;
         var5 = var5 << 30 | var5 >>> 2;
         var2 += (var3 << 5 | var3 >>> 27) + this.f(var4, var5, var1) + this.X[var6++] + 1518500249;
         var4 = var4 << 30 | var4 >>> 2;
         var1 += (var2 << 5 | var2 >>> 27) + this.f(var3, var4, var5) + this.X[var6++] + 1518500249;
         var3 = var3 << 30 | var3 >>> 2;
      }

      for(var7 = 0; var7 < 4; ++var7) {
         var5 += (var1 << 5 | var1 >>> 27) + this.h(var2, var3, var4) + this.X[var6++] + 1859775393;
         var2 = var2 << 30 | var2 >>> 2;
         var4 += (var5 << 5 | var5 >>> 27) + this.h(var1, var2, var3) + this.X[var6++] + 1859775393;
         var1 = var1 << 30 | var1 >>> 2;
         var3 += (var4 << 5 | var4 >>> 27) + this.h(var5, var1, var2) + this.X[var6++] + 1859775393;
         var5 = var5 << 30 | var5 >>> 2;
         var2 += (var3 << 5 | var3 >>> 27) + this.h(var4, var5, var1) + this.X[var6++] + 1859775393;
         var4 = var4 << 30 | var4 >>> 2;
         var1 += (var2 << 5 | var2 >>> 27) + this.h(var3, var4, var5) + this.X[var6++] + 1859775393;
         var3 = var3 << 30 | var3 >>> 2;
      }

      for(var7 = 0; var7 < 4; ++var7) {
         var5 += (var1 << 5 | var1 >>> 27) + this.g(var2, var3, var4) + this.X[var6++] + -1894007588;
         var2 = var2 << 30 | var2 >>> 2;
         var4 += (var5 << 5 | var5 >>> 27) + this.g(var1, var2, var3) + this.X[var6++] + -1894007588;
         var1 = var1 << 30 | var1 >>> 2;
         var3 += (var4 << 5 | var4 >>> 27) + this.g(var5, var1, var2) + this.X[var6++] + -1894007588;
         var5 = var5 << 30 | var5 >>> 2;
         var2 += (var3 << 5 | var3 >>> 27) + this.g(var4, var5, var1) + this.X[var6++] + -1894007588;
         var4 = var4 << 30 | var4 >>> 2;
         var1 += (var2 << 5 | var2 >>> 27) + this.g(var3, var4, var5) + this.X[var6++] + -1894007588;
         var3 = var3 << 30 | var3 >>> 2;
      }

      for(var7 = 0; var7 <= 3; ++var7) {
         var5 += (var1 << 5 | var1 >>> 27) + this.h(var2, var3, var4) + this.X[var6++] + -899497514;
         var2 = var2 << 30 | var2 >>> 2;
         var4 += (var5 << 5 | var5 >>> 27) + this.h(var1, var2, var3) + this.X[var6++] + -899497514;
         var1 = var1 << 30 | var1 >>> 2;
         var3 += (var4 << 5 | var4 >>> 27) + this.h(var5, var1, var2) + this.X[var6++] + -899497514;
         var5 = var5 << 30 | var5 >>> 2;
         var2 += (var3 << 5 | var3 >>> 27) + this.h(var4, var5, var1) + this.X[var6++] + -899497514;
         var4 = var4 << 30 | var4 >>> 2;
         var1 += (var2 << 5 | var2 >>> 27) + this.h(var3, var4, var5) + this.X[var6++] + -899497514;
         var3 = var3 << 30 | var3 >>> 2;
      }

      this.H1 += var1;
      this.H2 += var2;
      this.H3 += var3;
      this.H4 += var4;
      this.H5 += var5;
      this.xOff = 0;

      for(var7 = 0; var7 < 16; ++var7) {
         this.X[var7] = 0;
      }

   }
}
