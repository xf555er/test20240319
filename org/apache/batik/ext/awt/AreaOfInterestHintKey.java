package org.apache.batik.ext.awt;

import java.awt.RenderingHints;
import java.awt.Shape;

final class AreaOfInterestHintKey extends RenderingHints.Key {
   AreaOfInterestHintKey(int number) {
      super(number);
   }

   public boolean isCompatibleValue(Object val) {
      boolean isCompatible = true;
      if (val != null && !(val instanceof Shape)) {
         isCompatible = false;
      }

      return isCompatible;
   }
}
