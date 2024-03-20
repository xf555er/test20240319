package net.jsign.bouncycastle.operator.jcajce;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.PSSParameterSpec;
import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1Encodable;
import net.jsign.bouncycastle.asn1.ASN1Integer;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.ASN1Sequence;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.bsi.BSIObjectIdentifiers;
import net.jsign.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import net.jsign.bouncycastle.asn1.eac.EACObjectIdentifiers;
import net.jsign.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import net.jsign.bouncycastle.asn1.isara.IsaraObjectIdentifiers;
import net.jsign.bouncycastle.asn1.kisa.KISAObjectIdentifiers;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.ntt.NTTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import net.jsign.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import net.jsign.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import net.jsign.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import net.jsign.bouncycastle.jcajce.util.AlgorithmParametersUtils;
import net.jsign.bouncycastle.jcajce.util.JcaJceHelper;
import net.jsign.bouncycastle.jcajce.util.MessageDigestUtils;
import net.jsign.bouncycastle.util.Integers;

class OperatorHelper {
   private static final Map oids = new HashMap();
   private static final Map asymmetricWrapperAlgNames = new HashMap();
   private static final Map symmetricWrapperAlgNames = new HashMap();
   private static final Map symmetricKeyAlgNames = new HashMap();
   private static final Map symmetricWrapperKeySizes = new HashMap();
   private JcaJceHelper helper;

   OperatorHelper(JcaJceHelper var1) {
      this.helper = var1;
   }

   MessageDigest createDigest(AlgorithmIdentifier var1) throws GeneralSecurityException {
      MessageDigest var2;
      try {
         if (var1.getAlgorithm().equals(NISTObjectIdentifiers.id_shake256_len)) {
            var2 = this.helper.createMessageDigest("SHAKE256-" + ASN1Integer.getInstance(var1.getParameters()).getValue());
         } else if (var1.getAlgorithm().equals(NISTObjectIdentifiers.id_shake128_len)) {
            var2 = this.helper.createMessageDigest("SHAKE128-" + ASN1Integer.getInstance(var1.getParameters()).getValue());
         } else {
            var2 = this.helper.createMessageDigest(MessageDigestUtils.getDigestName(var1.getAlgorithm()));
         }
      } catch (NoSuchAlgorithmException var5) {
         if (oids.get(var1.getAlgorithm()) == null) {
            throw var5;
         }

         String var4 = (String)oids.get(var1.getAlgorithm());
         var2 = this.helper.createMessageDigest(var4);
      }

      return var2;
   }

   Signature createSignature(AlgorithmIdentifier var1) throws GeneralSecurityException {
      String var2 = getSignatureName(var1);

      Signature var3;
      try {
         var3 = this.helper.createSignature(var2);
      } catch (NoSuchAlgorithmException var7) {
         String var5;
         if (var2.endsWith("WITHRSAANDMGF1")) {
            var5 = var2.substring(0, var2.indexOf(87)) + "WITHRSASSA-PSS";
            var3 = this.helper.createSignature(var5);
         } else {
            if (oids.get(var1.getAlgorithm()) == null) {
               throw var7;
            }

            var5 = (String)oids.get(var1.getAlgorithm());
            var3 = this.helper.createSignature(var5);
         }
      }

      if (var1.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
         ASN1Sequence var4 = ASN1Sequence.getInstance(var1.getParameters());
         if (this.notDefaultPSSParams(var4)) {
            try {
               AlgorithmParameters var8 = this.helper.createAlgorithmParameters("PSS");
               var8.init(var4.getEncoded());
               var3.setParameter(var8.getParameterSpec(PSSParameterSpec.class));
            } catch (IOException var6) {
               throw new GeneralSecurityException("unable to process PSS parameters: " + var6.getMessage());
            }
         }
      }

      return var3;
   }

   public Signature createRawSignature(AlgorithmIdentifier var1) {
      try {
         String var3 = getSignatureName(var1);
         var3 = "NONE" + var3.substring(var3.indexOf("WITH"));
         Signature var2 = this.helper.createSignature(var3);
         if (var1.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
            AlgorithmParameters var4 = this.helper.createAlgorithmParameters(var3);
            AlgorithmParametersUtils.loadParameters(var4, var1.getParameters());
            PSSParameterSpec var5 = (PSSParameterSpec)var4.getParameterSpec(PSSParameterSpec.class);
            var2.setParameter(var5);
         }

         return var2;
      } catch (Exception var6) {
         return null;
      }
   }

   private static String getSignatureName(AlgorithmIdentifier var0) {
      ASN1Encodable var1 = var0.getParameters();
      if (var1 != null && !DERNull.INSTANCE.equals(var1) && var0.getAlgorithm().equals(PKCSObjectIdentifiers.id_RSASSA_PSS)) {
         RSASSAPSSparams var2 = RSASSAPSSparams.getInstance(var1);
         return getDigestName(var2.getHashAlgorithm().getAlgorithm()) + "WITHRSAANDMGF1";
      } else {
         return oids.containsKey(var0.getAlgorithm()) ? (String)oids.get(var0.getAlgorithm()) : var0.getAlgorithm().getId();
      }
   }

   private static String getDigestName(ASN1ObjectIdentifier var0) {
      String var1 = MessageDigestUtils.getDigestName(var0);
      int var2 = var1.indexOf(45);
      return var2 > 0 && !var1.startsWith("SHA3") ? var1.substring(0, var2) + var1.substring(var2 + 1) : var1;
   }

   private boolean notDefaultPSSParams(ASN1Sequence var1) throws GeneralSecurityException {
      if (var1 != null && var1.size() != 0) {
         RSASSAPSSparams var2 = RSASSAPSSparams.getInstance(var1);
         if (!var2.getMaskGenAlgorithm().getAlgorithm().equals(PKCSObjectIdentifiers.id_mgf1)) {
            return true;
         } else if (!var2.getHashAlgorithm().equals(AlgorithmIdentifier.getInstance(var2.getMaskGenAlgorithm().getParameters()))) {
            return true;
         } else {
            MessageDigest var3 = this.createDigest(var2.getHashAlgorithm());
            return var2.getSaltLength().intValue() != var3.getDigestLength();
         }
      } else {
         return false;
      }
   }

   static {
      oids.put(EdECObjectIdentifiers.id_Ed25519, "Ed25519");
      oids.put(EdECObjectIdentifiers.id_Ed448, "Ed448");
      oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"), "SHA1WITHRSA");
      oids.put(PKCSObjectIdentifiers.sha224WithRSAEncryption, "SHA224WITHRSA");
      oids.put(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256WITHRSA");
      oids.put(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384WITHRSA");
      oids.put(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512WITHRSA");
      oids.put(CMSObjectIdentifiers.id_RSASSA_PSS_SHAKE128, "SHAKE128WITHRSAPSS");
      oids.put(CMSObjectIdentifiers.id_RSASSA_PSS_SHAKE256, "SHAKE256WITHRSAPSS");
      oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94, "GOST3411WITHGOST3410");
      oids.put(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001, "GOST3411WITHECGOST3410");
      oids.put(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_256, "GOST3411-2012-256WITHECGOST3410-2012-256");
      oids.put(RosstandartObjectIdentifiers.id_tc26_signwithdigest_gost_3410_12_512, "GOST3411-2012-512WITHECGOST3410-2012-512");
      oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA1, "SHA1WITHPLAIN-ECDSA");
      oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA224, "SHA224WITHPLAIN-ECDSA");
      oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA256, "SHA256WITHPLAIN-ECDSA");
      oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA384, "SHA384WITHPLAIN-ECDSA");
      oids.put(BSIObjectIdentifiers.ecdsa_plain_SHA512, "SHA512WITHPLAIN-ECDSA");
      oids.put(BSIObjectIdentifiers.ecdsa_plain_RIPEMD160, "RIPEMD160WITHPLAIN-ECDSA");
      oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_1, "SHA1WITHCVC-ECDSA");
      oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_224, "SHA224WITHCVC-ECDSA");
      oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_256, "SHA256WITHCVC-ECDSA");
      oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_384, "SHA384WITHCVC-ECDSA");
      oids.put(EACObjectIdentifiers.id_TA_ECDSA_SHA_512, "SHA512WITHCVC-ECDSA");
      oids.put(IsaraObjectIdentifiers.id_alg_xmss, "XMSS");
      oids.put(IsaraObjectIdentifiers.id_alg_xmssmt, "XMSSMT");
      oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.4"), "MD5WITHRSA");
      oids.put(new ASN1ObjectIdentifier("1.2.840.113549.1.1.2"), "MD2WITHRSA");
      oids.put(new ASN1ObjectIdentifier("1.2.840.10040.4.3"), "SHA1WITHDSA");
      oids.put(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1WITHECDSA");
      oids.put(X9ObjectIdentifiers.ecdsa_with_SHA224, "SHA224WITHECDSA");
      oids.put(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256WITHECDSA");
      oids.put(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384WITHECDSA");
      oids.put(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512WITHECDSA");
      oids.put(CMSObjectIdentifiers.id_ecdsa_with_shake128, "SHAKE128WITHECDSA");
      oids.put(CMSObjectIdentifiers.id_ecdsa_with_shake256, "SHAKE256WITHECDSA");
      oids.put(OIWObjectIdentifiers.sha1WithRSA, "SHA1WITHRSA");
      oids.put(OIWObjectIdentifiers.dsaWithSHA1, "SHA1WITHDSA");
      oids.put(NISTObjectIdentifiers.dsa_with_sha224, "SHA224WITHDSA");
      oids.put(NISTObjectIdentifiers.dsa_with_sha256, "SHA256WITHDSA");
      oids.put(OIWObjectIdentifiers.idSHA1, "SHA1");
      oids.put(NISTObjectIdentifiers.id_sha224, "SHA224");
      oids.put(NISTObjectIdentifiers.id_sha256, "SHA256");
      oids.put(NISTObjectIdentifiers.id_sha384, "SHA384");
      oids.put(NISTObjectIdentifiers.id_sha512, "SHA512");
      oids.put(TeleTrusTObjectIdentifiers.ripemd128, "RIPEMD128");
      oids.put(TeleTrusTObjectIdentifiers.ripemd160, "RIPEMD160");
      oids.put(TeleTrusTObjectIdentifiers.ripemd256, "RIPEMD256");
      asymmetricWrapperAlgNames.put(PKCSObjectIdentifiers.rsaEncryption, "RSA/ECB/PKCS1Padding");
      asymmetricWrapperAlgNames.put(CryptoProObjectIdentifiers.gostR3410_2001, "ECGOST3410");
      symmetricWrapperAlgNames.put(PKCSObjectIdentifiers.id_alg_CMS3DESwrap, "DESEDEWrap");
      symmetricWrapperAlgNames.put(PKCSObjectIdentifiers.id_alg_CMSRC2wrap, "RC2Wrap");
      symmetricWrapperAlgNames.put(NISTObjectIdentifiers.id_aes128_wrap, "AESWrap");
      symmetricWrapperAlgNames.put(NISTObjectIdentifiers.id_aes192_wrap, "AESWrap");
      symmetricWrapperAlgNames.put(NISTObjectIdentifiers.id_aes256_wrap, "AESWrap");
      symmetricWrapperAlgNames.put(NTTObjectIdentifiers.id_camellia128_wrap, "CamelliaWrap");
      symmetricWrapperAlgNames.put(NTTObjectIdentifiers.id_camellia192_wrap, "CamelliaWrap");
      symmetricWrapperAlgNames.put(NTTObjectIdentifiers.id_camellia256_wrap, "CamelliaWrap");
      symmetricWrapperAlgNames.put(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap, "SEEDWrap");
      symmetricWrapperAlgNames.put(PKCSObjectIdentifiers.des_EDE3_CBC, "DESede");
      symmetricWrapperKeySizes.put(PKCSObjectIdentifiers.id_alg_CMS3DESwrap, Integers.valueOf(192));
      symmetricWrapperKeySizes.put(NISTObjectIdentifiers.id_aes128_wrap, Integers.valueOf(128));
      symmetricWrapperKeySizes.put(NISTObjectIdentifiers.id_aes192_wrap, Integers.valueOf(192));
      symmetricWrapperKeySizes.put(NISTObjectIdentifiers.id_aes256_wrap, Integers.valueOf(256));
      symmetricWrapperKeySizes.put(NTTObjectIdentifiers.id_camellia128_wrap, Integers.valueOf(128));
      symmetricWrapperKeySizes.put(NTTObjectIdentifiers.id_camellia192_wrap, Integers.valueOf(192));
      symmetricWrapperKeySizes.put(NTTObjectIdentifiers.id_camellia256_wrap, Integers.valueOf(256));
      symmetricWrapperKeySizes.put(KISAObjectIdentifiers.id_npki_app_cmsSeed_wrap, Integers.valueOf(128));
      symmetricWrapperKeySizes.put(PKCSObjectIdentifiers.des_EDE3_CBC, Integers.valueOf(192));
      symmetricKeyAlgNames.put(NISTObjectIdentifiers.aes, "AES");
      symmetricKeyAlgNames.put(NISTObjectIdentifiers.id_aes128_CBC, "AES");
      symmetricKeyAlgNames.put(NISTObjectIdentifiers.id_aes192_CBC, "AES");
      symmetricKeyAlgNames.put(NISTObjectIdentifiers.id_aes256_CBC, "AES");
      symmetricKeyAlgNames.put(PKCSObjectIdentifiers.des_EDE3_CBC, "DESede");
      symmetricKeyAlgNames.put(PKCSObjectIdentifiers.RC2_CBC, "RC2");
   }
}
