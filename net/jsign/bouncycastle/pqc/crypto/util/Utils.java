package net.jsign.bouncycastle.pqc.crypto.util;

import java.util.HashMap;
import java.util.Map;
import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.crypto.Digest;
import net.jsign.bouncycastle.crypto.digests.SHA256Digest;
import net.jsign.bouncycastle.crypto.digests.SHA512Digest;
import net.jsign.bouncycastle.crypto.digests.SHAKEDigest;
import net.jsign.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import net.jsign.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import net.jsign.bouncycastle.util.Integers;

class Utils {
   static final AlgorithmIdentifier AlgID_qTESLA_p_I;
   static final AlgorithmIdentifier AlgID_qTESLA_p_III;
   static final AlgorithmIdentifier SPHINCS_SHA3_256;
   static final AlgorithmIdentifier SPHINCS_SHA512_256;
   static final AlgorithmIdentifier XMSS_SHA256;
   static final AlgorithmIdentifier XMSS_SHA512;
   static final AlgorithmIdentifier XMSS_SHAKE128;
   static final AlgorithmIdentifier XMSS_SHAKE256;
   static final Map categories;

   static int qTeslaLookupSecurityCategory(AlgorithmIdentifier var0) {
      return (Integer)categories.get(var0.getAlgorithm());
   }

   static AlgorithmIdentifier qTeslaLookupAlgID(int var0) {
      switch (var0) {
         case 5:
            return AlgID_qTESLA_p_I;
         case 6:
            return AlgID_qTESLA_p_III;
         default:
            throw new IllegalArgumentException("unknown security category: " + var0);
      }
   }

   static AlgorithmIdentifier sphincs256LookupTreeAlgID(String var0) {
      if (var0.equals("SHA3-256")) {
         return SPHINCS_SHA3_256;
      } else if (var0.equals("SHA-512/256")) {
         return SPHINCS_SHA512_256;
      } else {
         throw new IllegalArgumentException("unknown tree digest: " + var0);
      }
   }

   static AlgorithmIdentifier xmssLookupTreeAlgID(String var0) {
      if (var0.equals("SHA-256")) {
         return XMSS_SHA256;
      } else if (var0.equals("SHA-512")) {
         return XMSS_SHA512;
      } else if (var0.equals("SHAKE128")) {
         return XMSS_SHAKE128;
      } else if (var0.equals("SHAKE256")) {
         return XMSS_SHAKE256;
      } else {
         throw new IllegalArgumentException("unknown tree digest: " + var0);
      }
   }

   static String sphincs256LookupTreeAlgName(SPHINCS256KeyParams var0) {
      AlgorithmIdentifier var1 = var0.getTreeDigest();
      if (var1.getAlgorithm().equals(SPHINCS_SHA3_256.getAlgorithm())) {
         return "SHA3-256";
      } else if (var1.getAlgorithm().equals(SPHINCS_SHA512_256.getAlgorithm())) {
         return "SHA-512/256";
      } else {
         throw new IllegalArgumentException("unknown tree digest: " + var1.getAlgorithm());
      }
   }

   static Digest getDigest(ASN1ObjectIdentifier var0) {
      if (var0.equals(NISTObjectIdentifiers.id_sha256)) {
         return new SHA256Digest();
      } else if (var0.equals(NISTObjectIdentifiers.id_sha512)) {
         return new SHA512Digest();
      } else if (var0.equals(NISTObjectIdentifiers.id_shake128)) {
         return new SHAKEDigest(128);
      } else if (var0.equals(NISTObjectIdentifiers.id_shake256)) {
         return new SHAKEDigest(256);
      } else {
         throw new IllegalArgumentException("unrecognized digest OID: " + var0);
      }
   }

   public static AlgorithmIdentifier getAlgorithmIdentifier(String var0) {
      if (var0.equals("SHA-1")) {
         return new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE);
      } else if (var0.equals("SHA-224")) {
         return new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha224);
      } else if (var0.equals("SHA-256")) {
         return new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256);
      } else if (var0.equals("SHA-384")) {
         return new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384);
      } else if (var0.equals("SHA-512")) {
         return new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512);
      } else {
         throw new IllegalArgumentException("unrecognised digest algorithm: " + var0);
      }
   }

   public static String getDigestName(ASN1ObjectIdentifier var0) {
      if (var0.equals(OIWObjectIdentifiers.idSHA1)) {
         return "SHA-1";
      } else if (var0.equals(NISTObjectIdentifiers.id_sha224)) {
         return "SHA-224";
      } else if (var0.equals(NISTObjectIdentifiers.id_sha256)) {
         return "SHA-256";
      } else if (var0.equals(NISTObjectIdentifiers.id_sha384)) {
         return "SHA-384";
      } else if (var0.equals(NISTObjectIdentifiers.id_sha512)) {
         return "SHA-512";
      } else {
         throw new IllegalArgumentException("unrecognised digest algorithm: " + var0);
      }
   }

   static {
      AlgID_qTESLA_p_I = new AlgorithmIdentifier(PQCObjectIdentifiers.qTESLA_p_I);
      AlgID_qTESLA_p_III = new AlgorithmIdentifier(PQCObjectIdentifiers.qTESLA_p_III);
      SPHINCS_SHA3_256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha3_256);
      SPHINCS_SHA512_256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512_256);
      XMSS_SHA256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256);
      XMSS_SHA512 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512);
      XMSS_SHAKE128 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_shake128);
      XMSS_SHAKE256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_shake256);
      categories = new HashMap();
      categories.put(PQCObjectIdentifiers.qTESLA_p_I, Integers.valueOf(5));
      categories.put(PQCObjectIdentifiers.qTESLA_p_III, Integers.valueOf(6));
   }
}
