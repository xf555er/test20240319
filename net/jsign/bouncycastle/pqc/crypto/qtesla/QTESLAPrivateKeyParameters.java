package net.jsign.bouncycastle.pqc.crypto.qtesla;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.util.Arrays;

public final class QTESLAPrivateKeyParameters extends AsymmetricKeyParameter {
   private int securityCategory;
   private byte[] privateKey;

   public QTESLAPrivateKeyParameters(int var1, byte[] var2) {
      super(true);
      if (var2.length != QTESLASecurityCategory.getPrivateSize(var1)) {
         throw new IllegalArgumentException("invalid key size for security category");
      } else {
         this.securityCategory = var1;
         this.privateKey = Arrays.clone(var2);
      }
   }

   public int getSecurityCategory() {
      return this.securityCategory;
   }

   public byte[] getSecret() {
      return Arrays.clone(this.privateKey);
   }
}
