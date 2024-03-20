package net.jsign.bouncycastle.pqc.crypto.lms;

import net.jsign.bouncycastle.crypto.Digest;
import net.jsign.bouncycastle.util.Pack;

class LM_OTS {
   static byte[] lms_ots_generatePublicKey(LMOtsParameters var0, byte[] var1, int var2, byte[] var3) {
      Digest var4 = DigestUtil.getDigest(var0.getDigestOID());
      byte[] var5 = Composer.compose().bytes(var1).u32str(var2).u16str(-32640).padUntil(0, 22).build();
      var4.update(var5, 0, var5.length);
      Digest var6 = DigestUtil.getDigest(var0.getDigestOID());
      byte[] var7 = Composer.compose().bytes(var1).u32str(var2).padUntil(0, 23 + var6.getDigestSize()).build();
      SeedDerive var8 = new SeedDerive(var1, var3, DigestUtil.getDigest(var0.getDigestOID()));
      var8.setQ(var2);
      var8.setJ(0);
      int var9 = var0.getP();
      int var10 = var0.getN();
      int var11 = (1 << var0.getW()) - 1;

      for(int var12 = 0; var12 < var9; ++var12) {
         var8.deriveSeed(var7, var12 < var9 - 1, 23);
         Pack.shortToBigEndian((short)var12, var7, 20);

         for(int var13 = 0; var13 < var11; ++var13) {
            var7[22] = (byte)var13;
            var6.update(var7, 0, var7.length);
            var6.doFinal(var7, 23);
         }

         var4.update(var7, 23, var10);
      }

      byte[] var14 = new byte[var4.getDigestSize()];
      var4.doFinal(var14, 0);
      return var14;
   }
}
