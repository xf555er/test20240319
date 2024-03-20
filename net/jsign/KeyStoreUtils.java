package net.jsign;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;

public class KeyStoreUtils {
   private KeyStoreUtils() {
   }

   public static KeyStore load(File keystore, String storetype, String storepass, Provider provider) throws KeyStoreException {
      if (keystore != null && storetype == null) {
         String filename = keystore.getName().toLowerCase();
         if (!filename.endsWith(".p12") && !filename.endsWith(".pfx")) {
            if (filename.endsWith(".jceks")) {
               storetype = "JCEKS";
            } else {
               storetype = "JKS";
            }
         } else {
            storetype = "PKCS12";
         }
      }

      KeyStore ks;
      try {
         if (provider != null) {
            ks = KeyStore.getInstance(storetype, provider);
         } else {
            ks = KeyStore.getInstance(storetype);
         }
      } catch (KeyStoreException var19) {
         throw new KeyStoreException("keystore type '" + storetype + "' is not supported", var19);
      }

      boolean filebased = "JKS".equals(storetype) || "JCEKS".equals(storetype) || "PKCS12".equals(storetype);
      if (filebased && (keystore == null || !keystore.exists())) {
         throw new KeyStoreException("The keystore " + keystore + " couldn't be found");
      } else {
         try {
            FileInputStream in = !filebased ? null : new FileInputStream(keystore);
            Throwable var7 = null;

            try {
               ks.load(in, storepass != null ? storepass.toCharArray() : null);
            } catch (Throwable var18) {
               var7 = var18;
               throw var18;
            } finally {
               if (in != null) {
                  if (var7 != null) {
                     try {
                        in.close();
                     } catch (Throwable var17) {
                        var7.addSuppressed(var17);
                     }
                  } else {
                     in.close();
                  }
               }

            }

            return ks;
         } catch (Exception var21) {
            throw new KeyStoreException("Unable to load the keystore " + keystore, var21);
         }
      }
   }
}
