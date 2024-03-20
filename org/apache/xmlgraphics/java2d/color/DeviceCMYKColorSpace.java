package org.apache.xmlgraphics.java2d.color;

import java.awt.Color;

public class DeviceCMYKColorSpace extends AbstractDeviceSpecificColorSpace implements ColorSpaceOrigin {
   private static final long serialVersionUID = 2925508946083542974L;
   public static final String PSEUDO_PROFILE_NAME = "#CMYK";

   public DeviceCMYKColorSpace() {
      super(9, 4);
   }

   /** @deprecated */
   @Deprecated
   public static DeviceCMYKColorSpace getInstance() {
      return ColorSpaces.getDeviceCMYKColorSpace();
   }

   public float[] toRGB(float[] colorvalue) {
      return new float[]{(1.0F - colorvalue[0]) * (1.0F - colorvalue[3]), (1.0F - colorvalue[1]) * (1.0F - colorvalue[3]), (1.0F - colorvalue[2]) * (1.0F - colorvalue[3])};
   }

   public float[] fromRGB(float[] rgbvalue) {
      assert rgbvalue.length == 3;

      float r = rgbvalue[0];
      float g = rgbvalue[1];
      float b = rgbvalue[2];
      if (r == g && r == b) {
         return new float[]{0.0F, 0.0F, 0.0F, 1.0F - r};
      } else {
         float c = 1.0F - r;
         float m = 1.0F - g;
         float y = 1.0F - b;
         float k = Math.min(c, Math.min(m, y));
         return new float[]{c, m, y, k};
      }
   }

   public float[] toCIEXYZ(float[] colorvalue) {
      throw new UnsupportedOperationException("NYI");
   }

   public float[] fromCIEXYZ(float[] colorvalue) {
      throw new UnsupportedOperationException("NYI");
   }

   public static Color createCMYKColor(float[] cmykComponents) {
      DeviceCMYKColorSpace cmykCs = ColorSpaces.getDeviceCMYKColorSpace();
      Color cmykColor = new ColorWithAlternatives(cmykCs, cmykComponents, 1.0F, (Color[])null);
      return cmykColor;
   }

   public String getProfileName() {
      return "#CMYK";
   }

   public String getProfileURI() {
      return null;
   }
}
