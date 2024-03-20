package net.jsign.json-io.util.io;

public class JsonIoException extends RuntimeException {
   public JsonIoException() {
   }

   public JsonIoException(String message) {
      super(message);
   }

   public JsonIoException(String message, Throwable cause) {
      super(message, cause);
   }
}
