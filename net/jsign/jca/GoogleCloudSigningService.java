package net.jsign.jca;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.jsign.DigestAlgorithm;
import net.jsign.json-io.util.io.JsonWriter;

public class GoogleCloudSigningService implements SigningService {
   private final String keyring;
   private final Function certificateStore;
   private final Map keys = new HashMap();
   private final RESTClient client;

   public GoogleCloudSigningService(String keyring, String token, Function certificateStore) {
      this.keyring = keyring;
      this.certificateStore = certificateStore;
      this.client = new RESTClient("https://cloudkms.googleapis.com/v1/", (conn) -> {
         conn.setRequestProperty("Authorization", "Bearer " + token);
      });
   }

   public String getName() {
      return "GoogleCloud";
   }

   public List aliases() throws KeyStoreException {
      List aliases = new ArrayList();

      try {
         Map response = this.client.get(this.keyring + "/cryptoKeys");
         Object[] cryptoKeys = (Object[])((Object[])response.get("cryptoKeys"));
         Object[] var4 = cryptoKeys;
         int var5 = cryptoKeys.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Object cryptoKey = var4[var6];
            String name = (String)((Map)cryptoKey).get("name");
            aliases.add(name.substring(name.lastIndexOf("/") + 1));
         }

         return aliases;
      } catch (IOException var9) {
         throw new KeyStoreException(var9);
      }
   }

   public Certificate[] getCertificateChain(String alias) {
      return (Certificate[])this.certificateStore.apply(alias);
   }

   public SigningServicePrivateKey getPrivateKey(String alias) throws UnrecoverableKeyException {
      if (!alias.startsWith("projects/")) {
         alias = this.keyring + "/cryptoKeys/" + alias;
      }

      if (this.keys.containsKey(alias)) {
         return (SigningServicePrivateKey)this.keys.get(alias);
      } else {
         String algorithm;
         try {
            Map response;
            if (alias.contains("cryptoKeyVersions")) {
               if (alias.contains(":")) {
                  algorithm = alias.substring(alias.indexOf(58) + 1) + "_SIGN";
                  alias = alias.substring(0, alias.indexOf(58));
               } else {
                  response = this.client.get(alias);
                  algorithm = (String)response.get("algorithm");
               }
            } else {
               response = this.client.get(alias + "/cryptoKeyVersions?filter=state%3DENABLED");
               Object[] cryptoKeyVersions = (Object[])((Object[])response.get("cryptoKeyVersions"));
               if (cryptoKeyVersions == null || cryptoKeyVersions.length == 0) {
                  throw new UnrecoverableKeyException("Unable to fetch Google Cloud private key '" + alias + "', no version found");
               }

               Map cryptoKeyVersion = (Map)cryptoKeyVersions[cryptoKeyVersions.length - 1];
               alias = (String)cryptoKeyVersion.get("name");
               algorithm = (String)cryptoKeyVersion.get("algorithm");
            }
         } catch (IOException var6) {
            throw (UnrecoverableKeyException)(new UnrecoverableKeyException("Unable to fetch Google Cloud private key '" + alias + "'")).initCause(var6);
         }

         algorithm = algorithm.substring(0, algorithm.indexOf("_"));
         SigningServicePrivateKey key = new SigningServicePrivateKey(alias, algorithm);
         this.keys.put(alias, key);
         return key;
      }
   }

   public byte[] sign(SigningServicePrivateKey privateKey, String algorithm, byte[] data) throws GeneralSecurityException {
      DigestAlgorithm digestAlgorithm = DigestAlgorithm.of(algorithm.substring(0, algorithm.toLowerCase().indexOf("with")));
      data = digestAlgorithm.getMessageDigest().digest(data);
      Map digest = new HashMap();
      digest.put(digestAlgorithm.name().toLowerCase(), Base64.getEncoder().encodeToString(data));
      Map request = new HashMap();
      request.put("digest", digest);

      try {
         Map args = new HashMap();
         args.put("TYPE", "false");
         Map response = this.client.post(privateKey.getId() + ":asymmetricSign", JsonWriter.objectToJson(request, args));
         String signature = (String)response.get("signature");
         return Base64.getDecoder().decode(signature);
      } catch (IOException var10) {
         throw new GeneralSecurityException(var10);
      }
   }
}
