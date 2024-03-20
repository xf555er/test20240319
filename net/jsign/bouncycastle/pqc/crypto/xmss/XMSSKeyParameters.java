package net.jsign.bouncycastle.pqc.crypto.xmss;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class XMSSKeyParameters extends AsymmetricKeyParameter {
   private final String treeDigest;

   public XMSSKeyParameters(boolean var1, String var2) {
      super(var1);
      this.treeDigest = var2;
   }

   public String getTreeDigest() {
      return this.treeDigest;
   }
}
