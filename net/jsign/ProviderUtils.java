package net.jsign;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.ProviderException;
import java.security.Security;

class ProviderUtils {
   static Provider createSunPKCS11Provider(String configuration) {
      try {
         try {
            Method providerConfigureMethod = Provider.class.getMethod("configure", String.class);
            Provider provider = Security.getProvider("SunPKCS11");
            return (Provider)providerConfigureMethod.invoke(provider, configuration);
         } catch (NoSuchMethodException var3) {
            Constructor sunpkcs11Constructor = Class.forName("sun.security.pkcs11.SunPKCS11").getConstructor(String.class);
            return (Provider)sunpkcs11Constructor.newInstance(configuration);
         }
      } catch (Exception var4) {
         throw new ProviderException("Failed to create a SunPKCS11 provider from the configuration " + configuration, var4);
      }
   }
}
