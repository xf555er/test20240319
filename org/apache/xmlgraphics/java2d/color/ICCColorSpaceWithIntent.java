package org.apache.xmlgraphics.java2d.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

public class ICCColorSpaceWithIntent extends ICC_ColorSpace implements ColorSpaceOrigin {
   private static final long serialVersionUID = -3338065900662625221L;
   static final ColorSpace SRGB = ColorSpace.getInstance(1000);
   private RenderingIntent intent;
   private String profileName;
   private String profileURI;

   public ICCColorSpaceWithIntent(ICC_Profile p, RenderingIntent intent, String profileName, String profileURI) {
      super(p);
      this.intent = intent;
      if (intent != RenderingIntent.AUTO) {
         byte[] hdr = p.getData(1751474532);
         hdr[64] = (byte)intent.getIntegerValue();
      }

      this.profileName = profileName;
      this.profileURI = profileURI;
   }

   public float[] intendedToRGB(float[] values) {
      switch (this.intent) {
         case ABSOLUTE_COLORIMETRIC:
            return this.absoluteColorimetricToRGB(values);
         case PERCEPTUAL:
         case AUTO:
            return this.perceptualToRGB(values);
         case RELATIVE_COLORIMETRIC:
            return this.relativeColorimetricToRGB(values);
         case SATURATION:
            return this.saturationToRGB(values);
         default:
            throw new RuntimeException("invalid intent:" + this.intent);
      }
   }

   private float[] perceptualToRGB(float[] values) {
      return this.toRGB(values);
   }

   private float[] relativeColorimetricToRGB(float[] values) {
      float[] ciexyz = this.toCIEXYZ(values);
      return SRGB.fromCIEXYZ(ciexyz);
   }

   private float[] absoluteColorimetricToRGB(float[] values) {
      return this.perceptualToRGB(values);
   }

   private float[] saturationToRGB(float[] values) {
      return this.perceptualToRGB(values);
   }

   public String getProfileName() {
      return this.profileName;
   }

   public String getProfileURI() {
      return this.profileURI;
   }
}
