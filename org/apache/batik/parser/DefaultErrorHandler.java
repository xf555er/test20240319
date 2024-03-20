package org.apache.batik.parser;

public class DefaultErrorHandler implements ErrorHandler {
   public void error(ParseException e) throws ParseException {
      throw e;
   }
}
