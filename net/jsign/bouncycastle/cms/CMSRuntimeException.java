package net.jsign.bouncycastle.cms;

public class CMSRuntimeException extends RuntimeException {
   Exception e;

   public Throwable getCause() {
      return this.e;
   }
}
