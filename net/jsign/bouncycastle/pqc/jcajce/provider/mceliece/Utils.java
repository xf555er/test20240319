package net.jsign.bouncycastle.pqc.jcajce.provider.mceliece;

import net.jsign.bouncycastle.asn1.DERNull;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import net.jsign.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;
import net.jsign.bouncycastle.crypto.Digest;
import net.jsign.bouncycastle.crypto.util.DigestFactory;

class Utils {
   static AlgorithmIdentifier getDigAlgId(String var0) {
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

   static Digest getDigest(AlgorithmIdentifier var0) {
      if (var0.getAlgorithm().equals(OIWObjectIdentifiers.idSHA1)) {
         return DigestFactory.createSHA1();
      } else if (var0.getAlgorithm().equals(NISTObjectIdentifiers.id_sha224)) {
         return DigestFactory.createSHA224();
      } else if (var0.getAlgorithm().equals(NISTObjectIdentifiers.id_sha256)) {
         return DigestFactory.createSHA256();
      } else if (var0.getAlgorithm().equals(NISTObjectIdentifiers.id_sha384)) {
         return DigestFactory.createSHA384();
      } else if (var0.getAlgorithm().equals(NISTObjectIdentifiers.id_sha512)) {
         return DigestFactory.createSHA512();
      } else {
         throw new IllegalArgumentException("unrecognised OID in digest algorithm identifier: " + var0.getAlgorithm());
      }
   }
}
