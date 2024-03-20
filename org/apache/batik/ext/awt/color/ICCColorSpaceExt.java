package org.apache.batik.ext.awt.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/** @deprecated */
public class ICCColorSpaceExt extends ICC_ColorSpace {
   public static final int PERCEPTUAL = 0;
   public static final int RELATIVE_COLORIMETRIC = 1;
   public static final int ABSOLUTE_COLORIMETRIC = 2;
   public static final int SATURATION = 3;
   public static final int AUTO = 4;
   static final ColorSpace sRGB = ColorSpace.getInstance(1000);
   int intent;

   public ICCColorSpaceExt(ICC_Profile p, int intent) {
      super(p);
      this.intent = intent;
      switch (intent) {
         case 0:
         case 1:
         case 2:
         case 3:
         case 4:
            if (intent != 4) {
               byte[] hdr = p.getData(1751474532);
               hdr[64] = (byte)intent;
            }

            return;
         default:
            throw new IllegalArgumentException();
      }
   }

   public float[] intendedToRGB(float[] values) {
      switch (this.intent) {
         case 0:
         case 4:
            return this.perceptualToRGB(values);
         case 1:
            return this.relativeColorimetricToRGB(values);
         case 2:
            return this.absoluteColorimetricToRGB(values);
         case 3:
            return this.saturationToRGB(values);
         default:
            throw new RuntimeException("invalid intent:" + this.intent);
      }
   }

   public float[] perceptualToRGB(float[] values) {
      return this.toRGB(values);
   }

   public float[] relativeColorimetricToRGB(float[] values) {
      float[] ciexyz = this.toCIEXYZ(values);
      return sRGB.fromCIEXYZ(ciexyz);
   }

   public float[] absoluteColorimetricToRGB(float[] values) {
      return this.perceptualToRGB(values);
   }

   public float[] saturationToRGB(float[] values) {
      return this.perceptualToRGB(values);
   }
}
