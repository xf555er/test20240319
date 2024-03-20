package org.apache.xmlgraphics.image.codec.tiff;

import java.awt.image.ColorModel;

enum ExtraSamplesType {
   UNSPECIFIED(0),
   ASSOCIATED_ALPHA(1),
   UNASSOCIATED_ALPHA(2);

   private final int typeValue;

   private ExtraSamplesType(int value) {
      this.typeValue = value;
   }

   static ExtraSamplesType getValue(ColorModel colorModel, int numExtraSamples) {
      if (numExtraSamples == 1 && colorModel.hasAlpha()) {
         return colorModel.isAlphaPremultiplied() ? ASSOCIATED_ALPHA : UNASSOCIATED_ALPHA;
      } else {
         return UNSPECIFIED;
      }
   }

   int getValue() {
      return this.typeValue;
   }
}
