package net.jsign.bouncycastle.jce.provider;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import net.jsign.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import net.jsign.bouncycastle.jcajce.provider.symmetric.util.ClassUtil;
import net.jsign.bouncycastle.jcajce.provider.util.AlgorithmProvider;
import net.jsign.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.jcajce.provider.lms.LMSKeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.mceliece.McElieceKeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.newhope.NHKeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.qtesla.QTESLAKeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.rainbow.RainbowKeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.sphincs.Sphincs256KeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.xmss.XMSSKeyFactorySpi;
import net.jsign.bouncycastle.pqc.jcajce.provider.xmss.XMSSMTKeyFactorySpi;

public final class BouncyCastleProvider extends Provider implements ConfigurableProvider {
   private static String info = "BouncyCastle Security Provider v1.69";
   public static final ProviderConfiguration CONFIGURATION = new BouncyCastleProviderConfiguration();
   private static final Map keyInfoConverters = new HashMap();
   private static final Class revChkClass = ClassUtil.loadClass(BouncyCastleProvider.class, "java.security.cert.PKIXRevocationChecker");
   private static final String[] SYMMETRIC_GENERIC = new String[]{"PBEPBKDF1", "PBEPBKDF2", "PBEPKCS12", "TLSKDF", "SCRYPT"};
   private static final String[] SYMMETRIC_MACS = new String[]{"SipHash", "SipHash128", "Poly1305"};
   private static final String[] SYMMETRIC_CIPHERS = new String[]{"AES", "ARC4", "ARIA", "Blowfish", "Camellia", "CAST5", "CAST6", "ChaCha", "DES", "DESede", "GOST28147", "Grainv1", "Grain128", "HC128", "HC256", "IDEA", "Noekeon", "RC2", "RC5", "RC6", "Rijndael", "Salsa20", "SEED", "Serpent", "Shacal2", "Skipjack", "SM4", "TEA", "Twofish", "Threefish", "VMPC", "VMPCKSA3", "XTEA", "XSalsa20", "OpenSSLPBKDF", "DSTU7624", "GOST3412_2015", "Zuc"};
   private static final String[] ASYMMETRIC_GENERIC = new String[]{"X509", "IES", "COMPOSITE"};
   private static final String[] ASYMMETRIC_CIPHERS = new String[]{"DSA", "DH", "EC", "RSA", "GOST", "ECGOST", "ElGamal", "DSTU4145", "GM", "EdEC"};
   private static final String[] DIGESTS = new String[]{"GOST3411", "Keccak", "MD2", "MD4", "MD5", "SHA1", "RIPEMD128", "RIPEMD160", "RIPEMD256", "RIPEMD320", "SHA224", "SHA256", "SHA384", "SHA512", "SHA3", "Skein", "SM3", "Tiger", "Whirlpool", "Blake2b", "Blake2s", "DSTU7564", "Haraka"};
   private static final String[] KEYSTORES = new String[]{"BC", "BCFKS", "PKCS12"};
   private static final String[] SECURE_RANDOMS = new String[]{"DRBG"};

   public BouncyCastleProvider() {
      super("BC", 1.69, info);
      AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            BouncyCastleProvider.this.setup();
            return null;
         }
      });
   }

   private void setup() {
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.digest.", DIGESTS);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.symmetric.", SYMMETRIC_GENERIC);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.symmetric.", SYMMETRIC_MACS);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.symmetric.", SYMMETRIC_CIPHERS);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.asymmetric.", ASYMMETRIC_GENERIC);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.asymmetric.", ASYMMETRIC_CIPHERS);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.keystore.", KEYSTORES);
      this.loadAlgorithms("net.jsign.bouncycastle.jcajce.provider.drbg.", SECURE_RANDOMS);
      this.loadPQCKeys();
      this.put("X509Store.CERTIFICATE/COLLECTION", "net.jsign.bouncycastle.jce.provider.X509StoreCertCollection");
      this.put("X509Store.ATTRIBUTECERTIFICATE/COLLECTION", "net.jsign.bouncycastle.jce.provider.X509StoreAttrCertCollection");
      this.put("X509Store.CRL/COLLECTION", "net.jsign.bouncycastle.jce.provider.X509StoreCRLCollection");
      this.put("X509Store.CERTIFICATEPAIR/COLLECTION", "net.jsign.bouncycastle.jce.provider.X509StoreCertPairCollection");
      this.put("X509Store.CERTIFICATE/LDAP", "net.jsign.bouncycastle.jce.provider.X509StoreLDAPCerts");
      this.put("X509Store.CRL/LDAP", "net.jsign.bouncycastle.jce.provider.X509StoreLDAPCRLs");
      this.put("X509Store.ATTRIBUTECERTIFICATE/LDAP", "net.jsign.bouncycastle.jce.provider.X509StoreLDAPAttrCerts");
      this.put("X509Store.CERTIFICATEPAIR/LDAP", "net.jsign.bouncycastle.jce.provider.X509StoreLDAPCertPairs");
      this.put("X509StreamParser.CERTIFICATE", "net.jsign.bouncycastle.jce.provider.X509CertParser");
      this.put("X509StreamParser.ATTRIBUTECERTIFICATE", "net.jsign.bouncycastle.jce.provider.X509AttrCertParser");
      this.put("X509StreamParser.CRL", "net.jsign.bouncycastle.jce.provider.X509CRLParser");
      this.put("X509StreamParser.CERTIFICATEPAIR", "net.jsign.bouncycastle.jce.provider.X509CertPairParser");
      this.put("Cipher.BROKENPBEWITHMD5ANDDES", "net.jsign.bouncycastle.jce.provider.BrokenJCEBlockCipher$BrokePBEWithMD5AndDES");
      this.put("Cipher.BROKENPBEWITHSHA1ANDDES", "net.jsign.bouncycastle.jce.provider.BrokenJCEBlockCipher$BrokePBEWithSHA1AndDES");
      this.put("Cipher.OLDPBEWITHSHAANDTWOFISH-CBC", "net.jsign.bouncycastle.jce.provider.BrokenJCEBlockCipher$OldPBEWithSHAAndTwofish");
      if (revChkClass != null) {
         this.put("CertPathValidator.RFC3281", "net.jsign.bouncycastle.jce.provider.PKIXAttrCertPathValidatorSpi");
         this.put("CertPathBuilder.RFC3281", "net.jsign.bouncycastle.jce.provider.PKIXAttrCertPathBuilderSpi");
         this.put("CertPathValidator.RFC3280", "net.jsign.bouncycastle.jce.provider.PKIXCertPathValidatorSpi_8");
         this.put("CertPathBuilder.RFC3280", "net.jsign.bouncycastle.jce.provider.PKIXCertPathBuilderSpi_8");
         this.put("CertPathValidator.PKIX", "net.jsign.bouncycastle.jce.provider.PKIXCertPathValidatorSpi_8");
         this.put("CertPathBuilder.PKIX", "net.jsign.bouncycastle.jce.provider.PKIXCertPathBuilderSpi_8");
      } else {
         this.put("CertPathValidator.RFC3281", "net.jsign.bouncycastle.jce.provider.PKIXAttrCertPathValidatorSpi");
         this.put("CertPathBuilder.RFC3281", "net.jsign.bouncycastle.jce.provider.PKIXAttrCertPathBuilderSpi");
         this.put("CertPathValidator.RFC3280", "net.jsign.bouncycastle.jce.provider.PKIXCertPathValidatorSpi");
         this.put("CertPathBuilder.RFC3280", "net.jsign.bouncycastle.jce.provider.PKIXCertPathBuilderSpi");
         this.put("CertPathValidator.PKIX", "net.jsign.bouncycastle.jce.provider.PKIXCertPathValidatorSpi");
         this.put("CertPathBuilder.PKIX", "net.jsign.bouncycastle.jce.provider.PKIXCertPathBuilderSpi");
      }

      this.put("CertStore.Collection", "net.jsign.bouncycastle.jce.provider.CertStoreCollectionSpi");
      this.put("CertStore.LDAP", "net.jsign.bouncycastle.jce.provider.X509LDAPCertStoreSpi");
      this.put("CertStore.Multi", "net.jsign.bouncycastle.jce.provider.MultiCertStoreSpi");
      this.put("Alg.Alias.CertStore.X509LDAP", "LDAP");
   }

   private void loadAlgorithms(String var1, String[] var2) {
      for(int var3 = 0; var3 != var2.length; ++var3) {
         Class var4 = ClassUtil.loadClass(BouncyCastleProvider.class, var1 + var2[var3] + "$Mappings");
         if (var4 != null) {
            try {
               ((AlgorithmProvider)var4.newInstance()).configure(this);
            } catch (Exception var6) {
               throw new InternalError("cannot create instance of " + var1 + var2[var3] + "$Mappings : " + var6);
            }
         }
      }

   }

   private void loadPQCKeys() {
      this.addKeyInfoConverter(PQCObjectIdentifiers.sphincs256, new Sphincs256KeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.newHope, new NHKeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.xmss, new XMSSKeyFactorySpi());
      this.addKeyInfoConverter(IsaraObjectIdentifiers.id_alg_xmss, new XMSSKeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyFactorySpi());
      this.addKeyInfoConverter(IsaraObjectIdentifiers.id_alg_xmssmt, new XMSSMTKeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.mcEliece, new McElieceKeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.mcElieceCca2, new McElieceCCA2KeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.rainbow, new RainbowKeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.qTESLA_p_I, new QTESLAKeyFactorySpi());
      this.addKeyInfoConverter(PQCObjectIdentifiers.qTESLA_p_III, new QTESLAKeyFactorySpi());
      this.addKeyInfoConverter(PKCSObjectIdentifiers.id_alg_hss_lms_hashsig, new LMSKeyFactorySpi());
   }

   public void addKeyInfoConverter(ASN1ObjectIdentifier var1, AsymmetricKeyInfoConverter var2) {
      synchronized(keyInfoConverters) {
         keyInfoConverters.put(var1, var2);
      }
   }
}
