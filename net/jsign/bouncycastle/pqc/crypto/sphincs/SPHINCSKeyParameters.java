package net.jsign.bouncycastle.pqc.crypto.sphincs;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class SPHINCSKeyParameters extends AsymmetricKeyParameter {
   private final String treeDigest;

   protected SPHINCSKeyParameters(boolean var1, String var2) {
      super(var1);
      this.treeDigest = var2;
   }

   public String getTreeDigest() {
      return this.treeDigest;
   }
}
