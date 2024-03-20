package org.apache.batik.transcoder.keys;

import org.apache.batik.transcoder.TranscodingHints;

public class BooleanKey extends TranscodingHints.Key {
   public boolean isCompatibleValue(Object v) {
      return v != null && v instanceof Boolean;
   }
}
