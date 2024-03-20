package org.apache.xmlgraphics.java2d.color;

import java.awt.color.ColorSpace;

public final class ColorSpaces {
   private static DeviceCMYKColorSpace deviceCMYK;
   private static CIELabColorSpace cieLabD50;
   private static CIELabColorSpace cieLabD65;
   private static final ColorSpaceOrigin UNKNOWN_ORIGIN = new ColorSpaceOrigin() {
      public String getProfileURI() {
         return null;
      }

      public String getProfileName() {
         return null;
      }
   };

   private ColorSpaces() {
   }

   public static synchronized DeviceCMYKColorSpace getDeviceCMYKColorSpace() {
      if (deviceCMYK == null) {
         deviceCMYK = new DeviceCMYKColorSpace();
      }

      return deviceCMYK;
   }

   public static boolean isDeviceColorSpace(ColorSpace cs) {
      return cs instanceof AbstractDeviceSpecificColorSpace;
   }

   public static synchronized CIELabColorSpace getCIELabColorSpaceD50() {
      if (cieLabD50 == null) {
         cieLabD50 = new CIELabColorSpace(CIELabColorSpace.getD50WhitePoint());
      }

      return cieLabD50;
   }

   public static synchronized CIELabColorSpace getCIELabColorSpaceD65() {
      if (cieLabD65 == null) {
         cieLabD65 = new CIELabColorSpace(CIELabColorSpace.getD65WhitePoint());
      }

      return cieLabD65;
   }

   public static ColorSpaceOrigin getColorSpaceOrigin(ColorSpace cs) {
      return cs instanceof ColorSpaceOrigin ? (ColorSpaceOrigin)cs : UNKNOWN_ORIGIN;
   }
}
