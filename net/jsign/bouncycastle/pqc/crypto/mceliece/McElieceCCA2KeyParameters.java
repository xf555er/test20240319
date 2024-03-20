package net.jsign.bouncycastle.pqc.crypto.mceliece;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class McElieceCCA2KeyParameters extends AsymmetricKeyParameter {
   private String params;

   public McElieceCCA2KeyParameters(boolean var1, String var2) {
      super(var1);
      this.params = var2;
   }

   public String getDigest() {
      return this.params;
   }
}
