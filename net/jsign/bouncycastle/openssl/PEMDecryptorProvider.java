package net.jsign.bouncycastle.openssl;

import net.jsign.bouncycastle.operator.OperatorCreationException;

public interface PEMDecryptorProvider {
   PEMDecryptor get(String var1) throws OperatorCreationException;
}
