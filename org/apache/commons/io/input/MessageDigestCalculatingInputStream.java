package org.apache.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MessageDigestCalculatingInputStream extends ObservableInputStream {
   private final MessageDigest messageDigest;

   public MessageDigestCalculatingInputStream(InputStream inputStream, MessageDigest messageDigest) {
      super(inputStream, new MessageDigestMaintainingObserver(messageDigest));
      this.messageDigest = messageDigest;
   }

   public MessageDigestCalculatingInputStream(InputStream inputStream, String algorithm) throws NoSuchAlgorithmException {
      this(inputStream, MessageDigest.getInstance(algorithm));
   }

   public MessageDigestCalculatingInputStream(InputStream inputStream) throws NoSuchAlgorithmException {
      this(inputStream, MessageDigest.getInstance("MD5"));
   }

   public MessageDigest getMessageDigest() {
      return this.messageDigest;
   }

   public static class MessageDigestMaintainingObserver extends ObservableInputStream.Observer {
      private final MessageDigest messageDigest;

      public MessageDigestMaintainingObserver(MessageDigest messageDigest) {
         this.messageDigest = messageDigest;
      }

      public void data(int input) throws IOException {
         this.messageDigest.update((byte)input);
      }

      public void data(byte[] input, int offset, int length) throws IOException {
         this.messageDigest.update(input, offset, length);
      }
   }
}
