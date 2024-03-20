package net.jsign.bouncycastle.cms;

public class CMSAttributeTableGenerationException extends CMSRuntimeException {
   Exception e;

   public Throwable getCause() {
      return this.e;
   }
}
