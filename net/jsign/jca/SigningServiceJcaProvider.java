package net.jsign.jca;

import java.security.AccessController;
import java.security.Provider;
import java.util.Collections;
import java.util.Map;
import net.jsign.DigestAlgorithm;

public class SigningServiceJcaProvider extends Provider {
   private final SigningService service;

   public SigningServiceJcaProvider(SigningService service) {
      super(service.getName(), 1.0, service.getName() + " signing service provider");
      this.service = service;
      AccessController.doPrivileged(() -> {
         this.putService(new KeyStoreProviderService());
         String[] var1 = new String[]{"RSA", "ECDSA"};
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String alg = var1[var3];
            DigestAlgorithm[] var5 = DigestAlgorithm.values();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               DigestAlgorithm digest = var5[var7];
               if (digest != DigestAlgorithm.MD5) {
                  this.putService(new SignatureProviderService(digest.name() + "with" + alg));
               }
            }
         }

         return null;
      });
   }

   private class SignatureProviderService extends Provider.Service {
      private final String signingAlgorithm;

      public SignatureProviderService(String signingAlgorithm) {
         super(SigningServiceJcaProvider.this, "Signature", signingAlgorithm, SigningServiceSignature.class.getName(), Collections.emptyList(), Collections.emptyMap());
         this.signingAlgorithm = signingAlgorithm;
      }

      public Object newInstance(Object constructorParameter) {
         return new SigningServiceSignature(SigningServiceJcaProvider.this.service, this.signingAlgorithm);
      }
   }

   private class KeyStoreProviderService extends Provider.Service {
      public KeyStoreProviderService() {
         super(SigningServiceJcaProvider.this, "KeyStore", SigningServiceJcaProvider.this.service.getName().toUpperCase(), SigningServiceKeyStore.class.getName(), Collections.emptyList(), (Map)null);
      }

      public Object newInstance(Object constructorParameter) {
         return new SigningServiceKeyStore(SigningServiceJcaProvider.this.service);
      }
   }
}
