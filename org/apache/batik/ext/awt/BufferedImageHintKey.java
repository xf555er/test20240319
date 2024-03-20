package org.apache.batik.ext.awt;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.ref.Reference;

final class BufferedImageHintKey extends RenderingHints.Key {
   BufferedImageHintKey(int number) {
      super(number);
   }

   public boolean isCompatibleValue(Object val) {
      if (val == null) {
         return true;
      } else if (!(val instanceof Reference)) {
         return false;
      } else {
         Reference ref = (Reference)val;
         val = ref.get();
         if (val == null) {
            return true;
         } else {
            return val instanceof BufferedImage;
         }
      }
   }
}
