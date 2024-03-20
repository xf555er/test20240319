package net.jsign.bouncycastle.util;

public class StoreException extends RuntimeException {
   private Throwable _e;

   public Throwable getCause() {
      return this._e;
   }
}
