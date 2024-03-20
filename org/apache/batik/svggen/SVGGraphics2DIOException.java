package org.apache.batik.svggen;

import java.io.IOException;

public class SVGGraphics2DIOException extends IOException {
   private IOException embedded;

   public SVGGraphics2DIOException(String s) {
      this(s, (IOException)null);
   }

   public SVGGraphics2DIOException(IOException ex) {
      this((String)null, ex);
   }

   public SVGGraphics2DIOException(String s, IOException ex) {
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

   public IOException getException() {
      return this.embedded;
   }
}
