package net.jsign.bouncycastle.jcajce.util;

import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public class ProviderJcaJceHelper implements JcaJceHelper {
   protected final Provider provider;

   public ProviderJcaJceHelper(Provider var1) {
      this.provider = var1;
   }

   public Cipher createCipher(String var1) throws NoSuchAlgorithmException, NoSuchPaddingException {
      return Cipher.getInstance(var1, this.provider);
   }

   public AlgorithmParameters createAlgorithmParameters(String var1) throws NoSuchAlgorithmException {
      return AlgorithmParameters.getInstance(var1, this.provider);
   }

   public KeyFactory createKeyFactory(String var1) throws NoSuchAlgorithmException {
      return KeyFactory.getInstance(var1, this.provider);
   }

   public SecretKeyFactory createSecretKeyFactory(String var1) throws NoSuchAlgorithmException {
      return SecretKeyFactory.getInstance(var1, this.provider);
   }

   public MessageDigest createMessageDigest(String var1) throws NoSuchAlgorithmException {
      return MessageDigest.getInstance(var1, this.provider);
   }

   public Signature createSignature(String var1) throws NoSuchAlgorithmException {
      return Signature.getInstance(var1, this.provider);
   }
}
