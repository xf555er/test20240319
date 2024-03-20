package org.apache.batik.parser;

public class ParseException extends RuntimeException {
   protected Exception exception;
   protected int lineNumber;
   protected int columnNumber;

   public ParseException(String message, int line, int column) {
      super(message);
      this.exception = null;
      this.lineNumber = line;
      this.columnNumber = column;
   }

   public ParseException(Exception e) {
      this.exception = e;
      this.lineNumber = -1;
      this.columnNumber = -1;
   }

   public ParseException(String message, Exception e) {
      super(message);
      this.exception = e;
   }

   public String getMessage() {
      String message = super.getMessage();
      return message == null && this.exception != null ? this.exception.getMessage() : message;
   }

   public Exception getException() {
      return this.exception;
   }

   public int getLineNumber() {
      return this.lineNumber;
   }

   public int getColumnNumber() {
      return this.columnNumber;
   }
}
