package net.jsign.jca;

import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.List;

public interface SigningService {
   String getName();

   List aliases() throws KeyStoreException;

   Certificate[] getCertificateChain(String var1) throws KeyStoreException;

   SigningServicePrivateKey getPrivateKey(String var1) throws UnrecoverableKeyException;

   byte[] sign(SigningServicePrivateKey var1, String var2, byte[] var3) throws GeneralSecurityException;
}
