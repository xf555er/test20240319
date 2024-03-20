package net.jsign.bouncycastle.crypto.digests;

import net.jsign.bouncycastle.crypto.ExtendedDigest;
import net.jsign.bouncycastle.util.Arrays;
import net.jsign.bouncycastle.util.Pack;
import net.jsign.bouncycastle.util.encoders.Hex;

public class KeccakDigest implements ExtendedDigest {
   private static long[] KeccakRoundConstants = new long[]{1L, 32898L, -9223372036854742902L, -9223372034707259392L, 32907L, 2147483649L, -9223372034707259263L, -9223372036854743031L, 138L, 136L, 2147516425L, 2147483658L, 2147516555L, -9223372036854775669L, -9223372036854742903L, -9223372036854743037L, -9223372036854743038L, -9223372036854775680L, 32778L, -9223372034707292150L, -9223372034707259263L, -9223372036854742912L, 2147483649L, -9223372034707259384L};
   protected long[] state = new long[25];
   protected byte[] dataQueue = new byte[192];
   protected int rate;
   protected int bitsInQueue;
   protected int fixedOutputLength;
   protected boolean squeezing;

   public KeccakDigest(int var1) {
      this.init(var1);
   }

   public String getAlgorithmName() {
      return "Keccak-" + this.fixedOutputLength;
   }

   public int getDigestSize() {
      return this.fixedOutputLength / 8;
   }

   public void update(byte var1) {
      this.absorb(var1);
   }

   public void update(byte[] var1, int var2, int var3) {
      this.absorb(var1, var2, var3);
   }

   public int doFinal(byte[] var1, int var2) {
      this.squeeze(var1, var2, (long)this.fixedOutputLength);
      this.reset();
      return this.getDigestSize();
   }

   public void reset() {
      this.init(this.fixedOutputLength);
   }

   private void init(int var1) {
      switch (var1) {
         case 128:
         case 224:
         case 256:
         case 288:
         case 384:
         case 512:
            this.initSponge(1600 - (var1 << 1));
            return;
         default:
            throw new IllegalArgumentException("bitLength must be one of 128, 224, 256, 288, 384, or 512.");
      }
   }

   private void initSponge(int var1) {
      if (var1 > 0 && var1 < 1600 && var1 % 64 == 0) {
         this.rate = var1;

         for(int var2 = 0; var2 < this.state.length; ++var2) {
            this.state[var2] = 0L;
         }

         Arrays.fill(this.dataQueue, (byte)0);
         this.bitsInQueue = 0;
         this.squeezing = false;
         this.fixedOutputLength = (1600 - var1) / 2;
      } else {
         throw new IllegalStateException("invalid rate value");
      }
   }

   protected void absorb(byte var1) {
      if (this.bitsInQueue % 8 != 0) {
         throw new IllegalStateException("attempt to absorb with odd length queue");
      } else if (this.squeezing) {
         throw new IllegalStateException("attempt to absorb while squeezing");
      } else {
         this.dataQueue[this.bitsInQueue >>> 3] = var1;
         if ((this.bitsInQueue += 8) == this.rate) {
            this.KeccakAbsorb(this.dataQueue, 0);
            this.bitsInQueue = 0;
         }

      }
   }

   protected void absorb(byte[] var1, int var2, int var3) {
      if (this.bitsInQueue % 8 != 0) {
         throw new IllegalStateException("attempt to absorb with odd length queue");
      } else if (this.squeezing) {
         throw new IllegalStateException("attempt to absorb while squeezing");
      } else {
         int var4 = this.bitsInQueue >>> 3;
         int var5 = this.rate >>> 3;
         int var6 = var5 - var4;
         if (var3 < var6) {
            System.arraycopy(var1, var2, this.dataQueue, var4, var3);
            this.bitsInQueue += var3 << 3;
         } else {
            int var7 = 0;
            if (var4 > 0) {
               System.arraycopy(var1, var2, this.dataQueue, var4, var6);
               var7 += var6;
               this.KeccakAbsorb(this.dataQueue, 0);
            }

            int var8;
            while((var8 = var3 - var7) >= var5) {
               this.KeccakAbsorb(var1, var2 + var7);
               var7 += var5;
            }

            System.arraycopy(var1, var2 + var7, this.dataQueue, 0, var8);
            this.bitsInQueue = var8 << 3;
         }
      }
   }

   protected void absorbBits(int var1, int var2) {
      if (var2 >= 1 && var2 <= 7) {
         if (this.bitsInQueue % 8 != 0) {
            throw new IllegalStateException("attempt to absorb with odd length queue");
         } else if (this.squeezing) {
            throw new IllegalStateException("attempt to absorb while squeezing");
         } else {
            int var3 = (1 << var2) - 1;
            this.dataQueue[this.bitsInQueue >>> 3] = (byte)(var1 & var3);
            this.bitsInQueue += var2;
         }
      } else {
         throw new IllegalArgumentException("'bits' must be in the range 1 to 7");
      }
   }

   protected byte[] dumpState() {
      byte[] var1 = new byte[this.state.length * 8];
      int var2 = 0;

      for(int var3 = 0; var3 != this.state.length; ++var3) {
         Pack.longToLittleEndian(this.state[var3], var1, var2);
         var2 += 8;
      }

      return var1;
   }

   private void padAndSwitchToSqueezingPhase() {
      byte[] var10000 = this.dataQueue;
      int var10001 = this.bitsInQueue >>> 3;
      var10000[var10001] |= (byte)(1 << (this.bitsInQueue & 7));
      long[] var8;
      if (++this.bitsInQueue == this.rate) {
         this.KeccakAbsorb(this.dataQueue, 0);
      } else {
         int var1 = this.bitsInQueue >>> 6;
         int var2 = this.bitsInQueue & 63;
         int var3 = 0;

         for(int var4 = 0; var4 < var1; ++var4) {
            var8 = this.state;
            var8[var4] ^= Pack.littleEndianToLong(this.dataQueue, var3);
            var3 += 8;
         }

         byte[] var7 = this.dumpState();
         if (var2 > 0) {
            long var5 = (1L << var2) - 1L;
            var8 = this.state;
            var8[var1] ^= Pack.littleEndianToLong(this.dataQueue, var3) & var5;
         }
      }

      var8 = this.state;
      var10001 = this.rate - 1 >>> 6;
      var8[var10001] ^= Long.MIN_VALUE;
      this.bitsInQueue = 0;
      this.squeezing = true;
   }

   protected void squeeze(byte[] var1, int var2, long var3) {
      if (!this.squeezing) {
         this.padAndSwitchToSqueezingPhase();
      }

      byte[] var5 = this.dumpState();
      if (var3 % 8L != 0L) {
         throw new IllegalStateException("outputLength not a multiple of 8");
      } else {
         int var8;
         for(long var6 = 0L; var6 < var3; var6 += (long)var8) {
            if (this.bitsInQueue == 0) {
               this.KeccakExtract();
            }

            var8 = (int)Math.min((long)this.bitsInQueue, var3 - var6);
            System.arraycopy(this.dataQueue, (this.rate - this.bitsInQueue) / 8, var1, var2 + (int)(var6 / 8L), var8 / 8);
            this.bitsInQueue -= var8;
         }

         var5 = this.dumpState();
      }
   }

   private void KeccakAbsorb(byte[] var1, int var2) {
      int var3 = this.rate >>> 6;

      for(int var4 = 0; var4 < var3; ++var4) {
         long[] var10000 = this.state;
         var10000[var4] ^= Pack.littleEndianToLong(var1, var2);
         var2 += 8;
      }

      String var5 = Hex.toHexString(this.dumpState()).toLowerCase();
      this.KeccakPermutation();
   }

   private void KeccakExtract() {
      this.KeccakPermutation();
      byte[] var1 = this.dumpState();
      Pack.longToLittleEndian(this.state, 0, this.rate >>> 6, this.dataQueue, 0);
      this.bitsInQueue = this.rate;
   }

   private void KeccakPermutation() {
      long[] var1 = this.state;
      long var2 = var1[0];
      long var4 = var1[1];
      long var6 = var1[2];
      long var8 = var1[3];
      long var10 = var1[4];
      long var12 = var1[5];
      long var14 = var1[6];
      long var16 = var1[7];
      long var18 = var1[8];
      long var20 = var1[9];
      long var22 = var1[10];
      long var24 = var1[11];
      long var26 = var1[12];
      long var28 = var1[13];
      long var30 = var1[14];
      long var32 = var1[15];
      long var34 = var1[16];
      long var36 = var1[17];
      long var38 = var1[18];
      long var40 = var1[19];
      long var42 = var1[20];
      long var44 = var1[21];
      long var46 = var1[22];
      long var48 = var1[23];
      long var50 = var1[24];

      for(int var52 = 0; var52 < 24; ++var52) {
         long var53 = var2 ^ var12 ^ var22 ^ var32 ^ var42;
         long var55 = var4 ^ var14 ^ var24 ^ var34 ^ var44;
         long var57 = var6 ^ var16 ^ var26 ^ var36 ^ var46;
         long var59 = var8 ^ var18 ^ var28 ^ var38 ^ var48;
         long var61 = var10 ^ var20 ^ var30 ^ var40 ^ var50;
         long var63 = (var55 << 1 | var55 >>> -1) ^ var61;
         long var65 = (var57 << 1 | var57 >>> -1) ^ var53;
         long var67 = (var59 << 1 | var59 >>> -1) ^ var55;
         long var69 = (var61 << 1 | var61 >>> -1) ^ var57;
         long var71 = (var53 << 1 | var53 >>> -1) ^ var59;
         var2 ^= var63;
         var12 ^= var63;
         var22 ^= var63;
         var32 ^= var63;
         var42 ^= var63;
         var4 ^= var65;
         var14 ^= var65;
         var24 ^= var65;
         var34 ^= var65;
         var44 ^= var65;
         var6 ^= var67;
         var16 ^= var67;
         var26 ^= var67;
         var36 ^= var67;
         var46 ^= var67;
         var8 ^= var69;
         var18 ^= var69;
         var28 ^= var69;
         var38 ^= var69;
         var48 ^= var69;
         var10 ^= var71;
         var20 ^= var71;
         var30 ^= var71;
         var40 ^= var71;
         var50 ^= var71;
         var55 = var4 << 1 | var4 >>> 63;
         var4 = var14 << 44 | var14 >>> 20;
         var14 = var20 << 20 | var20 >>> 44;
         var20 = var46 << 61 | var46 >>> 3;
         var46 = var30 << 39 | var30 >>> 25;
         var30 = var42 << 18 | var42 >>> 46;
         var42 = var6 << 62 | var6 >>> 2;
         var6 = var26 << 43 | var26 >>> 21;
         var26 = var28 << 25 | var28 >>> 39;
         var28 = var40 << 8 | var40 >>> 56;
         var40 = var48 << 56 | var48 >>> 8;
         var48 = var32 << 41 | var32 >>> 23;
         var32 = var10 << 27 | var10 >>> 37;
         var10 = var50 << 14 | var50 >>> 50;
         var50 = var44 << 2 | var44 >>> 62;
         var44 = var18 << 55 | var18 >>> 9;
         var18 = var34 << 45 | var34 >>> 19;
         var34 = var12 << 36 | var12 >>> 28;
         var12 = var8 << 28 | var8 >>> 36;
         var8 = var38 << 21 | var38 >>> 43;
         var38 = var36 << 15 | var36 >>> 49;
         var36 = var24 << 10 | var24 >>> 54;
         var24 = var16 << 6 | var16 >>> 58;
         var16 = var22 << 3 | var22 >>> 61;
         var22 = var55;
         var53 = var2 ^ ~var4 & var6;
         var55 = var4 ^ ~var6 & var8;
         var6 ^= ~var8 & var10;
         var8 ^= ~var10 & var2;
         var10 ^= ~var2 & var4;
         var2 = var53;
         var4 = var55;
         var53 = var12 ^ ~var14 & var16;
         var55 = var14 ^ ~var16 & var18;
         var16 ^= ~var18 & var20;
         var18 ^= ~var20 & var12;
         var20 ^= ~var12 & var14;
         var12 = var53;
         var14 = var55;
         var53 = var22 ^ ~var24 & var26;
         var55 = var24 ^ ~var26 & var28;
         var26 ^= ~var28 & var30;
         var28 ^= ~var30 & var22;
         var30 ^= ~var22 & var24;
         var22 = var53;
         var24 = var55;
         var53 = var32 ^ ~var34 & var36;
         var55 = var34 ^ ~var36 & var38;
         var36 ^= ~var38 & var40;
         var38 ^= ~var40 & var32;
         var40 ^= ~var32 & var34;
         var32 = var53;
         var34 = var55;
         var53 = var42 ^ ~var44 & var46;
         var55 = var44 ^ ~var46 & var48;
         var46 ^= ~var48 & var50;
         var48 ^= ~var50 & var42;
         var50 ^= ~var42 & var44;
         var42 = var53;
         var44 = var55;
         var2 ^= KeccakRoundConstants[var52];
      }

      var1[0] = var2;
      var1[1] = var4;
      var1[2] = var6;
      var1[3] = var8;
      var1[4] = var10;
      var1[5] = var12;
      var1[6] = var14;
      var1[7] = var16;
      var1[8] = var18;
      var1[9] = var20;
      var1[10] = var22;
      var1[11] = var24;
      var1[12] = var26;
      var1[13] = var28;
      var1[14] = var30;
      var1[15] = var32;
      var1[16] = var34;
      var1[17] = var36;
      var1[18] = var38;
      var1[19] = var40;
      var1[20] = var42;
      var1[21] = var44;
      var1[22] = var46;
      var1[23] = var48;
      var1[24] = var50;
   }
}
