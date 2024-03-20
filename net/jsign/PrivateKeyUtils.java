package net.jsign;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyException;
import java.security.PrivateKey;
import net.jsign.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import net.jsign.bouncycastle.jce.provider.BouncyCastleProvider;
import net.jsign.bouncycastle.openssl.PEMDecryptorProvider;
import net.jsign.bouncycastle.openssl.PEMEncryptedKeyPair;
import net.jsign.bouncycastle.openssl.PEMKeyPair;
import net.jsign.bouncycastle.openssl.PEMParser;
import net.jsign.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import net.jsign.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import net.jsign.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import net.jsign.bouncycastle.operator.InputDecryptorProvider;
import net.jsign.bouncycastle.operator.OperatorCreationException;
import net.jsign.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import net.jsign.bouncycastle.pkcs.PKCSException;

public class PrivateKeyUtils {
   private PrivateKeyUtils() {
   }

   public static PrivateKey load(File file, String password) throws KeyException {
      try {
         if (file.getName().endsWith(".pvk")) {
            return PVK.parse(file, password);
         }

         if (file.getName().endsWith(".pem")) {
            return readPrivateKeyPEM(file, password);
         }
      } catch (Exception var3) {
         throw new KeyException("Failed to load the private key from " + file, var3);
      }

      throw new IllegalArgumentException("Unsupported private key format (PEM or PVK file expected");
   }

   private static PrivateKey readPrivateKeyPEM(File file, String password) throws IOException, OperatorCreationException, PKCSException {
      FileReader reader = new FileReader(file);
      Throwable var3 = null;

      PrivateKey var8;
      try {
         PEMParser parser = new PEMParser(reader);
         Object object = parser.readObject();
         if (object == null) {
            throw new IllegalArgumentException("No key found in " + file);
         }

         BouncyCastleProvider provider = new BouncyCastleProvider();
         JcaPEMKeyConverter converter = (new JcaPEMKeyConverter()).setProvider(provider);
         PrivateKey var10;
         if (object instanceof PEMEncryptedKeyPair) {
            PEMDecryptorProvider decryptionProvider = (new JcePEMDecryptorProviderBuilder()).setProvider(provider).build(password.toCharArray());
            PEMKeyPair keypair = ((PEMEncryptedKeyPair)object).decryptKeyPair(decryptionProvider);
            var10 = converter.getPrivateKey(keypair.getPrivateKeyInfo());
            return var10;
         }

         if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
            InputDecryptorProvider decryptionProvider = (new JceOpenSSLPKCS8DecryptorProviderBuilder()).setProvider(provider).build(password.toCharArray());
            PrivateKeyInfo info = ((PKCS8EncryptedPrivateKeyInfo)object).decryptPrivateKeyInfo(decryptionProvider);
            var10 = converter.getPrivateKey(info);
            return var10;
         }

         if (!(object instanceof PEMKeyPair)) {
            if (object instanceof PrivateKeyInfo) {
               var8 = converter.getPrivateKey((PrivateKeyInfo)object);
               return var8;
            }

            throw new UnsupportedOperationException("Unsupported PEM object: " + object.getClass().getSimpleName());
         }

         var8 = converter.getKeyPair((PEMKeyPair)object).getPrivate();
      } catch (Throwable var22) {
         var3 = var22;
         throw var22;
      } finally {
         if (reader != null) {
            if (var3 != null) {
               try {
                  reader.close();
               } catch (Throwable var21) {
                  var3.addSuppressed(var21);
               }
            } else {
               reader.close();
            }
         }

      }

      return var8;
   }
}
