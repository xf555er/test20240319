package net.jsign.timestamp;

public class TimestampingException extends RuntimeException {
   private static final long serialVersionUID = -8795388119159573448L;

   public TimestampingException(String message) {
      super(message);
   }

   public TimestampingException(String message, Throwable cause) {
      super(message, cause);
   }
}
