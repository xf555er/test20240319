package net.jsign.bouncycastle.openssl.jcajce;

import java.security.Provider;
import net.jsign.bouncycastle.jcajce.util.DefaultJcaJceHelper;
import net.jsign.bouncycastle.jcajce.util.JcaJceHelper;
import net.jsign.bouncycastle.jcajce.util.ProviderJcaJceHelper;
import net.jsign.bouncycastle.openssl.PEMDecryptor;
import net.jsign.bouncycastle.openssl.PEMDecryptorProvider;
import net.jsign.bouncycastle.openssl.PEMException;
import net.jsign.bouncycastle.openssl.PasswordException;

public class JcePEMDecryptorProviderBuilder {
   private JcaJceHelper helper = new DefaultJcaJceHelper();

   public JcePEMDecryptorProviderBuilder setProvider(Provider var1) {
      this.helper = new ProviderJcaJceHelper(var1);
      return this;
   }

   public PEMDecryptorProvider build(final char[] var1) {
      return new PEMDecryptorProvider() {
         public PEMDecryptor get(final String var1x) {
            return new PEMDecryptor() {
               public byte[] decrypt(byte[] var1xx, byte[] var2) throws PEMException {
                  if (var1 == null) {
                     throw new PasswordException("Password is null, but a password is required");
                  } else {
                     return PEMUtilities.crypt(false, JcePEMDecryptorProviderBuilder.this.helper, var1xx, var1, var1x, var2);
                  }
               }
            };
         }
      };
   }
}
