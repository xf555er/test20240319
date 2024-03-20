package net.jsign.bouncycastle.crypto.digests;

import net.jsign.bouncycastle.crypto.Xof;

public class SHAKEDigest extends KeccakDigest implements Xof {
   private static int checkBitLength(int var0) {
      switch (var0) {
         case 128:
         case 256:
            return var0;
         default:
            throw new IllegalArgumentException("'bitLength' " + var0 + " not supported for SHAKE");
      }
   }

   public SHAKEDigest() {
      this(128);
   }

   public SHAKEDigest(int var1) {
      super(checkBitLength(var1));
   }

   public String getAlgorithmName() {
      return "SHAKE" + this.fixedOutputLength;
   }

   public int getDigestSize() {
      return this.fixedOutputLength / 4;
   }

   public int doFinal(byte[] var1, int var2) {
      return this.doFinal(var1, var2, this.getDigestSize());
   }

   public int doFinal(byte[] var1, int var2, int var3) {
      int var4 = this.doOutput(var1, var2, var3);
      this.reset();
      return var4;
   }

   public int doOutput(byte[] var1, int var2, int var3) {
      if (!this.squeezing) {
         this.absorbBits(15, 4);
      }

      this.squeeze(var1, var2, (long)var3 * 8L);
      return var3;
   }
}
