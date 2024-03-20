package org.apache.batik.transcoder.keys;

import org.apache.batik.transcoder.TranscodingHints;
import org.w3c.dom.DOMImplementation;

public class DOMImplementationKey extends TranscodingHints.Key {
   public boolean isCompatibleValue(Object v) {
      return v instanceof DOMImplementation;
   }
}
