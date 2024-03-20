package org.apache.fop.util;

import java.awt.color.ICC_Profile;

/** @deprecated */
public final class ColorProfileUtil {
   private ColorProfileUtil() {
   }

   /** @deprecated */
   public static String getICCProfileDescription(ICC_Profile profile) {
      return org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil.getICCProfileDescription(profile);
   }

   /** @deprecated */
   public static boolean isDefaultsRGB(ICC_Profile profile) {
      return org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil.isDefaultsRGB(profile);
   }
}
