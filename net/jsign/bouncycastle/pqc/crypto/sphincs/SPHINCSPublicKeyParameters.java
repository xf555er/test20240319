package net.jsign.bouncycastle.pqc.crypto.sphincs;

import net.jsign.bouncycastle.util.Arrays;

public class SPHINCSPublicKeyParameters extends SPHINCSKeyParameters {
   private final byte[] keyData;

   public SPHINCSPublicKeyParameters(byte[] var1, String var2) {
      super(false, var2);
      this.keyData = Arrays.clone(var1);
   }

   public byte[] getKeyData() {
      return Arrays.clone(this.keyData);
   }
}
