package org.apache.batik.ext.awt.image;

import java.awt.Color;

public abstract class AbstractLight implements Light {
   private double[] color;

   public static final double sRGBToLsRGB(double value) {
      return value <= 0.003928 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
   }

   public double[] getColor(boolean linear) {
      double[] ret = new double[3];
      if (linear) {
         ret[0] = sRGBToLsRGB(this.color[0]);
         ret[1] = sRGBToLsRGB(this.color[1]);
         ret[2] = sRGBToLsRGB(this.color[2]);
      } else {
         ret[0] = this.color[0];
         ret[1] = this.color[1];
         ret[2] = this.color[2];
      }

      return ret;
   }

   public AbstractLight(Color color) {
      this.setColor(color);
   }

   public void setColor(Color newColor) {
      this.color = new double[3];
      this.color[0] = (double)newColor.getRed() / 255.0;
      this.color[1] = (double)newColor.getGreen() / 255.0;
      this.color[2] = (double)newColor.getBlue() / 255.0;
   }

   public boolean isConstant() {
      return true;
   }

   public double[][][] getLightMap(double x, double y, double dx, double dy, int width, int height, double[][][] z) {
      double[][][] L = new double[height][][];

      for(int i = 0; i < height; ++i) {
         L[i] = this.getLightRow(x, y, dx, width, z[i], (double[][])null);
         y += dy;
      }

      return L;
   }

   public double[][] getLightRow(double x, double y, double dx, int width, double[][] z, double[][] lightRow) {
      double[][] ret = lightRow;
      if (lightRow == null) {
         ret = new double[width][3];
      }

      for(int i = 0; i < width; ++i) {
         this.getLight(x, y, z[i][3], ret[i]);
         x += dx;
      }

      return ret;
   }
}
