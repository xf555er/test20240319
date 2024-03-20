package org.apache.batik.transcoder.keys;

import org.apache.batik.transcoder.TranscodingHints;

public class LengthKey extends TranscodingHints.Key {
   public boolean isCompatibleValue(Object v) {
      return v instanceof Float && (Float)v > 0.0F;
   }
}
