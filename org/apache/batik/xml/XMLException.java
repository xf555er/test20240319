package org.apache.batik.xml;

import java.io.PrintStream;
import java.io.PrintWriter;

public class XMLException extends RuntimeException {
   protected Exception exception;

   public XMLException(String message) {
      super(message);
      this.exception = null;
   }

   public XMLException(Exception e) {
      this.exception = e;
   }

   public XMLException(String message, Exception e) {
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

   public void printStackTrace() {
      if (this.exception == null) {
         super.printStackTrace();
      } else {
         synchronized(System.err) {
            System.err.println(this);
            super.printStackTrace();
         }
      }

   }

   public void printStackTrace(PrintStream s) {
      if (this.exception == null) {
         super.printStackTrace(s);
      } else {
         synchronized(s) {
            s.println(this);
            super.printStackTrace();
         }
      }

   }

   public void printStackTrace(PrintWriter s) {
      if (this.exception == null) {
         super.printStackTrace(s);
      } else {
         synchronized(s) {
            s.println(this);
            super.printStackTrace(s);
         }
      }

   }
}
