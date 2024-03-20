package net.jsign.bouncycastle.pqc.crypto.newhope;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.util.Arrays;

public class NHPublicKeyParameters extends AsymmetricKeyParameter {
   final byte[] pubData;

   public NHPublicKeyParameters(byte[] var1) {
      super(false);
      this.pubData = Arrays.clone(var1);
   }

   public byte[] getPubData() {
      return Arrays.clone(this.pubData);
   }
}
