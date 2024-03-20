package org.apache.fop.util;

import java.awt.color.ColorSpace;

public class OCAColorSpace extends ColorSpace {
   private static final long serialVersionUID = 1L;

   protected OCAColorSpace() {
      super(5, 1);
   }

   public float[] fromCIEXYZ(float[] colorvalue) {
      throw new UnsupportedOperationException("Color conversion from CIE XYZ to OCA is not possible");
   }

   public float[] fromRGB(float[] rgbvalue) {
      throw new UnsupportedOperationException("Color conversion from RGB to OCA is not possible");
   }

   public float[] toCIEXYZ(float[] colorvalue) {
      float[] rgb = this.toRGB(colorvalue);
      ColorSpace sRGB = ColorSpace.getInstance(1000);
      return sRGB.toCIEXYZ(rgb);
   }

   public float[] toRGB(float[] colorvalue) {
      int oca = (int)colorvalue[0];
      if (oca == OCAColor.OCAColorValue.BLACK.value) {
         return new float[]{0.0F, 0.0F, 0.0F};
      } else if (oca == OCAColor.OCAColorValue.BLUE.value) {
         return new float[]{0.0F, 0.0F, 1.0F};
      } else if (oca == OCAColor.OCAColorValue.BROWN.value) {
         return new float[]{0.565F, 0.188F, 0.0F};
      } else if (oca == OCAColor.OCAColorValue.CYAN.value) {
         return new float[]{0.0F, 1.0F, 1.0F};
      } else if (oca == OCAColor.OCAColorValue.GREEN.value) {
         return new float[]{0.0F, 1.0F, 0.0F};
      } else if (oca == OCAColor.OCAColorValue.MAGENTA.value) {
         return new float[]{1.0F, 0.0F, 1.0F};
      } else if (oca == OCAColor.OCAColorValue.RED.value) {
         return new float[]{1.0F, 0.0F, 0.0F};
      } else {
         return oca == OCAColor.OCAColorValue.YELLOW.value ? new float[]{1.0F, 1.0F, 0.0F} : null;
      }
   }
}
