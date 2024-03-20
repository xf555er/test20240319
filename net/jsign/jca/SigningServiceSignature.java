package net.jsign.jca;

import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.SignatureSpi;

class SigningServiceSignature extends SignatureSpi {
   private final SigningService service;
   private final String signingAlgorithm;
   private SigningServicePrivateKey privateKey;
   private byte[] data;

   public SigningServiceSignature(SigningService service, String signingAlgorithm) {
      this.service = service;
      this.signingAlgorithm = signingAlgorithm;
   }

   protected void engineInitVerify(PublicKey publicKey) {
      throw new UnsupportedOperationException();
   }

   protected void engineInitSign(PrivateKey privateKey) {
      this.privateKey = (SigningServicePrivateKey)privateKey;
   }

   protected void engineUpdate(byte b) {
      throw new UnsupportedOperationException();
   }

   protected void engineUpdate(byte[] b, int off, int len) {
      this.data = new byte[len];
      System.arraycopy(b, off, this.data, 0, len);
   }

   protected byte[] engineSign() throws SignatureException {
      try {
         return this.service.sign(this.privateKey, this.signingAlgorithm, this.data);
      } catch (GeneralSecurityException var2) {
         throw new SignatureException(var2);
      }
   }

   protected boolean engineVerify(byte[] sigBytes) {
      throw new UnsupportedOperationException();
   }

   protected void engineSetParameter(String param, Object value) throws InvalidParameterException {
      throw new UnsupportedOperationException();
   }

   protected Object engineGetParameter(String param) throws InvalidParameterException {
      throw new UnsupportedOperationException();
   }
}
