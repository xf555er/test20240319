package org.apache.xmlgraphics.java2d.color;

public enum RenderingIntent {
   PERCEPTUAL(0),
   RELATIVE_COLORIMETRIC(1),
   ABSOLUTE_COLORIMETRIC(3),
   SATURATION(2),
   AUTO(4);

   private int intValue;

   private RenderingIntent(int value) {
      this.intValue = value;
   }

   public int getIntegerValue() {
      return this.intValue;
   }

   public static RenderingIntent fromICCValue(int value) {
      switch (value) {
         case 0:
            return PERCEPTUAL;
         case 1:
            return RELATIVE_COLORIMETRIC;
         case 2:
            return SATURATION;
         case 3:
            return ABSOLUTE_COLORIMETRIC;
         default:
            throw new IllegalArgumentException("Invalid value for rendering intent: " + value);
      }
   }
}
