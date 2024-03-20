package org.apache.batik.script;

public class InterpreterException extends RuntimeException {
   private int line;
   private int column;
   private Exception embedded;

   public InterpreterException(String message, int lineno, int columnno) {
      super(message);
      this.line = -1;
      this.column = -1;
      this.embedded = null;
      this.line = lineno;
      this.column = columnno;
   }

   public InterpreterException(Exception exception, String message, int lineno, int columnno) {
      this(message, lineno, columnno);
      this.embedded = exception;
   }

   public int getLineNumber() {
      return this.line;
   }

   public int getColumnNumber() {
      return this.column;
   }

   public Exception getException() {
      return this.embedded;
   }

   public String getMessage() {
      String msg = super.getMessage();
      if (msg != null) {
         return msg;
      } else {
         return this.embedded != null ? this.embedded.getMessage() : null;
      }
   }
}
