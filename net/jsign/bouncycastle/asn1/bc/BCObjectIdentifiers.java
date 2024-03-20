package net.jsign.bouncycastle.asn1.bc;

import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface BCObjectIdentifiers {
   ASN1ObjectIdentifier bc = new ASN1ObjectIdentifier("1.3.6.1.4.1.22554");
   ASN1ObjectIdentifier bc_pbe = bc.branch("1");
   ASN1ObjectIdentifier bc_pbe_sha1 = bc_pbe.branch("1");
   ASN1ObjectIdentifier bc_pbe_sha256 = bc_pbe.branch("2.1");
   ASN1ObjectIdentifier bc_pbe_sha384 = bc_pbe.branch("2.2");
   ASN1ObjectIdentifier bc_pbe_sha512 = bc_pbe.branch("2.3");
   ASN1ObjectIdentifier bc_pbe_sha224 = bc_pbe.branch("2.4");
   ASN1ObjectIdentifier bc_pbe_sha1_pkcs5 = bc_pbe_sha1.branch("1");
   ASN1ObjectIdentifier bc_pbe_sha1_pkcs12 = bc_pbe_sha1.branch("2");
   ASN1ObjectIdentifier bc_pbe_sha256_pkcs5 = bc_pbe_sha256.branch("1");
   ASN1ObjectIdentifier bc_pbe_sha256_pkcs12 = bc_pbe_sha256.branch("2");
   ASN1ObjectIdentifier bc_pbe_sha1_pkcs12_aes128_cbc = bc_pbe_sha1_pkcs12.branch("1.2");
   ASN1ObjectIdentifier bc_pbe_sha1_pkcs12_aes192_cbc = bc_pbe_sha1_pkcs12.branch("1.22");
   ASN1ObjectIdentifier bc_pbe_sha1_pkcs12_aes256_cbc = bc_pbe_sha1_pkcs12.branch("1.42");
   ASN1ObjectIdentifier bc_pbe_sha256_pkcs12_aes128_cbc = bc_pbe_sha256_pkcs12.branch("1.2");
   ASN1ObjectIdentifier bc_pbe_sha256_pkcs12_aes192_cbc = bc_pbe_sha256_pkcs12.branch("1.22");
   ASN1ObjectIdentifier bc_pbe_sha256_pkcs12_aes256_cbc = bc_pbe_sha256_pkcs12.branch("1.42");
   ASN1ObjectIdentifier bc_sig = bc.branch("2");
   ASN1ObjectIdentifier sphincs256 = bc_sig.branch("1");
   ASN1ObjectIdentifier sphincs256_with_BLAKE512 = sphincs256.branch("1");
   ASN1ObjectIdentifier sphincs256_with_SHA512 = sphincs256.branch("2");
   ASN1ObjectIdentifier sphincs256_with_SHA3_512 = sphincs256.branch("3");
   ASN1ObjectIdentifier xmss = bc_sig.branch("2");
   ASN1ObjectIdentifier xmss_SHA256ph = xmss.branch("1");
   ASN1ObjectIdentifier xmss_SHA512ph = xmss.branch("2");
   ASN1ObjectIdentifier xmss_SHAKE128ph = xmss.branch("3");
   ASN1ObjectIdentifier xmss_SHAKE256ph = xmss.branch("4");
   ASN1ObjectIdentifier xmss_SHA256 = xmss.branch("5");
   ASN1ObjectIdentifier xmss_SHA512 = xmss.branch("6");
   ASN1ObjectIdentifier xmss_SHAKE128 = xmss.branch("7");
   ASN1ObjectIdentifier xmss_SHAKE256 = xmss.branch("8");
   ASN1ObjectIdentifier xmss_mt = bc_sig.branch("3");
   ASN1ObjectIdentifier xmss_mt_SHA256ph = xmss_mt.branch("1");
   ASN1ObjectIdentifier xmss_mt_SHA512ph = xmss_mt.branch("2");
   ASN1ObjectIdentifier xmss_mt_SHAKE128ph = xmss_mt.branch("3");
   ASN1ObjectIdentifier xmss_mt_SHAKE256ph = xmss_mt.branch("4");
   ASN1ObjectIdentifier xmss_mt_SHA256 = xmss_mt.branch("5");
   ASN1ObjectIdentifier xmss_mt_SHA512 = xmss_mt.branch("6");
   ASN1ObjectIdentifier xmss_mt_SHAKE128 = xmss_mt.branch("7");
   ASN1ObjectIdentifier xmss_mt_SHAKE256 = xmss_mt.branch("8");
   /** @deprecated */
   ASN1ObjectIdentifier xmss_with_SHA256 = xmss_SHA256ph;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_with_SHA512 = xmss_SHA512ph;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_with_SHAKE128 = xmss_SHAKE128ph;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_with_SHAKE256 = xmss_SHAKE256ph;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_mt_with_SHA256 = xmss_mt_SHA256ph;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_mt_with_SHA512 = xmss_mt_SHA512ph;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_mt_with_SHAKE128 = xmss_mt_SHAKE128;
   /** @deprecated */
   ASN1ObjectIdentifier xmss_mt_with_SHAKE256 = xmss_mt_SHAKE256;
   ASN1ObjectIdentifier qTESLA = bc_sig.branch("4");
   ASN1ObjectIdentifier qTESLA_Rnd1_I = qTESLA.branch("1");
   ASN1ObjectIdentifier qTESLA_Rnd1_III_size = qTESLA.branch("2");
   ASN1ObjectIdentifier qTESLA_Rnd1_III_speed = qTESLA.branch("3");
   ASN1ObjectIdentifier qTESLA_Rnd1_p_I = qTESLA.branch("4");
   ASN1ObjectIdentifier qTESLA_Rnd1_p_III = qTESLA.branch("5");
   ASN1ObjectIdentifier qTESLA_p_I = qTESLA.branch("11");
   ASN1ObjectIdentifier qTESLA_p_III = qTESLA.branch("12");
   ASN1ObjectIdentifier bc_exch = bc.branch("3");
   ASN1ObjectIdentifier newHope = bc_exch.branch("1");
   ASN1ObjectIdentifier bc_ext = bc.branch("4");
   ASN1ObjectIdentifier linkedCertificate = bc_ext.branch("1");
}
