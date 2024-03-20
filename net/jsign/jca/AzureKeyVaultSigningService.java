package net.jsign.jca;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.jsign.DigestAlgorithm;
import net.jsign.json-io.util.io.JsonWriter;

public class AzureKeyVaultSigningService implements SigningService {
   private final Map certificates = new HashMap();
   private final RESTClient client;
   private final Map algorithmMapping = new HashMap();

   public AzureKeyVaultSigningService(String vault, String token) {
      this.algorithmMapping.put("SHA256withRSA", "RS256");
      this.algorithmMapping.put("SHA384withRSA", "RS384");
      this.algorithmMapping.put("SHA512withRSA", "RS512");
      this.algorithmMapping.put("SHA256withECDSA", "ES256");
      this.algorithmMapping.put("SHA384withECDSA", "ES384");
      this.algorithmMapping.put("SHA512withECDSA", "ES512");
      this.algorithmMapping.put("SHA256withRSA/PSS", "PS256");
      this.algorithmMapping.put("SHA384withRSA/PSS", "PS384");
      this.algorithmMapping.put("SHA512withRSA/PSS", "PS512");
      if (!vault.startsWith("http")) {
         vault = "https://" + vault + ".vault.azure.net";
      }

      this.client = new RESTClient(vault, (conn) -> {
         conn.setRequestProperty("Authorization", "Bearer " + token);
      });
   }

   public String getName() {
      return "AzureKeyVault";
   }

   private Map getCertificateInfo(String alias) throws IOException {
      if (!this.certificates.containsKey(alias)) {
         Map response = this.client.get("/certificates/" + alias + "?api-version=7.2");
         this.certificates.put(alias, response);
      }

      return (Map)this.certificates.get(alias);
   }

   public List aliases() throws KeyStoreException {
      List aliases = new ArrayList();

      try {
         Map response = this.client.get("/certificates?api-version=7.2");
         Object[] certificates = (Object[])((Object[])response.get("value"));
         Object[] var4 = certificates;
         int var5 = certificates.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Object certificate = var4[var6];
            String id = (String)((Map)certificate).get("id");
            aliases.add(id.substring(id.lastIndexOf(47) + 1));
         }

         return aliases;
      } catch (IOException var9) {
         throw new KeyStoreException("Unable to retrieve Azure Key Vault certificate aliases", var9);
      }
   }

   public Certificate[] getCertificateChain(String alias) throws KeyStoreException {
      try {
         Map response = this.getCertificateInfo(alias);
         String pem = (String)response.get("cer");
         Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(pem)));
         return new Certificate[]{certificate};
      } catch (CertificateException | IOException var5) {
         throw new KeyStoreException("Unable to retrieve Azure Key Vault certificate '" + alias + "'", var5);
      }
   }

   public SigningServicePrivateKey getPrivateKey(String alias) throws UnrecoverableKeyException {
      try {
         Map response = this.getCertificateInfo(alias);
         String kid = (String)response.get("kid");
         Map policy = (Map)response.get("policy");
         Map keyprops = (Map)policy.get("key_props");
         String algorithm = ((String)keyprops.get("kty")).replace("-HSM", "");
         return new SigningServicePrivateKey(kid, algorithm);
      } catch (IOException var7) {
         throw (UnrecoverableKeyException)(new UnrecoverableKeyException("Unable to fetch Azure Key Vault private key for the certificate '" + alias + "'")).initCause(var7);
      }
   }

   public byte[] sign(SigningServicePrivateKey privateKey, String algorithm, byte[] data) throws GeneralSecurityException {
      String alg = (String)this.algorithmMapping.get(algorithm);
      if (alg == null) {
         throw new InvalidAlgorithmParameterException("Unsupported signing algorithm: " + algorithm);
      } else {
         MessageDigest digest = DigestAlgorithm.of(algorithm.substring(0, algorithm.toLowerCase().indexOf("with"))).getMessageDigest();
         data = digest.digest(data);
         Map request = new HashMap();
         request.put("alg", alg);
         request.put("value", Base64.getEncoder().encodeToString(data));

         try {
            Map args = new HashMap();
            args.put("TYPE", "false");
            Map response = this.client.post(privateKey.getId() + "/sign?api-version=7.2", JsonWriter.objectToJson(request, args));
            String value = (String)response.get("value");
            return Base64.getUrlDecoder().decode(value);
         } catch (IOException var10) {
            throw new GeneralSecurityException(var10);
         }
      }
   }
}
