package net.jsign.bouncycastle.asn1.edec;

import net.jsign.bouncycastle.asn1.ASN1ObjectIdentifier;

public interface EdECObjectIdentifiers {
   ASN1ObjectIdentifier id_edwards_curve_algs = new ASN1ObjectIdentifier("1.3.101");
   ASN1ObjectIdentifier id_X25519 = id_edwards_curve_algs.branch("110").intern();
   ASN1ObjectIdentifier id_X448 = id_edwards_curve_algs.branch("111").intern();
   ASN1ObjectIdentifier id_Ed25519 = id_edwards_curve_algs.branch("112").intern();
   ASN1ObjectIdentifier id_Ed448 = id_edwards_curve_algs.branch("113").intern();
}
