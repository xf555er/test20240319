package net.jsign.bouncycastle.pqc.crypto.qtesla;

import net.jsign.bouncycastle.crypto.params.AsymmetricKeyParameter;
import net.jsign.bouncycastle.util.Arrays;

public final class QTESLAPublicKeyParameters extends AsymmetricKeyParameter {
   private int securityCategory;
   private byte[] publicKey;

   public QTESLAPublicKeyParameters(int var1, byte[] var2) {
      super(false);
      if (var2.length != QTESLASecurityCategory.getPublicSize(var1)) {
         throw new IllegalArgumentException("invalid key size for security category");
      } else {
         this.securityCategory = var1;
         this.publicKey = Arrays.clone(var2);
      }
   }

   public int getSecurityCategory() {
      return this.securityCategory;
   }

   public byte[] getPublicData() {
      return Arrays.clone(this.publicKey);
   }
}
