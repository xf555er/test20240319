package org.apache.xmlgraphics.java2d.color;

import java.awt.Color;
import java.awt.color.ColorSpace;

public class CIELabColorSpace extends ColorSpace {
   private static final long serialVersionUID = -1821569090707520704L;
   private static final float REF_X_D65 = 95.047F;
   private static final float REF_Y_D65 = 100.0F;
   private static final float REF_Z_D65 = 108.883F;
   private static final float REF_X_D50 = 96.42F;
   private static final float REF_Y_D50 = 100.0F;
   private static final float REF_Z_D50 = 82.49F;
   private static final double D = 0.20689655172413793;
   private static final double REF_A = 1.0 / (3.0 * Math.pow(0.20689655172413793, 2.0));
   private static final double REF_B = 0.13793103448275862;
   private static final double T0 = Math.pow(0.20689655172413793, 3.0);
   private float wpX;
   private float wpY;
   private float wpZ;
   private static final String CIE_LAB_ONLY_HAS_3_COMPONENTS = "CIE Lab only has 3 components!";

   public CIELabColorSpace() {
      this(getD65WhitePoint());
   }

   public CIELabColorSpace(float[] whitePoint) {
      super(1, 3);
      this.checkNumComponents(whitePoint, 3);
      this.wpX = whitePoint[0];
      this.wpY = whitePoint[1];
      this.wpZ = whitePoint[2];
   }

   public static float[] getD65WhitePoint() {
      return new float[]{95.047F, 100.0F, 108.883F};
   }

   public static float[] getD50WhitePoint() {
      return new float[]{96.42F, 100.0F, 82.49F};
   }

   private void checkNumComponents(float[] colorvalue) {
      this.checkNumComponents(colorvalue, this.getNumComponents());
   }

   private void checkNumComponents(float[] colorvalue, int expected) {
      if (colorvalue == null) {
         throw new NullPointerException("color value may not be null");
      } else if (colorvalue.length != expected) {
         throw new IllegalArgumentException("Expected " + expected + " components, but got " + colorvalue.length);
      }
   }

   public float[] getWhitePoint() {
      return new float[]{this.wpX, this.wpY, this.wpZ};
   }

   public float getMinValue(int component) {
      switch (component) {
         case 0:
            return 0.0F;
         case 1:
         case 2:
            return -128.0F;
         default:
            throw new IllegalArgumentException("CIE Lab only has 3 components!");
      }
   }

   public float getMaxValue(int component) {
      switch (component) {
         case 0:
            return 100.0F;
         case 1:
         case 2:
            return 128.0F;
         default:
            throw new IllegalArgumentException("CIE Lab only has 3 components!");
      }
   }

   public String getName(int component) {
      switch (component) {
         case 0:
            return "L*";
         case 1:
            return "a*";
         case 2:
            return "b*";
         default:
            throw new IllegalArgumentException("CIE Lab only has 3 components!");
      }
   }

   public float[] fromCIEXYZ(float[] colorvalue) {
      this.checkNumComponents(colorvalue, 3);
      float x = colorvalue[0];
      float y = colorvalue[1];
      float z = colorvalue[2];
      double varX = (double)(x / this.wpX);
      double varY = (double)(y / this.wpY);
      double varZ = (double)(z / this.wpZ);
      if (varX > T0) {
         varX = Math.pow(varX, 0.3333333333333333);
      } else {
         varX = REF_A * varX + 0.13793103448275862;
      }

      if (varY > T0) {
         varY = Math.pow(varY, 0.3333333333333333);
      } else {
         varY = REF_A * varY + 0.13793103448275862;
      }

      if (varZ > T0) {
         varZ = Math.pow(varZ, 0.3333333333333333);
      } else {
         varZ = REF_A * varZ + 0.13793103448275862;
      }

      float l = (float)(116.0 * varY - 16.0);
      float a = (float)(500.0 * (varX - varY));
      float b = (float)(200.0 * (varY - varZ));
      l = this.normalize(l, 0);
      a = this.normalize(a, 1);
      b = this.normalize(b, 2);
      return new float[]{l, a, b};
   }

   public float[] fromRGB(float[] rgbvalue) {
      ColorSpace sRGB = ColorSpace.getInstance(1000);
      float[] xyz = sRGB.toCIEXYZ(rgbvalue);
      return this.fromCIEXYZ(xyz);
   }

   public float[] toCIEXYZ(float[] colorvalue) {
      this.checkNumComponents(colorvalue);
      float l = this.denormalize(colorvalue[0], 0);
      float a = this.denormalize(colorvalue[1], 1);
      float b = this.denormalize(colorvalue[2], 2);
      return this.toCIEXYZNative(l, a, b);
   }

   public float[] toCIEXYZNative(float l, float a, float b) {
      double varY = (double)(l + 16.0F) / 116.0;
      double varX = (double)(a / 500.0F) + varY;
      double varZ = varY - (double)b / 200.0;
      if (Math.pow(varY, 3.0) > T0) {
         varY = Math.pow(varY, 3.0);
      } else {
         varY = (varY - 0.13793103448275862) / REF_A;
      }

      if (Math.pow(varX, 3.0) > T0) {
         varX = Math.pow(varX, 3.0);
      } else {
         varX = (varX - 0.13793103448275862) / REF_A;
      }

      if (Math.pow(varZ, 3.0) > T0) {
         varZ = Math.pow(varZ, 3.0);
      } else {
         varZ = (varZ - 0.13793103448275862) / REF_A;
      }

      float x = (float)((double)this.wpX * varX / 100.0);
      float y = (float)((double)this.wpY * varY / 100.0);
      float z = (float)((double)this.wpZ * varZ / 100.0);
      return new float[]{x, y, z};
   }

   public float[] toRGB(float[] colorvalue) {
      ColorSpace sRGB = ColorSpace.getInstance(1000);
      float[] xyz = this.toCIEXYZ(colorvalue);
      return sRGB.fromCIEXYZ(xyz);
   }

   private float getNativeValueRange(int component) {
      return this.getMaxValue(component) - this.getMinValue(component);
   }

   private float normalize(float value, int component) {
      return (value - this.getMinValue(component)) / this.getNativeValueRange(component);
   }

   private float denormalize(float value, int component) {
      return value * this.getNativeValueRange(component) + this.getMinValue(component);
   }

   public float[] toNativeComponents(float[] comps) {
      this.checkNumComponents(comps);
      float[] nativeComps = new float[comps.length];
      int i = 0;

      for(int c = comps.length; i < c; ++i) {
         nativeComps[i] = this.denormalize(comps[i], i);
      }

      return nativeComps;
   }

   public Color toColor(float[] colorvalue, float alpha) {
      int c = colorvalue.length;
      float[] normalized = new float[c];

      for(int i = 0; i < c; ++i) {
         normalized[i] = this.normalize(colorvalue[i], i);
      }

      return new ColorWithAlternatives(this, normalized, alpha, (Color[])null);
   }

   public Color toColor(float l, float a, float b, float alpha) {
      return this.toColor(new float[]{l, a, b}, alpha);
   }
}
