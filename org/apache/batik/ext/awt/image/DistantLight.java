package org.apache.batik.ext.awt.image;

import java.awt.Color;

public class DistantLight extends AbstractLight {
   private double azimuth;
   private double elevation;
   private double Lx;
   private double Ly;
   private double Lz;

   public double getAzimuth() {
      return this.azimuth;
   }

   public double getElevation() {
      return this.elevation;
   }

   public DistantLight(double azimuth, double elevation, Color color) {
      super(color);
      this.azimuth = azimuth;
      this.elevation = elevation;
      this.Lx = Math.cos(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation));
      this.Ly = Math.sin(Math.toRadians(azimuth)) * Math.cos(Math.toRadians(elevation));
      this.Lz = Math.sin(Math.toRadians(elevation));
   }

   public boolean isConstant() {
      return true;
   }

   public void getLight(double x, double y, double z, double[] L) {
      L[0] = this.Lx;
      L[1] = this.Ly;
      L[2] = this.Lz;
   }

   public double[][] getLightRow(double x, double y, double dx, int width, double[][] z, double[][] lightRow) {
      double[][] ret = lightRow;
      if (lightRow == null) {
         ret = new double[width][];
         double[] CL = new double[]{this.Lx, this.Ly, this.Lz};

         for(int i = 0; i < width; ++i) {
            ret[i] = CL;
         }
      } else {
         double lx = this.Lx;
         double ly = this.Ly;
         double lz = this.Lz;

         for(int i = 0; i < width; ++i) {
            ret[i][0] = lx;
            ret[i][1] = ly;
            ret[i][2] = lz;
         }
      }

      return ret;
   }
}
