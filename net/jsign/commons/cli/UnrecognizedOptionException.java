package net.jsign.commons.cli;

public class UnrecognizedOptionException extends ParseException {
   private String option;

   public UnrecognizedOptionException(String message) {
      super(message);
   }

   public UnrecognizedOptionException(String message, String option) {
      this(message);
      this.option = option;
   }
}
