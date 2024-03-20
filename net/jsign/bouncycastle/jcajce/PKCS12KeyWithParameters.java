package net.jsign.bouncycastle.jcajce;

import javax.crypto.interfaces.PBEKey;
import net.jsign.bouncycastle.util.Arrays;

public class PKCS12KeyWithParameters extends PKCS12Key implements PBEKey {
   private final byte[] salt;
   private final int iterationCount;

   public PKCS12KeyWithParameters(char[] var1, byte[] var2, int var3) {
      super(var1);
      this.salt = Arrays.clone(var2);
      this.iterationCount = var3;
   }
}
