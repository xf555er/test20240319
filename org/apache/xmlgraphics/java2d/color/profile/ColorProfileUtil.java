package org.apache.xmlgraphics.java2d.color.profile;

import java.awt.color.ICC_Profile;
import java.awt.color.ICC_ProfileRGB;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public final class ColorProfileUtil {
   private ColorProfileUtil() {
   }

   public static String getICCProfileDescription(ICC_Profile profile) {
      byte[] data = profile.getData(1684370275);
      if (data == null) {
         return null;
      } else {
         int length = data[8] << 24 | data[9] << 16 | data[10] << 8 | data[11];
         --length;

         try {
            return new String(data, 12, length, "US-ASCII");
         } catch (UnsupportedEncodingException var4) {
            throw new UnsupportedOperationException("Incompatible VM");
         }
      }
   }

   public static boolean isDefaultsRGB(ICC_Profile profile) {
      if (!(profile instanceof ICC_ProfileRGB)) {
         return false;
      } else {
         ICC_Profile sRGBProfile = ICC_Profile.getInstance(1000);
         if (profile.getProfileClass() != sRGBProfile.getProfileClass()) {
            return false;
         } else if (profile.getMajorVersion() != sRGBProfile.getMajorVersion()) {
            return false;
         } else if (profile.getMinorVersion() != sRGBProfile.getMinorVersion()) {
            return false;
         } else {
            return Arrays.equals(profile.getData(), sRGBProfile.getData());
         }
      }
   }

   public static ICC_Profile getICC_Profile(byte[] data) {
      Class var1 = ICC_Profile.class;
      synchronized(ICC_Profile.class) {
         return ICC_Profile.getInstance(data);
      }
   }

   public static ICC_Profile getICC_Profile(int colorSpace) {
      Class var1 = ICC_Profile.class;
      synchronized(ICC_Profile.class) {
         return ICC_Profile.getInstance(colorSpace);
      }
   }

   public static ICC_Profile getICC_Profile(InputStream in) throws IOException {
      Class var1 = ICC_Profile.class;
      synchronized(ICC_Profile.class) {
         return ICC_Profile.getInstance(in);
      }
   }

   public static ICC_Profile getICC_Profile(String fileName) throws IOException {
      Class var1 = ICC_Profile.class;
      synchronized(ICC_Profile.class) {
         return ICC_Profile.getInstance(fileName);
      }
   }
}
