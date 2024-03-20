package net.jsign.bouncycastle.crypto.digests;

public class SHA3Digest extends KeccakDigest {
   private static int checkBitLength(int var0) {
      switch (var0) {
         case 224:
         case 256:
         case 384:
         case 512:
            return var0;
         default:
            throw new IllegalArgumentException("'bitLength' " + var0 + " not supported for SHA-3");
      }
   }

   public SHA3Digest() {
      this(256);
   }

   public SHA3Digest(int var1) {
      super(checkBitLength(var1));
   }

   public String getAlgorithmName() {
      return "SHA3-" + this.fixedOutputLength;
   }

   public int doFinal(byte[] var1, int var2) {
      this.absorbBits(2, 2);
      return super.doFinal(var1, var2);
   }
}
