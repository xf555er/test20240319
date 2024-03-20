package net.jsign.bouncycastle.cms;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Primitive;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.ASN1Set;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import net.jsign.bouncycastle.asn1.eac.EACObjectIdentifiers;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import net.jsign.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x509.Certificate;
import net.jsign.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import net.jsign.bouncycastle.cert.X509CertificateHolder;
import net.jsign.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import net.jsign.bouncycastle.util.CollectionStore;
import net.jsign.bouncycastle.util.Store;

class CMSSignedHelper {
   static final CMSSignedHelper INSTANCE = new CMSSignedHelper();
   private static final Map encryptionAlgs = new HashMap();

   private static void addEntries(ASN1ObjectIdentifier var0, String var1) {
      encryptionAlgs.put(var0.getId(), var1);
   }

   String getEncryptionAlgName(String var1) {
      String var2 = (String)encryptionAlgs.get(var1);
      return var2 != null ? var2 : var1;
   }

   AlgorithmIdentifier fixDigestAlgID(AlgorithmIdentifier var1, DigestAlgorithmIdentifierFinder var2) {
      ASN1Encodable var3 = var1.getParameters();
      return var3 != null && !DERNull.INSTANCE.equals(var3) ? var1 : var2.find(var1.getAlgorithm());
   }

   Store getCertificates(ASN1Set var1) {
      if (var1 != null) {
         ArrayList var2 = new ArrayList(var1.size());
         Enumeration var3 = var1.getObjects();

         while(var3.hasMoreElements()) {
            ASN1Primitive var4 = ((ASN1Encodable)var3.nextElement()).toASN1Primitive();
            if (var4 instanceof ASN1Sequence) {
               var2.add(new X509CertificateHolder(Certificate.getInstance(var4)));
            }
         }

         return new CollectionStore(var2);
      } else {
         return new CollectionStore(new ArrayList());
      }
   }

   static {
      addEntries(NISTObjectIdentifiers.dsa_with_sha224, "DSA");
      addEntries(NISTObjectIdentifiers.dsa_with_sha256, "DSA");
      addEntries(NISTObjectIdentifiers.dsa_with_sha384, "DSA");
      addEntries(NISTObjectIdentifiers.dsa_with_sha512, "DSA");
      addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_224, "DSA");
      addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_256, "DSA");
      addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_384, "DSA");
      addEntries(NISTObjectIdentifiers.id_dsa_with_sha3_512, "DSA");
      addEntries(OIWObjectIdentifiers.dsaWithSHA1, "DSA");
      addEntries(OIWObjectIdentifiers.md4WithRSA, "RSA");
      addEntries(OIWObjectIdentifiers.md4WithRSAEncryption, "RSA");
      addEntries(OIWObjectIdentifiers.md5WithRSA, "RSA");
      addEntries(OIWObjectIdentifiers.sha1WithRSA, "RSA");
      addEntries(PKCSObjectIdentifiers.md2WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.md4WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.md5WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.sha1WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.sha224WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.sha256WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.sha384WithRSAEncryption, "RSA");
      addEntries(PKCSObjectIdentifiers.sha512WithRSAEncryption, "RSA");
      addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_224, "RSA");
      addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_256, "RSA");
      addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_384, "RSA");
      addEntries(NISTObjectIdentifiers.id_rsassa_pkcs1_v1_5_with_sha3_512, "RSA");
      addEntries(X9ObjectIdentifiers.ecdsa_with_SHA1, "ECDSA");
      addEntries(X9ObjectIdentifiers.ecdsa_with_SHA224, "ECDSA");
      addEntries(X9ObjectIdentifiers.ecdsa_with_SHA256, "ECDSA");
      addEntries(X9ObjectIdentifiers.ecdsa_with_SHA384, "ECDSA");
      addEntries(X9ObjectIdentifiers.ecdsa_with_SHA512, "ECDSA");
      addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_224, "ECDSA");
      addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_256, "ECDSA");
      addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_384, "ECDSA");
      addEntries(NISTObjectIdentifiers.id_ecdsa_with_sha3_512, "ECDSA");
      addEntries(X9ObjectIdentifiers.id_dsa_with_sha1, "DSA");
      addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "ECDSA");
      addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "ECDSA");
      addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "ECDSA");
      addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "ECDSA");
      addEntries(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "ECDSA");
      addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_1, "RSA");
      addEntries(EACObjectIdentifiers.id_TA_RSA_v1_5_SHA_256, "RSA");
      addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_1, "RSAandMGF1");
      addEntries(EACObjectIdentifiers.id_TA_RSA_PSS_SHA_256, "RSAandMGF1");
      addEntries(X9ObjectIdentifiers.id_dsa, "DSA");
      addEntries(PKCSObjectIdentifiers.rsaEncryption, "RSA");
      addEntries(TeleTrusTObjectIdentifiers.teleTrusTRSAsignatureAlgorithm, "RSA");
      addEntries(X509ObjectIdentifiers.id_ea_rsa, "RSA");
      addEntries(PKCSObjectIdentifiers.id_RSASSA_PSS, "RSAandMGF1");
      addEntries(CryptoProObjectIdentifiers.gostR3410_94, "GOST3410");
      addEntries(CryptoProObjectIdentifiers.gostR3410_2001, "ECGOST3410");
      addEntries(new ASN1ObjectIdentifier("1.3.6.1.4.1.5849.1.6.2"), "ECGOST3410");
      addEntries(new ASN1ObjectIdentifier("1.3.6.1.4.1.5849.1.1.5"), "GOST3410");
      addEntries(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, "ECGOST3410-2012-256");
      addEntries(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512, "ECGOST3410-2012-512");
      addEntries(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "ECGOST3410");
      addEntries(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3410");
      addEntries(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_256, "ECGOST3410-2012-256");
      addEntries(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_512, "ECGOST3410-2012-512");
   }
}
