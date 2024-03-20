package net.jsign.bouncycastle.crypto.io;

import java.io.IOException;

public class CipherIOException extends IOException {
   private final Throwable cause;

   public CipherIOException(String var1, Throwable var2) {
      super(var1);
      this.cause = var2;
   }

   public Throwable getCause() {
      return this.cause;
   }
}
