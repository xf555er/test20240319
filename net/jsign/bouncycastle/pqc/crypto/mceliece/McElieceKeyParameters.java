package net.jsign.bouncycastle.pqc.crypto.mceliece;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;

public class McElieceKeyParameters extends AsymmetricKeyParameter {
   private McElieceParameters params;

   public McElieceKeyParameters(boolean var1, McElieceParameters var2) {
      super(var1);
      this.params = var2;
   }
}
