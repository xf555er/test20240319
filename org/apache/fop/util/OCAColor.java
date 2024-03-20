package org.apache.fop.util;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class OCAColor extends Color {
   private static final long serialVersionUID = 1L;

   public OCAColor(OCAColorValue oca) {
      super(oca.value);
   }

   public int getOCA() {
      return this.getRGB() & '\uffff';
   }

   public ColorSpace getColorSpace() {
      return new OCAColorSpace();
   }

   public float[] getColorComponents(ColorSpace cspace, float[] compArray) {
      if (cspace.isCS_sRGB()) {
         ColorSpace oca = new OCAColorSpace();
         return oca.toRGB(new float[]{(float)this.getOCA()});
      } else {
         return null;
      }
   }

   public static enum OCAColorValue {
      BLUE(1),
      RED(2),
      MAGENTA(3),
      GREEN(4),
      CYAN(5),
      YELLOW(6),
      BLACK(8),
      BROWN(16),
      DEVICE_DEFAULT(65287),
      MEDIUM_COLOR(65288);

      final int value;

      private OCAColorValue(int value) {
         this.value = value;
      }
   }
}
