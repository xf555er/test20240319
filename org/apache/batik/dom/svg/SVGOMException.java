package org.apache.batik.dom.svg;

import org.w3c.dom.svg.SVGException;

public class SVGOMException extends SVGException {
   public SVGOMException(short code, String message) {
      super(code, message);
   }
}
