package net.jsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

class PVK {
   private static final byte[] RSA2_KEY_MAGIC = "RSA2".getBytes();

   public static PrivateKey parse(File file, String password) throws GeneralSecurityException, IOException {
      ByteBuffer buffer = ByteBuffer.allocate((int)file.length());
      FileInputStream in = new FileInputStream(file);
      Throwable var4 = null;

      PrivateKey var5;
      try {
         in.getChannel().read(buffer);
         var5 = parse(buffer, password);
      } catch (Throwable var14) {
         var4 = var14;
         throw var14;
      } finally {
         if (in != null) {
            if (var4 != null) {
               try {
                  in.close();
               } catch (Throwable var13) {
                  var4.addSuppressed(var13);
               }
            } else {
               in.close();
            }
         }

      }

      return var5;
   }

   public static PrivateKey parse(ByteBuffer buffer, String password) throws GeneralSecurityException {
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      buffer.rewind();
      long magic = (long)buffer.getInt() & 4294967295L;
      if (2964713758L != magic) {
         throw new IllegalArgumentException("PVK header signature not found");
      } else {
         buffer.position(buffer.position() + 4);
         int keyType = buffer.getInt();
         boolean encrypted = buffer.getInt() != 0;
         int saltLength = buffer.getInt();
         int keyLength = buffer.getInt();
         byte[] salt = new byte[saltLength];
         buffer.get(salt);
         byte btype = buffer.get();
         byte version = buffer.get();
         buffer.position(buffer.position() + 2);
         int keyalg = buffer.getInt();
         byte[] key = new byte[keyLength - 8];
         buffer.get(key);
         if (encrypted) {
            key = decrypt(key, salt, password);
         }

         return parseKey(key);
      }
   }

   private static byte[] decrypt(byte[] encoded, byte[] salt, String password) throws GeneralSecurityException {
      byte[] hash = deriveKey(salt, password);
      String algorithm = "RC4";
      SecretKey strongKey = new SecretKeySpec(hash, 0, 16, algorithm);
      byte[] decoded = decrypt(strongKey, encoded);
      if (startsWith(decoded, RSA2_KEY_MAGIC)) {
         return decoded;
      } else {
         Arrays.fill(hash, 5, hash.length, (byte)0);
         SecretKey weakKey = new SecretKeySpec(hash, 0, 16, algorithm);
         decoded = decrypt(weakKey, encoded);
         if (startsWith(decoded, RSA2_KEY_MAGIC)) {
            return decoded;
         } else {
            throw new IllegalArgumentException("Unable to decrypt the PVK key, please verify the password");
         }
      }
   }

   private static byte[] decrypt(SecretKey key, byte[] encoded) throws GeneralSecurityException {
      Cipher cipher = Cipher.getInstance(key.getAlgorithm());
      cipher.init(2, key);
      return cipher.doFinal(encoded);
   }

   private static byte[] deriveKey(byte[] salt, String password) throws NoSuchAlgorithmException {
      MessageDigest digest = MessageDigest.getInstance("SHA1");
      digest.update(salt);
      if (password != null) {
         digest.update(password.getBytes());
      }

      return digest.digest();
   }

   private static PrivateKey parseKey(byte[] key) throws GeneralSecurityException {
      ByteBuffer buffer = ByteBuffer.wrap(key);
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      if (!startsWith(key, RSA2_KEY_MAGIC)) {
         throw new IllegalArgumentException("Unable to parse the PVK key, unsupported key format: " + new String(key, 0, RSA2_KEY_MAGIC.length));
      } else {
         buffer.position(buffer.position() + RSA2_KEY_MAGIC.length);
         int bitlength = buffer.getInt();
         BigInteger publicExponent = new BigInteger(String.valueOf(buffer.getInt()));
         int l = bitlength / 8;
         BigInteger modulus = getBigInteger(buffer, l);
         BigInteger primeP = getBigInteger(buffer, l / 2);
         BigInteger primeQ = getBigInteger(buffer, l / 2);
         BigInteger primeExponentP = getBigInteger(buffer, l / 2);
         BigInteger primeExponentQ = getBigInteger(buffer, l / 2);
         BigInteger crtCoefficient = getBigInteger(buffer, l / 2);
         BigInteger privateExponent = getBigInteger(buffer, l);
         RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(modulus, publicExponent, privateExponent, primeP, primeQ, primeExponentP, primeExponentQ, crtCoefficient);
         KeyFactory factory = KeyFactory.getInstance("RSA");
         return factory.generatePrivate(spec);
      }
   }

   private static BigInteger getBigInteger(ByteBuffer buffer, int length) {
      byte[] array = new byte[length + 1];
      buffer.get(array, 0, length);
      return new BigInteger(reverse(array));
   }

   private static byte[] reverse(byte[] array) {
      for(int i = 0; i < array.length / 2; ++i) {
         byte b = array[i];
         array[i] = array[array.length - 1 - i];
         array[array.length - 1 - i] = b;
      }

      return array;
   }

   private static boolean startsWith(byte[] array, byte[] prefix) {
      for(int i = 0; i < prefix.length; ++i) {
         if (prefix[i] != array[i]) {
            return false;
         }
      }

      return true;
   }
}
