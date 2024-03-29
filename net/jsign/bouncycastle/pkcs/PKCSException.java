package net.jsign.bouncycastle.pkcs;

public class PKCSException extends Exception {
   private Throwable cause;

   public PKCSException(String var1, Throwable var2) {
      super(var1);
      this.cause = var2;
   }

   public Throwable getCause() {
      return this.cause;
   }
}
