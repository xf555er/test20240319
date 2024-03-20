package org.apache.fop.render.intermediate;

public class IFException extends Exception {
   private static final long serialVersionUID = 0L;

   public IFException(String message, Exception cause) {
      super(message, cause);
   }

   public IFException(String message) {
      super(message);
   }
}
