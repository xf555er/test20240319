package org.apache.batik.transcoder;

public class DefaultErrorHandler implements ErrorHandler {
   public void error(TranscoderException ex) throws TranscoderException {
      System.err.println("ERROR: " + ex.getMessage());
   }

   public void fatalError(TranscoderException ex) throws TranscoderException {
      throw ex;
   }

   public void warning(TranscoderException ex) throws TranscoderException {
      System.err.println("WARNING: " + ex.getMessage());
   }
}
