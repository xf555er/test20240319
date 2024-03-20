package net.jsign.bouncycastle.crypto.digests;

import net.jsign.bouncycastle.util.Pack;

public class SHA384Digest extends LongDigest {
   public String getAlgorithmName() {
      return "SHA-384";
   }

   public int getDigestSize() {
      return 48;
   }

   public int doFinal(byte[] var1, int var2) {
      this.finish();
      Pack.longToBigEndian(this.H1, var1, var2);
      Pack.longToBigEndian(this.H2, var1, var2 + 8);
      Pack.longToBigEndian(this.H3, var1, var2 + 16);
      Pack.longToBigEndian(this.H4, var1, var2 + 24);
      Pack.longToBigEndian(this.H5, var1, var2 + 32);
      Pack.longToBigEndian(this.H6, var1, var2 + 40);
      this.reset();
      return 48;
   }

   public void reset() {
      super.reset();
      this.H1 = -3766243637369397544L;
      this.H2 = 7105036623409894663L;
      this.H3 = -7973340178411365097L;
      this.H4 = 1526699215303891257L;
      this.H5 = 7436329637833083697L;
      this.H6 = -8163818279084223215L;
      this.H7 = -2662702644619276377L;
      this.H8 = 5167115440072839076L;
   }
}
