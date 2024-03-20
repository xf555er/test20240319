package net.jsign.bouncycastle.jcajce.util;

import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;

public interface JcaJceHelper {
   Cipher createCipher(String var1) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException;

   AlgorithmParameters createAlgorithmParameters(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

   KeyFactory createKeyFactory(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

   SecretKeyFactory createSecretKeyFactory(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

   MessageDigest createMessageDigest(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;

   Signature createSignature(String var1) throws NoSuchAlgorithmException, NoSuchProviderException;
}
