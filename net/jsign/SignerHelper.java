package net.jsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.Proxy.Type;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.jsign.bouncycastle.asn1.ASN1InputStream;
import net.jsign.bouncycastle.asn1.cms.ContentInfo;
import net.jsign.bouncycastle.cms.CMSException;
import net.jsign.bouncycastle.cms.CMSProcessable;
import net.jsign.bouncycastle.cms.CMSSignedData;
import net.jsign.commons.io.FileUtils;
import net.jsign.jca.AzureKeyVaultSigningService;
import net.jsign.jca.DigiCertOneSigningService;
import net.jsign.jca.GoogleCloudSigningService;
import net.jsign.jca.SigningServiceJcaProvider;
import net.jsign.timestamp.TimestampingMode;

class SignerHelper {
   private final Console console;
   private final String parameterName;
   private File keystore;
   private String storepass;
   private String storetype;
   private String alias;
   private String keypass;
   private File keyfile;
   private File certfile;
   private String tsaurl;
   private String tsmode;
   private int tsretries = -1;
   private int tsretrywait = -1;
   private String alg;
   private String name;
   private String url;
   private String proxyUrl;
   private String proxyUser;
   private String proxyPass;
   private boolean replace;
   private Charset encoding;
   private boolean detached;
   private AuthenticodeSigner signer;

   public SignerHelper(Console console, String parameterName) {
      this.console = console;
      this.parameterName = parameterName;
   }

   public SignerHelper keystore(String keystore) {
      this.keystore(this.createFile(keystore));
      return this;
   }

   public SignerHelper keystore(File keystore) {
      this.keystore = keystore;
      return this;
   }

   public SignerHelper storepass(String storepass) {
      this.storepass = storepass;
      return this;
   }

   public SignerHelper storetype(String storetype) {
      this.storetype = storetype;
      return this;
   }

   public SignerHelper alias(String alias) {
      this.alias = alias;
      return this;
   }

   public SignerHelper keypass(String keypass) {
      this.keypass = keypass;
      return this;
   }

   public SignerHelper keyfile(String keyfile) {
      this.keyfile(this.createFile(keyfile));
      return this;
   }

   public SignerHelper keyfile(File keyfile) {
      this.keyfile = keyfile;
      return this;
   }

   public SignerHelper certfile(String certfile) {
      this.certfile(this.createFile(certfile));
      return this;
   }

   public SignerHelper certfile(File certfile) {
      this.certfile = certfile;
      return this;
   }

   public SignerHelper alg(String alg) {
      this.alg = alg;
      return this;
   }

   public SignerHelper tsaurl(String tsaurl) {
      this.tsaurl = tsaurl;
      return this;
   }

   public SignerHelper tsmode(String tsmode) {
      this.tsmode = tsmode;
      return this;
   }

   public SignerHelper tsretries(int tsretries) {
      this.tsretries = tsretries;
      return this;
   }

   public SignerHelper tsretrywait(int tsretrywait) {
      this.tsretrywait = tsretrywait;
      return this;
   }

   public SignerHelper name(String name) {
      this.name = name;
      return this;
   }

   public SignerHelper url(String url) {
      this.url = url;
      return this;
   }

   public SignerHelper proxyUrl(String proxyUrl) {
      this.proxyUrl = proxyUrl;
      return this;
   }

   public SignerHelper proxyUser(String proxyUser) {
      this.proxyUser = proxyUser;
      return this;
   }

   public SignerHelper proxyPass(String proxyPass) {
      this.proxyPass = proxyPass;
      return this;
   }

   public SignerHelper replace(boolean replace) {
      this.replace = replace;
      return this;
   }

   public SignerHelper encoding(String encoding) {
      this.encoding = Charset.forName(encoding);
      return this;
   }

   public SignerHelper detached(boolean detached) {
      this.detached = detached;
      return this;
   }

   public SignerHelper param(String key, String value) {
      if (value == null) {
         return this;
      } else {
         switch (key) {
            case "keystore":
               return this.keystore(value);
            case "storepass":
               return this.storepass(value);
            case "storetype":
               return this.storetype(value);
            case "alias":
               return this.alias(value);
            case "keypass":
               return this.keypass(value);
            case "keyfile":
               return this.keyfile(value);
            case "certfile":
               return this.certfile(value);
            case "alg":
               return this.alg(value);
            case "tsaurl":
               return this.tsaurl(value);
            case "tsmode":
               return this.tsmode(value);
            case "tsretries":
               return this.tsretries(Integer.parseInt(value));
            case "tsretrywait":
               return this.tsretrywait(Integer.parseInt(value));
            case "name":
               return this.name(value);
            case "url":
               return this.url(value);
            case "proxyUrl":
               return this.proxyUrl(value);
            case "proxyUser":
               return this.proxyUser(value);
            case "proxyPass":
               return this.proxyPass(value);
            case "replace":
               return this.replace("true".equalsIgnoreCase(value));
            case "encoding":
               return this.encoding(value);
            case "detached":
               return this.detached("true".equalsIgnoreCase(value));
            default:
               throw new IllegalArgumentException("Unknown " + this.parameterName + ": " + key);
         }
      }
   }

   private File createFile(String file) {
      return file == null ? null : new File(file);
   }

   private AuthenticodeSigner build() throws SignerException {
      if (this.keystore == null && this.keyfile == null && this.certfile == null && !"YUBIKEY".equals(this.storetype) && !"DIGICERTONE".equals(this.storetype)) {
         throw new SignerException("keystore " + this.parameterName + ", or keyfile and certfile " + this.parameterName + "s must be set");
      } else if (this.keystore != null && this.keyfile != null) {
         throw new SignerException("keystore " + this.parameterName + " can't be mixed with keyfile");
      } else {
         if ("AZUREKEYVAULT".equals(this.storetype)) {
            if (this.keystore == null) {
               throw new SignerException("keystore " + this.parameterName + " must specify the Azure vault name");
            }

            if (this.storepass == null) {
               throw new SignerException("storepass " + this.parameterName + " must specify the Azure API access token");
            }
         } else if ("DIGICERTONE".equals(this.storetype)) {
            if (this.storepass == null || this.storepass.split("\\|").length != 3) {
               throw new SignerException("storepass " + this.parameterName + " must specify the DigiCert ONE API key and the client certificate: <apikey>|<keystore>|<password>");
            }
         } else if ("GOOGLECLOUD".equals(this.storetype)) {
            if (this.keystore == null) {
               throw new SignerException("keystore " + this.parameterName + " must specify the Goole Cloud keyring");
            }

            if (this.storepass == null) {
               throw new SignerException("storepass " + this.parameterName + " must specify the Goole Cloud API access token");
            }

            if (this.certfile == null) {
               throw new SignerException("certfile " + this.parameterName + " must be set");
            }
         }

         Provider provider = null;
         if ("PKCS11".equals(this.storetype)) {
            if (this.keystore != null && this.keystore.exists()) {
               provider = ProviderUtils.createSunPKCS11Provider(this.keystore.getPath());
            } else {
               if (this.keystore == null || !this.keystore.getName().startsWith("SunPKCS11-")) {
                  throw new SignerException("keystore " + this.parameterName + " should either refer to the SunPKCS11 configuration file or to the name of the provider configured in jre/lib/security/java.security");
               }

               provider = Security.getProvider(this.keystore.getName());
               if (provider == null) {
                  throw new SignerException("Security provider " + this.keystore.getName() + " not found");
               }
            }
         } else if ("YUBIKEY".equals(this.storetype)) {
            provider = YubiKey.getProvider();
         } else if ("AZUREKEYVAULT".equals(this.storetype)) {
            provider = new SigningServiceJcaProvider(new AzureKeyVaultSigningService(this.keystore.getName(), this.storepass));
         } else if ("DIGICERTONE".equals(this.storetype)) {
            String[] elements = this.storepass.split("\\|");
            provider = new SigningServiceJcaProvider(new DigiCertOneSigningService(elements[0], new File(elements[1]), elements[2]));
         } else if ("GOOGLECLOUD".equals(this.storetype)) {
            provider = new SigningServiceJcaProvider(new GoogleCloudSigningService(this.keystore.getPath(), this.storepass, (alias) -> {
               try {
                  return this.loadCertificateChain(this.certfile);
               } catch (CertificateException | IOException var3) {
                  throw new RuntimeException("Failed to load the certificate from " + this.certfile, var3);
               }
            }));
         }

         PrivateKey privateKey;
         Certificate[] chain;
         if (this.keystore == null && !"YUBIKEY".equals(this.storetype) && !"DIGICERTONE".equals(this.storetype)) {
            if (this.keyfile == null) {
               throw new SignerException("keyfile " + this.parameterName + " must be set");
            }

            if (!this.keyfile.exists()) {
               throw new SignerException("The keyfile " + this.keyfile + " couldn't be found");
            }

            if (this.certfile == null) {
               throw new SignerException("certfile " + this.parameterName + " must be set");
            }

            if (!this.certfile.exists()) {
               throw new SignerException("The certfile " + this.certfile + " couldn't be found");
            }

            try {
               chain = this.loadCertificateChain(this.certfile);
            } catch (Exception var11) {
               throw new SignerException("Failed to load the certificate from " + this.certfile, var11);
            }

            try {
               privateKey = PrivateKeyUtils.load(this.keyfile, this.keypass != null ? this.keypass : this.storepass);
            } catch (Exception var10) {
               throw new SignerException("Failed to load the private key from " + this.keyfile, var10);
            }
         } else {
            KeyStore ks;
            try {
               ks = KeyStoreUtils.load(this.keystore, "YUBIKEY".equals(this.storetype) ? "PKCS11" : this.storetype, this.storepass, (Provider)provider);
            } catch (KeyStoreException var17) {
               throw new SignerException("Failed to load the keystore " + this.keystore, var17);
            }

            Set aliases = null;
            if (this.alias == null) {
               if ("YUBIKEY".equals(this.storetype)) {
                  this.alias = "X.509 Certificate for Digital Signature";
               } else {
                  try {
                     aliases = new LinkedHashSet(Collections.list(ks.aliases()));
                  } catch (KeyStoreException var16) {
                     throw new SignerException(var16.getMessage(), var16);
                  }

                  if (aliases.isEmpty()) {
                     throw new SignerException("No certificate found in the keystore " + (provider != null ? ((Provider)provider).getName() : this.keystore));
                  }

                  if (aliases.size() != 1) {
                     throw new SignerException("alias " + this.parameterName + " must be set to select a certificate (available aliases: " + String.join(", ", aliases) + ")");
                  }

                  this.alias = (String)aliases.iterator().next();
               }
            }

            try {
               chain = ks.getCertificateChain(this.alias);
            } catch (KeyStoreException var15) {
               throw new SignerException(var15.getMessage(), var15);
            }

            if (chain == null) {
               String message = "No certificate found under the alias '" + this.alias + "' in the keystore " + (provider != null ? ((Provider)provider).getName() : this.keystore);
               if (aliases == null) {
                  try {
                     aliases = new LinkedHashSet(Collections.list(ks.aliases()));
                     if (aliases.isEmpty()) {
                        message = "No certificate found in the keystore " + (provider != null ? ((Provider)provider).getName() : this.keystore);
                     } else {
                        message = message + " (available aliases: " + String.join(", ", aliases) + ")";
                     }
                  } catch (KeyStoreException var8) {
                     message = message + " (couldn't load the list of available aliases: " + var8.getMessage() + ")";
                  }
               }

               throw new SignerException(message);
            }

            if (this.certfile != null && !"GOOGLECLOUD".equals(this.storetype)) {
               if (chain.length != 1) {
                  throw new SignerException("certfile " + this.parameterName + " can only be specified if the certificate from the keystore contains only one entry");
               }

               try {
                  Certificate[] chainFromFile = this.loadCertificateChain(this.certfile);
                  if (!chainFromFile[0].equals(chain[0])) {
                     throw new SignerException("The certificate chain in " + this.certfile + " does not match the chain from the keystore");
                  }

                  chain = chainFromFile;
               } catch (SignerException var13) {
                  throw var13;
               } catch (Exception var14) {
                  throw new SignerException("Failed to load the certificate from " + this.certfile, var14);
               }
            }

            char[] password = this.keypass != null ? this.keypass.toCharArray() : this.storepass.toCharArray();

            try {
               privateKey = (PrivateKey)ks.getKey(this.alias, password);
            } catch (Exception var12) {
               throw new SignerException("Failed to retrieve the private key from the keystore", var12);
            }
         }

         if (this.alg != null && DigestAlgorithm.of(this.alg) == null) {
            throw new SignerException("The digest algorithm " + this.alg + " is not supported");
         } else {
            try {
               this.initializeProxy(this.proxyUrl, this.proxyUser, this.proxyPass);
            } catch (Exception var9) {
               throw new SignerException("Couldn't initialize proxy", var9);
            }

            return (new AuthenticodeSigner(chain, privateKey)).withProgramName(this.name).withProgramURL(this.url).withDigestAlgorithm(DigestAlgorithm.of(this.alg)).withSignatureProvider((Provider)provider).withSignaturesReplaced(this.replace).withTimestamping(this.tsaurl != null || this.tsmode != null).withTimestampingMode(this.tsmode != null ? TimestampingMode.of(this.tsmode) : TimestampingMode.AUTHENTICODE).withTimestampingRetries(this.tsretries).withTimestampingRetryWait(this.tsretrywait).withTimestampingAuthority(this.tsaurl != null ? this.tsaurl.split(",") : null);
         }
      }
   }

   public void sign(File file) throws SignerException {
      if (file == null) {
         throw new SignerException("file must be set");
      } else if (!file.exists()) {
         throw new SignerException("The file " + file + " couldn't be found");
      } else {
         Signable signable;
         try {
            signable = Signable.of(file, this.encoding);
         } catch (UnsupportedOperationException var7) {
            throw new SignerException(var7.getMessage());
         } catch (IOException var8) {
            throw new SignerException("Couldn't open the file " + file, var8);
         }

         if (this.detached && this.getDetachedSignature(file).exists()) {
            try {
               if (this.console != null) {
                  this.console.info("Attaching Authenticode signature to " + file);
               }

               this.attach(file);
            } catch (Exception var4) {
               throw new SignerException("Couldn't attach the signature to " + file, var4);
            }
         } else {
            try {
               if (this.signer == null) {
                  this.signer = this.build();
               }

               if (this.console != null) {
                  this.console.info("Adding Authenticode signature to " + file);
               }

               this.signer.sign(signable);
               if (this.detached) {
                  this.detach(file);
               }

            } catch (SignerException var5) {
               throw var5;
            } catch (Exception var6) {
               throw new SignerException("Couldn't sign " + file, var6);
            }
         }
      }
   }

   private void attach(File file) throws IOException, CMSException {
      File detachedSignature = this.getDetachedSignature(file);
      byte[] signatureBytes = FileUtils.readFileToByteArray(detachedSignature);
      CMSSignedData signedData = new CMSSignedData((CMSProcessable)null, ContentInfo.getInstance((new ASN1InputStream(signatureBytes)).readObject()));
      Signable signable = Signable.of(file, this.encoding);
      signable.setSignature(signedData);
      signable.save();
   }

   private void detach(File file) throws IOException {
      Signable signable = Signable.of(file, this.encoding);
      CMSSignedData signedData = (CMSSignedData)signable.getSignatures().get(0);
      File detachedSignature = this.getDetachedSignature(file);
      byte[] content = signedData.toASN1Structure().getEncoded("DER");
      FileUtils.writeByteArrayToFile(detachedSignature, content);
   }

   private File getDetachedSignature(File file) {
      return new File(file.getParentFile(), file.getName() + ".sig");
   }

   private Certificate[] loadCertificateChain(File file) throws IOException, CertificateException {
      FileInputStream in = new FileInputStream(file);
      Throwable var3 = null;

      Certificate[] var6;
      try {
         CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
         Collection certificates = certificateFactory.generateCertificates(in);
         var6 = (Certificate[])certificates.toArray(new Certificate[0]);
      } catch (Throwable var15) {
         var3 = var15;
         throw var15;
      } finally {
         if (in != null) {
            if (var3 != null) {
               try {
                  in.close();
               } catch (Throwable var14) {
                  var3.addSuppressed(var14);
               }
            } else {
               in.close();
            }
         }

      }

      return var6;
   }

   private void initializeProxy(String proxyUrl, final String proxyUser, final String proxyPassword) throws MalformedURLException {
      if (proxyUrl != null && proxyUrl.trim().length() > 0) {
         if (!proxyUrl.trim().startsWith("http")) {
            proxyUrl = "http://" + proxyUrl.trim();
         }

         final URL url = new URL(proxyUrl);
         final int port = url.getPort() < 0 ? 80 : url.getPort();
         ProxySelector.setDefault(new ProxySelector() {
            public List select(URI uri) {
               Proxy proxy;
               if (uri.getScheme().equals("socket")) {
                  proxy = new Proxy(Type.SOCKS, new InetSocketAddress(url.getHost(), port));
               } else {
                  proxy = new Proxy(Type.HTTP, new InetSocketAddress(url.getHost(), port));
               }

               if (SignerHelper.this.console != null) {
                  SignerHelper.this.console.debug("Proxy selected for " + uri + " : " + proxy);
               }

               return Collections.singletonList(proxy);
            }

            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            }
         });
         if (proxyUser != null && proxyUser.length() > 0 && proxyPassword != null) {
            Authenticator.setDefault(new Authenticator() {
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
               }
            });
         }
      }

   }
}
