package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.Light;

public class DiffuseLightingRed extends AbstractRed {
   private double kd;
   private Light light;
   private BumpMap bumpMap;
   private double scaleX;
   private double scaleY;
   private Rectangle litRegion;
   private boolean linear;

   public DiffuseLightingRed(double kd, Light light, BumpMap bumpMap, Rectangle litRegion, double scaleX, double scaleY, boolean linear) {
      this.kd = kd;
      this.light = light;
      this.bumpMap = bumpMap;
      this.litRegion = litRegion;
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.linear = linear;
      ColorModel cm;
      if (linear) {
         cm = GraphicsUtil.Linear_sRGB_Pre;
      } else {
         cm = GraphicsUtil.sRGB_Pre;
      }

      SampleModel sm = cm.createCompatibleSampleModel(litRegion.width, litRegion.height);
      this.init((CachableRed)null, litRegion, cm, sm, litRegion.x, litRegion.y, (Map)null);
   }

   public WritableRaster copyData(WritableRaster wr) {
      double[] lightColor = this.light.getColor(this.linear);
      int w = wr.getWidth();
      int h = wr.getHeight();
      int minX = wr.getMinX();
      int minY = wr.getMinY();
      DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
      int[] pixels = db.getBankData()[0];
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
      int offset = db.getOffset() + sppsm.getOffset(minX - wr.getSampleModelTranslateX(), minY - wr.getSampleModelTranslateY());
      int scanStride = sppsm.getScanlineStride();
      int adjust = scanStride - w;
      int p = offset;
      int r = false;
      int g = false;
      int b = false;
      int i = false;
      int j = false;
      double x = this.scaleX * (double)minX;
      double y = this.scaleY * (double)minY;
      double NL = 0.0;
      double[][][] NA = this.bumpMap.getNormalArray(minX, minY, w, h);
      double[][] NR;
      double[] N;
      int r;
      int g;
      int b;
      int i;
      int j;
      if (!this.light.isConstant()) {
         double[][] LA = new double[w][3];

         for(i = 0; i < h; ++i) {
            NR = NA[i];
            this.light.getLightRow(x, y + (double)i * this.scaleY, this.scaleX, w, NR, LA);

            for(j = 0; j < w; ++j) {
               N = NR[j];
               double[] L = LA[j];
               NL = 255.0 * this.kd * (N[0] * L[0] + N[1] * L[1] + N[2] * L[2]);
               r = (int)(NL * lightColor[0]);
               g = (int)(NL * lightColor[1]);
               b = (int)(NL * lightColor[2]);
               if ((r & -256) != 0) {
                  r = (r & Integer.MIN_VALUE) != 0 ? 0 : 255;
               }

               if ((g & -256) != 0) {
                  g = (g & Integer.MIN_VALUE) != 0 ? 0 : 255;
               }

               if ((b & -256) != 0) {
                  b = (b & Integer.MIN_VALUE) != 0 ? 0 : 255;
               }

               pixels[p++] = -16777216 | r << 16 | g << 8 | b;
            }

            p += adjust;
         }
      } else {
         double[] L = new double[3];
         this.light.getLight(0.0, 0.0, 0.0, L);

         for(i = 0; i < h; ++i) {
            NR = NA[i];

            for(j = 0; j < w; ++j) {
               N = NR[j];
               NL = 255.0 * this.kd * (N[0] * L[0] + N[1] * L[1] + N[2] * L[2]);
               r = (int)(NL * lightColor[0]);
               g = (int)(NL * lightColor[1]);
               b = (int)(NL * lightColor[2]);
               if ((r & -256) != 0) {
                  r = (r & Integer.MIN_VALUE) != 0 ? 0 : 255;
               }

               if ((g & -256) != 0) {
                  g = (g & Integer.MIN_VALUE) != 0 ? 0 : 255;
               }

               if ((b & -256) != 0) {
                  b = (b & Integer.MIN_VALUE) != 0 ? 0 : 255;
               }

               pixels[p++] = -16777216 | r << 16 | g << 8 | b;
            }

            p += adjust;
         }
      }

      return wr;
   }
}
