package net.jsign.bouncycastle.pqc.jcajce.provider.xmss;

import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;
import net.jsign.bouncycastle.asn1.nist.NISTObjectIdentifiers;

class DigestUtil {
   static ASN1ObjectIdentifier getDigestOID(String var0) {
      if (var0.equals("SHA-256")) {
         return NISTObjectIdentifiers.id_sha256;
      } else if (var0.equals("SHA-512")) {
         return NISTObjectIdentifiers.id_sha512;
      } else if (var0.equals("SHAKE128")) {
         return NISTObjectIdentifiers.id_shake128;
      } else if (var0.equals("SHAKE256")) {
         return NISTObjectIdentifiers.id_shake256;
      } else {
         throw new IllegalArgumentException("unrecognized digest: " + var0);
      }
   }
}
