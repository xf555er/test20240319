package net.jsign.script;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

abstract class WSHScript extends SignableScript {
   public WSHScript() {
   }

   public WSHScript(File file) throws IOException {
      super(file);
   }

   public WSHScript(File file, Charset encoding) throws IOException {
      super(file, encoding);
   }

   boolean isByteOrderMarkSigned() {
      return false;
   }

   public byte[] computeDigest(MessageDigest digest) {
      String content = this.getContentWithoutSignatureBlock();
      digest.update(content.getBytes(StandardCharsets.UTF_16LE));
      int pos = this.getSignatureInsertionPoint(content);
      digest.update((byte)pos);
      digest.update((byte)(pos >>> 8));
      digest.update((byte)(pos >>> 16));
      digest.update((byte)(pos >>> 24));
      return digest.digest();
   }
}
