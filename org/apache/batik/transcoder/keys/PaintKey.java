package org.apache.batik.transcoder.keys;

import java.awt.Paint;
import org.apache.batik.transcoder.TranscodingHints;

public class PaintKey extends TranscodingHints.Key {
   public boolean isCompatibleValue(Object v) {
      return v instanceof Paint;
   }
}
