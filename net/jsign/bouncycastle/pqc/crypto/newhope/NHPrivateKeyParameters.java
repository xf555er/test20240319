package net.jsign.bouncycastle.pqc.crypto.newhope;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.util.Arrays;

public class NHPrivateKeyParameters extends AsymmetricKeyParameter {
   final short[] secData;

   public NHPrivateKeyParameters(short[] var1) {
      super(true);
      this.secData = Arrays.clone(var1);
   }

   public short[] getSecData() {
      return Arrays.clone(this.secData);
   }
}
