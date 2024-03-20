package org.apache.batik.ext.awt.image.rendered;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class ColorMatrixRed extends AbstractRed {
   private float[][] matrix;

   public float[][] getMatrix() {
      return this.copyMatrix(this.matrix);
   }

   public void setMatrix(float[][] matrix) {
      float[][] tmp = this.copyMatrix(matrix);
      if (tmp == null) {
         throw new IllegalArgumentException();
      } else if (tmp.length != 4) {
         throw new IllegalArgumentException();
      } else {
         for(int i = 0; i < 4; ++i) {
            if (tmp[i].length != 5) {
               throw new IllegalArgumentException(i + " : " + tmp[i].length);
            }
         }

         this.matrix = matrix;
      }
   }

   private float[][] copyMatrix(float[][] m) {
      if (m == null) {
         return (float[][])null;
      } else {
         float[][] cm = new float[m.length][];

         for(int i = 0; i < m.length; ++i) {
            if (m[i] != null) {
               cm[i] = new float[m[i].length];
               System.arraycopy(m[i], 0, cm[i], 0, m[i].length);
            }
         }

         return cm;
      }
   }

   public ColorMatrixRed(CachableRed src, float[][] matrix) {
      this.setMatrix(matrix);
      ColorModel srcCM = src.getColorModel();
      ColorSpace srcCS = null;
      if (srcCM != null) {
         srcCS = srcCM.getColorSpace();
      }

      ColorModel cm;
      if (srcCS == null) {
         cm = GraphicsUtil.Linear_sRGB_Unpre;
      } else if (srcCS == ColorSpace.getInstance(1004)) {
         cm = GraphicsUtil.Linear_sRGB_Unpre;
      } else {
         cm = GraphicsUtil.sRGB_Unpre;
      }

      SampleModel sm = cm.createCompatibleSampleModel(src.getWidth(), src.getHeight());
      this.init(src, src.getBounds(), cm, sm, src.getTileGridXOffset(), src.getTileGridYOffset(), (Map)null);
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      wr = src.copyData(wr);
      ColorModel cm = src.getColorModel();
      GraphicsUtil.coerceData(wr, cm, false);
      int minX = wr.getMinX();
      int minY = wr.getMinY();
      int w = wr.getWidth();
      int h = wr.getHeight();
      DataBufferInt dbf = (DataBufferInt)wr.getDataBuffer();
      int[] pixels = dbf.getBankData()[0];
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
      int offset = dbf.getOffset() + sppsm.getOffset(minX - wr.getSampleModelTranslateX(), minY - wr.getSampleModelTranslateY());
      int scanStride = ((SinglePixelPackedSampleModel)wr.getSampleModel()).getScanlineStride();
      int adjust = scanStride - w;
      int p = offset;
      int i = false;
      int j = false;
      float a00 = this.matrix[0][0] / 255.0F;
      float a01 = this.matrix[0][1] / 255.0F;
      float a02 = this.matrix[0][2] / 255.0F;
      float a03 = this.matrix[0][3] / 255.0F;
      float a04 = this.matrix[0][4] / 255.0F;
      float a10 = this.matrix[1][0] / 255.0F;
      float a11 = this.matrix[1][1] / 255.0F;
      float a12 = this.matrix[1][2] / 255.0F;
      float a13 = this.matrix[1][3] / 255.0F;
      float a14 = this.matrix[1][4] / 255.0F;
      float a20 = this.matrix[2][0] / 255.0F;
      float a21 = this.matrix[2][1] / 255.0F;
      float a22 = this.matrix[2][2] / 255.0F;
      float a23 = this.matrix[2][3] / 255.0F;
      float a24 = this.matrix[2][4] / 255.0F;
      float a30 = this.matrix[3][0] / 255.0F;
      float a31 = this.matrix[3][1] / 255.0F;
      float a32 = this.matrix[3][2] / 255.0F;
      float a33 = this.matrix[3][3] / 255.0F;
      float a34 = this.matrix[3][4] / 255.0F;

      for(int i = 0; i < h; ++i) {
         for(int j = 0; j < w; ++j) {
            int pel = pixels[p];
            int a = pel >>> 24;
            int r = pel >> 16 & 255;
            int g = pel >> 8 & 255;
            int b = pel & 255;
            int dr = (int)((a00 * (float)r + a01 * (float)g + a02 * (float)b + a03 * (float)a + a04) * 255.0F);
            int dg = (int)((a10 * (float)r + a11 * (float)g + a12 * (float)b + a13 * (float)a + a14) * 255.0F);
            int db = (int)((a20 * (float)r + a21 * (float)g + a22 * (float)b + a23 * (float)a + a24) * 255.0F);
            int da = (int)((a30 * (float)r + a31 * (float)g + a32 * (float)b + a33 * (float)a + a34) * 255.0F);
            if ((dr & -256) != 0) {
               dr = (dr & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            if ((dg & -256) != 0) {
               dg = (dg & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            if ((db & -256) != 0) {
               db = (db & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            if ((da & -256) != 0) {
               da = (da & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            pixels[p++] = da << 24 | dr << 16 | dg << 8 | db;
         }

         p += adjust;
      }

      return wr;
   }
}
