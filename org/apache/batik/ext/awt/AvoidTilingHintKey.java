package org.apache.batik.ext.awt;

import java.awt.RenderingHints;

public class AvoidTilingHintKey extends RenderingHints.Key {
   AvoidTilingHintKey(int number) {
      super(number);
   }

   public boolean isCompatibleValue(Object v) {
      if (v == null) {
         return false;
      } else {
         return v == RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_ON || v == RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_OFF || v == RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_DEFAULT;
      }
   }
}
