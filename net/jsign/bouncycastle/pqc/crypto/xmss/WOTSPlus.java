package net.jsign.bouncycastle.pqc.crypto.xmss;

import net.jsign.bouncycastle.util.Arrays;

final class WOTSPlus {
   private final WOTSPlusParameters params;
   private final KeyedHashFunctions khf;
   private byte[] secretKeySeed;
   private byte[] publicSeed;

   WOTSPlus(WOTSPlusParameters var1) {
      if (var1 == null) {
         throw new NullPointerException("params == null");
      } else {
         this.params = var1;
         int var2 = var1.getTreeDigestSize();
         this.khf = new KeyedHashFunctions(var1.getTreeDigest(), var2);
         this.secretKeySeed = new byte[var2];
         this.publicSeed = new byte[var2];
      }
   }

   void importKeys(byte[] var1, byte[] var2) {
      if (var1 == null) {
         throw new NullPointerException("secretKeySeed == null");
      } else if (var1.length != this.params.getTreeDigestSize()) {
         throw new IllegalArgumentException("size of secretKeySeed needs to be equal to size of digest");
      } else if (var2 == null) {
         throw new NullPointerException("publicSeed == null");
      } else if (var2.length != this.params.getTreeDigestSize()) {
         throw new IllegalArgumentException("size of publicSeed needs to be equal to size of digest");
      } else {
         this.secretKeySeed = var1;
         this.publicSeed = var2;
      }
   }

   private byte[] chain(byte[] var1, int var2, int var3, OTSHashAddress var4) {
      int var5 = this.params.getTreeDigestSize();
      if (var1 == null) {
         throw new NullPointerException("startHash == null");
      } else if (var1.length != var5) {
         throw new IllegalArgumentException("startHash needs to be " + var5 + "bytes");
      } else if (var4 == null) {
         throw new NullPointerException("otsHashAddress == null");
      } else if (var4.toByteArray() == null) {
         throw new NullPointerException("otsHashAddress byte array == null");
      } else if (var2 + var3 > this.params.getWinternitzParameter() - 1) {
         throw new IllegalArgumentException("max chain length must not be greater than w");
      } else if (var3 == 0) {
         return var1;
      } else {
         byte[] var6 = this.chain(var1, var2, var3 - 1, var4);
         var4 = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)((OTSHashAddress.Builder)(new OTSHashAddress.Builder()).withLayerAddress(var4.getLayerAddress())).withTreeAddress(var4.getTreeAddress())).withOTSAddress(var4.getOTSAddress()).withChainAddress(var4.getChainAddress()).withHashAddress(var2 + var3 - 1).withKeyAndMask(0)).build();
         byte[] var7 = this.khf.PRF(this.publicSeed, var4.toByteArray());
         var4 = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)((OTSHashAddress.Builder)(new OTSHashAddress.Builder()).withLayerAddress(var4.getLayerAddress())).withTreeAddress(var4.getTreeAddress())).withOTSAddress(var4.getOTSAddress()).withChainAddress(var4.getChainAddress()).withHashAddress(var4.getHashAddress()).withKeyAndMask(1)).build();
         byte[] var8 = this.khf.PRF(this.publicSeed, var4.toByteArray());
         byte[] var9 = new byte[var5];

         for(int var10 = 0; var10 < var5; ++var10) {
            var9[var10] = (byte)(var6[var10] ^ var8[var10]);
         }

         var6 = this.khf.F(var7, var9);
         return var6;
      }
   }

   protected byte[] getWOTSPlusSecretKey(byte[] var1, OTSHashAddress var2) {
      var2 = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)(new OTSHashAddress.Builder()).withLayerAddress(var2.getLayerAddress())).withTreeAddress(var2.getTreeAddress())).withOTSAddress(var2.getOTSAddress()).build();
      return this.khf.PRF(var1, var2.toByteArray());
   }

   private byte[] expandSecretKeySeed(int var1) {
      if (var1 >= 0 && var1 < this.params.getLen()) {
         return this.khf.PRF(this.secretKeySeed, XMSSUtil.toBytesBigEndian((long)var1, 32));
      } else {
         throw new IllegalArgumentException("index out of bounds");
      }
   }

   protected WOTSPlusParameters getParams() {
      return this.params;
   }

   protected KeyedHashFunctions getKhf() {
      return this.khf;
   }

   protected byte[] getPublicSeed() {
      return Arrays.clone(this.publicSeed);
   }

   WOTSPlusPublicKeyParameters getPublicKey(OTSHashAddress var1) {
      if (var1 == null) {
         throw new NullPointerException("otsHashAddress == null");
      } else {
         byte[][] var2 = new byte[this.params.getLen()][];

         for(int var3 = 0; var3 < this.params.getLen(); ++var3) {
            var1 = (OTSHashAddress)((OTSHashAddress.Builder)((OTSHashAddress.Builder)((OTSHashAddress.Builder)(new OTSHashAddress.Builder()).withLayerAddress(var1.getLayerAddress())).withTreeAddress(var1.getTreeAddress())).withOTSAddress(var1.getOTSAddress()).withChainAddress(var3).withHashAddress(var1.getHashAddress()).withKeyAndMask(var1.getKeyAndMask())).build();
            var2[var3] = this.chain(this.expandSecretKeySeed(var3), 0, this.params.getWinternitzParameter() - 1, var1);
         }

         return new WOTSPlusPublicKeyParameters(this.params, var2);
      }
   }
}
