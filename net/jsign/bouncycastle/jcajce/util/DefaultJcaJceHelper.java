package net.jsign.bouncycastle.jcajce.util;

import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public class DefaultJcaJceHelper implements JcaJceHelper {
   public Cipher createCipher(String var1) throws NoSuchAlgorithmException, NoSuchPaddingException {
      return Cipher.getInstance(var1);
   }

   public AlgorithmParameters createAlgorithmParameters(String var1) throws NoSuchAlgorithmException {
      return AlgorithmParameters.getInstance(var1);
   }

   public KeyFactory createKeyFactory(String var1) throws NoSuchAlgorithmException {
      return KeyFactory.getInstance(var1);
   }

   public SecretKeyFactory createSecretKeyFactory(String var1) throws NoSuchAlgorithmException {
      return SecretKeyFactory.getInstance(var1);
   }

   public MessageDigest createMessageDigest(String var1) throws NoSuchAlgorithmException {
      return MessageDigest.getInstance(var1);
   }

   public Signature createSignature(String var1) throws NoSuchAlgorithmException {
      return Signature.getInstance(var1);
   }
}
