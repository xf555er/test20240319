package org.apache.batik.svggen;

public class SVGGraphics2DRuntimeException extends RuntimeException {
   private Exception embedded;

   public SVGGraphics2DRuntimeException(String s) {
      this(s, (Exception)null);
   }

   public SVGGraphics2DRuntimeException(Exception ex) {
      this((String)null, ex);
   }

   public SVGGraphics2DRuntimeException(String s, Exception ex) {
      super(s);
      this.embedded = ex;
   }

   public String getMessage() {
      String msg = super.getMessage();
      if (msg != null) {
         return msg;
      } else {
         return this.embedded != null ? this.embedded.getMessage() : null;
      }
   }

   public Exception getException() {
      return this.embedded;
   }
}
