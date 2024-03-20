package net.jsign.bouncycastle.pqc.crypto.xmss;

import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.crypto.Digest;

final class WOTSPlusParameters {
   private final XMSSOid oid;
   private final int digestSize;
   private final int winternitzParameter;
   private final int len;
   private final int len1;
   private final int len2;
   private final ASN1ObjectIdentifier treeDigest;

   protected WOTSPlusParameters(ASN1ObjectIdentifier var1) {
      if (var1 == null) {
         throw new NullPointerException("treeDigest == null");
      } else {
         this.treeDigest = var1;
         Digest var2 = DigestUtil.getDigest(var1);
         this.digestSize = XMSSUtil.getDigestSize(var2);
         this.winternitzParameter = 16;
         this.len1 = (int)Math.ceil((double)(8 * this.digestSize) / (double)XMSSUtil.log2(this.winternitzParameter));
         this.len2 = (int)Math.floor((double)(XMSSUtil.log2(this.len1 * (this.winternitzParameter - 1)) / XMSSUtil.log2(this.winternitzParameter))) + 1;
         this.len = this.len1 + this.len2;
         this.oid = WOTSPlusOid.lookup(var2.getAlgorithmName(), this.digestSize, this.winternitzParameter, this.len);
         if (this.oid == null) {
            throw new IllegalArgumentException("cannot find OID for digest algorithm: " + var2.getAlgorithmName());
         }
      }
   }

   protected int getTreeDigestSize() {
      return this.digestSize;
   }

   protected int getWinternitzParameter() {
      return this.winternitzParameter;
   }

   protected int getLen() {
      return this.len;
   }

   public ASN1ObjectIdentifier getTreeDigest() {
      return this.treeDigest;
   }
}
