package net.jsign.bouncycastle.operator;

import net.jsign.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface InputDecryptorProvider {
   InputDecryptor get(AlgorithmIdentifier var1) throws OperatorCreationException;
}
