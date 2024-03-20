package org.apache.batik.transcoder;

public class TranscoderException extends Exception {
   protected Exception ex;

   public TranscoderException(String s) {
      this(s, (Exception)null);
   }

   public TranscoderException(Exception ex) {
      this((String)null, ex);
   }

   public TranscoderException(String s, Exception ex) {
      super(s, ex);
      this.ex = ex;
   }

   public String getMessage() {
      String msg = super.getMessage();
      if (this.ex != null) {
         msg = msg + "\nEnclosed Exception:\n";
         msg = msg + this.ex.getMessage();
      }

      return msg;
   }

   public Exception getException() {
      return this.ex;
   }
}
