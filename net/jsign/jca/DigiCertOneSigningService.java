package net.jsign.jca;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import net.jsign.DigestAlgorithm;
import net.jsign.KeyStoreUtils;
import net.jsign.json-io.util.io.JsonWriter;

public class DigiCertOneSigningService implements SigningService {
   private final Map certificates;
   private final RESTClient client;
   private static final Pattern ID_PATTERN = Pattern.compile("[0-9a-f\\-]+");

   public DigiCertOneSigningService(String apiKey, File keystore, String storepass) {
      this(apiKey, (X509KeyManager)getKeyManager(keystore, storepass));
   }

   public DigiCertOneSigningService(String apiKey, X509KeyManager keyManager) {
      this.certificates = new HashMap();
      this.client = new RESTClient("https://one.digicert.com/signingmanager/api/v1/", (conn) -> {
         conn.setRequestProperty("x-api-key", apiKey);

         try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(new KeyManager[]{keyManager}, (TrustManager[])null, new SecureRandom());
            ((HttpsURLConnection)conn).setSSLSocketFactory(context.getSocketFactory());
         } catch (GeneralSecurityException var4) {
            throw new RuntimeException("Unable to load the DigiCert ONE client certificate", var4);
         }
      });
   }

   public String getName() {
      return "DigiCertONE";
   }

   private Map getCertificateInfo(String alias) throws IOException {
      if (!this.certificates.containsKey(alias)) {
         Map response = this.client.get("certificates?" + (this.isIdentifier(alias) ? "id" : "alias") + "=" + alias);
         Object[] var3 = (Object[])((Object[])response.get("items"));
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Object item = var3[var5];
            Map certificate = (Map)item;
            this.certificates.put((String)certificate.get("id"), certificate);
            this.certificates.put((String)certificate.get("alias"), certificate);
         }
      }

      return (Map)this.certificates.get(alias);
   }

   private boolean isIdentifier(String id) {
      return ID_PATTERN.matcher(id).matches();
   }

   public List aliases() throws KeyStoreException {
      List aliases = new ArrayList();

      try {
         Map response = this.client.get("certificates?limit=100&certificate_status=ACTIVE");
         Object[] var3 = (Object[])((Object[])response.get("items"));
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Object item = var3[var5];
            Map certificate = (Map)item;
            this.certificates.put((String)certificate.get("id"), certificate);
            this.certificates.put((String)certificate.get("alias"), certificate);
            aliases.add((String)certificate.get("alias"));
         }

         return aliases;
      } catch (IOException var8) {
         throw new KeyStoreException("Unable to retrieve DigiCert ONE certificate aliases", var8);
      }
   }

   public Certificate[] getCertificateChain(String alias) throws KeyStoreException {
      try {
         Map response = this.getCertificateInfo(alias);
         if (response == null) {
            throw new KeyStoreException("Unable to retrieve DigiCert ONE certificate '" + alias + "'");
         } else {
            List encodedChain = new ArrayList();
            encodedChain.add((String)response.get("cert"));
            if (response.get("chain") != null) {
               Object[] var4 = (Object[])((Object[])response.get("chain"));
               int var5 = var4.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  Object certificate = var4[var6];
                  encodedChain.add(((Map)certificate).get("blob"));
               }
            }

            List chain = new ArrayList();
            Iterator var10 = encodedChain.iterator();

            while(var10.hasNext()) {
               String encodedCertificate = (String)var10.next();
               chain.add(CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(encodedCertificate))));
            }

            return (Certificate[])chain.toArray(new Certificate[0]);
         }
      } catch (CertificateException | IOException var8) {
         throw new KeyStoreException("Unable to retrieve DigiCert ONE certificate '" + alias + "'", var8);
      }
   }

   public SigningServicePrivateKey getPrivateKey(String alias) throws UnrecoverableKeyException {
      try {
         Map certificate = this.getCertificateInfo(alias);
         Map keypair = (Map)certificate.get("keypair");
         String keyId = (String)keypair.get("id");
         Map response = this.client.get("/keypairs/" + keyId);
         String algorithm = (String)response.get("key_alg");
         SigningServicePrivateKey key = new SigningServicePrivateKey(keyId, algorithm);
         key.getProperties().put("account", response.get("account"));
         return key;
      } catch (IOException var8) {
         throw (UnrecoverableKeyException)(new UnrecoverableKeyException("Unable to fetch DigiCert ONE private key for the certificate '" + alias + "'")).initCause(var8);
      }
   }

   public byte[] sign(SigningServicePrivateKey privateKey, String algorithm, byte[] data) throws GeneralSecurityException {
      DigestAlgorithm digestAlgorithm = DigestAlgorithm.of(algorithm.substring(0, algorithm.toLowerCase().indexOf("with")));
      data = digestAlgorithm.getMessageDigest().digest(data);
      Map request = new HashMap();
      request.put("account", privateKey.getProperties().get("account"));
      request.put("sig_alg", algorithm);
      request.put("hash", Base64.getEncoder().encodeToString(data));

      try {
         Map args = new HashMap();
         args.put("TYPE", "false");
         Map response = this.client.post("https://clientauth.one.digicert.com/signingmanager/api/v1/keypairs/" + privateKey.getId() + "/sign", JsonWriter.objectToJson(request, args));
         String value = (String)response.get("signature");
         return Base64.getDecoder().decode(value);
      } catch (IOException var9) {
         throw new GeneralSecurityException(var9);
      }
   }

   private static KeyManager getKeyManager(File keystoreFile, String storepass) {
      try {
         KeyStore keystore = KeyStoreUtils.load(keystoreFile, (String)null, storepass, (Provider)null);
         KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
         kmf.init(keystore, storepass.toCharArray());
         return kmf.getKeyManagers()[0];
      } catch (Exception var4) {
         throw new RuntimeException("Failed to load the client certificate for DigiCert ONE", var4);
      }
   }
}
