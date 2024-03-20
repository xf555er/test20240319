package net.jsign.commons.math3.exception;

import net.jsign.commons.math3.exception.util.ExceptionContext;
import net.jsign.commons.math3.exception.util.LocalizedFormats;

public class MathArithmeticException extends ArithmeticException {
   private final ExceptionContext context = new ExceptionContext(this);

   public MathArithmeticException() {
      this.context.addMessage(LocalizedFormats.ARITHMETIC_EXCEPTION);
   }

   public String getMessage() {
      return this.context.getMessage();
   }

   public String getLocalizedMessage() {
      return this.context.getLocalizedMessage();
   }
}
