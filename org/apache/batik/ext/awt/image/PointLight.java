package org.apache.batik.ext.awt.image;

import java.awt.Color;

public class PointLight extends AbstractLight {
   private double lightX;
   private double lightY;
   private double lightZ;

   public double getLightX() {
      return this.lightX;
   }

   public double getLightY() {
      return this.lightY;
   }

   public double getLightZ() {
      return this.lightZ;
   }

   public PointLight(double lightX, double lightY, double lightZ, Color lightColor) {
      super(lightColor);
      this.lightX = lightX;
      this.lightY = lightY;
      this.lightZ = lightZ;
   }

   public boolean isConstant() {
      return false;
   }

   public final void getLight(double x, double y, double z, double[] L) {
      double L0 = this.lightX - x;
      double L1 = this.lightY - y;
      double L2 = this.lightZ - z;
      double norm = Math.sqrt(L0 * L0 + L1 * L1 + L2 * L2);
      if (norm > 0.0) {
         double invNorm = 1.0 / norm;
         L0 *= invNorm;
         L1 *= invNorm;
         L2 *= invNorm;
      }

      L[0] = L0;
      L[1] = L1;
      L[2] = L2;
   }
}
