package org.apache.batik.ext.awt.image;

import java.awt.Color;

public class SpotLight extends AbstractLight {
   private double lightX;
   private double lightY;
   private double lightZ;
   private double pointAtX;
   private double pointAtY;
   private double pointAtZ;
   private double specularExponent;
   private double limitingConeAngle;
   private double limitingCos;
   private final double[] S = new double[3];

   public double getLightX() {
      return this.lightX;
   }

   public double getLightY() {
      return this.lightY;
   }

   public double getLightZ() {
      return this.lightZ;
   }

   public double getPointAtX() {
      return this.pointAtX;
   }

   public double getPointAtY() {
      return this.pointAtY;
   }

   public double getPointAtZ() {
      return this.pointAtZ;
   }

   public double getSpecularExponent() {
      return this.specularExponent;
   }

   public double getLimitingConeAngle() {
      return this.limitingConeAngle;
   }

   public SpotLight(double lightX, double lightY, double lightZ, double pointAtX, double pointAtY, double pointAtZ, double specularExponent, double limitingConeAngle, Color lightColor) {
      super(lightColor);
      this.lightX = lightX;
      this.lightY = lightY;
      this.lightZ = lightZ;
      this.pointAtX = pointAtX;
      this.pointAtY = pointAtY;
      this.pointAtZ = pointAtZ;
      this.specularExponent = specularExponent;
      this.limitingConeAngle = limitingConeAngle;
      this.limitingCos = Math.cos(Math.toRadians(limitingConeAngle));
      this.S[0] = pointAtX - lightX;
      this.S[1] = pointAtY - lightY;
      this.S[2] = pointAtZ - lightZ;
      double invNorm = 1.0 / Math.sqrt(this.S[0] * this.S[0] + this.S[1] * this.S[1] + this.S[2] * this.S[2]);
      double[] var10000 = this.S;
      var10000[0] *= invNorm;
      var10000 = this.S;
      var10000[1] *= invNorm;
      var10000 = this.S;
      var10000[2] *= invNorm;
   }

   public boolean isConstant() {
      return false;
   }

   public final double getLightBase(double x, double y, double z, double[] L) {
      double L0 = this.lightX - x;
      double L1 = this.lightY - y;
      double L2 = this.lightZ - z;
      double invNorm = 1.0 / Math.sqrt(L0 * L0 + L1 * L1 + L2 * L2);
      L0 *= invNorm;
      L1 *= invNorm;
      L2 *= invNorm;
      double LS = -(L0 * this.S[0] + L1 * this.S[1] + L2 * this.S[2]);
      L[0] = L0;
      L[1] = L1;
      L[2] = L2;
      if (LS <= this.limitingCos) {
         return 0.0;
      } else {
         double Iatt = this.limitingCos / LS;
         Iatt *= Iatt;
         Iatt *= Iatt;
         Iatt *= Iatt;
         Iatt *= Iatt;
         Iatt *= Iatt;
         Iatt *= Iatt;
         Iatt = 1.0 - Iatt;
         return Iatt * Math.pow(LS, this.specularExponent);
      }
   }

   public final void getLight(double x, double y, double z, double[] L) {
      double s = this.getLightBase(x, y, z, L);
      L[0] *= s;
      L[1] *= s;
      L[2] *= s;
   }

   public final void getLight4(double x, double y, double z, double[] L) {
      L[3] = this.getLightBase(x, y, z, L);
   }

   public double[][] getLightRow4(double x, double y, double dx, int width, double[][] z, double[][] lightRow) {
      double[][] ret = lightRow;
      if (lightRow == null) {
         ret = new double[width][4];
      }

      for(int i = 0; i < width; ++i) {
         this.getLight4(x, y, z[i][3], ret[i]);
         x += dx;
      }

      return ret;
   }
}
