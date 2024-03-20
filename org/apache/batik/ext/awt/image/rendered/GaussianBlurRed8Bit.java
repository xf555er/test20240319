package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class GaussianBlurRed8Bit extends AbstractRed {
   int xinset;
   int yinset;
   double stdDevX;
   double stdDevY;
   RenderingHints hints;
   ConvolveOp[] convOp;
   int dX;
   int dY;
   static final float SQRT2PI = (float)Math.sqrt(6.283185307179586);
   static final float DSQRT2PI;
   static final float precision = 0.499F;

   public GaussianBlurRed8Bit(CachableRed src, double stdDev, RenderingHints rh) {
      this(src, stdDev, stdDev, rh);
   }

   public GaussianBlurRed8Bit(CachableRed src, double stdDevX, double stdDevY, RenderingHints rh) {
      this.convOp = new ConvolveOp[2];
      this.stdDevX = stdDevX;
      this.stdDevY = stdDevY;
      this.hints = rh;
      this.xinset = surroundPixels(stdDevX, rh);
      this.yinset = surroundPixels(stdDevY, rh);
      Rectangle myBounds = src.getBounds();
      myBounds.x += this.xinset;
      myBounds.y += this.yinset;
      myBounds.width -= 2 * this.xinset;
      myBounds.height -= 2 * this.yinset;
      if (myBounds.width <= 0 || myBounds.height <= 0) {
         myBounds.width = 0;
         myBounds.height = 0;
      }

      ColorModel cm = fixColorModel(src);
      SampleModel sm = src.getSampleModel();
      int tw = sm.getWidth();
      int th = sm.getHeight();
      if (tw > myBounds.width) {
         tw = myBounds.width;
      }

      if (th > myBounds.height) {
         th = myBounds.height;
      }

      sm = cm.createCompatibleSampleModel(tw, th);
      this.init(src, myBounds, cm, sm, src.getTileGridXOffset() + this.xinset, src.getTileGridYOffset() + this.yinset, (Map)null);
      boolean highQuality = this.hints != null && RenderingHints.VALUE_RENDER_QUALITY.equals(this.hints.get(RenderingHints.KEY_RENDERING));
      if (this.xinset != 0 && (stdDevX < 2.0 || highQuality)) {
         this.convOp[0] = new ConvolveOp(this.makeQualityKernelX(this.xinset * 2 + 1));
      } else {
         this.dX = (int)Math.floor((double)DSQRT2PI * stdDevX + 0.5);
      }

      if (this.yinset == 0 || !(stdDevY < 2.0) && !highQuality) {
         this.dY = (int)Math.floor((double)DSQRT2PI * stdDevY + 0.5);
      } else {
         this.convOp[1] = new ConvolveOp(this.makeQualityKernelY(this.yinset * 2 + 1));
      }

   }

   public static int surroundPixels(double stdDev) {
      return surroundPixels(stdDev, (RenderingHints)null);
   }

   public static int surroundPixels(double stdDev, RenderingHints hints) {
      boolean highQuality = hints != null && RenderingHints.VALUE_RENDER_QUALITY.equals(hints.get(RenderingHints.KEY_RENDERING));
      if (!(stdDev < 2.0) && !highQuality) {
         int diam = (int)Math.floor((double)DSQRT2PI * stdDev + 0.5);
         return diam % 2 == 0 ? diam - 1 + diam / 2 : diam - 2 + diam / 2;
      } else {
         float areaSum = (float)(0.5 / (stdDev * (double)SQRT2PI));

         int i;
         for(i = 0; areaSum < 0.499F; ++i) {
            areaSum += (float)(Math.pow(Math.E, (double)(-i * i) / (2.0 * stdDev * stdDev)) / (stdDev * (double)SQRT2PI));
         }

         return i;
      }
   }

   private float[] computeQualityKernelData(int len, double stdDev) {
      float[] kernelData = new float[len];
      int mid = len / 2;
      float sum = 0.0F;

      int i;
      for(i = 0; i < len; ++i) {
         kernelData[i] = (float)(Math.pow(Math.E, (double)(-(i - mid) * (i - mid)) / (2.0 * stdDev * stdDev)) / ((double)SQRT2PI * stdDev));
         sum += kernelData[i];
      }

      for(i = 0; i < len; ++i) {
         kernelData[i] /= sum;
      }

      return kernelData;
   }

   private Kernel makeQualityKernelX(int len) {
      return new Kernel(len, 1, this.computeQualityKernelData(len, this.stdDevX));
   }

   private Kernel makeQualityKernelY(int len) {
      return new Kernel(1, len, this.computeQualityKernelData(len, this.stdDevY));
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      Rectangle r = wr.getBounds();
      r.x -= this.xinset;
      r.y -= this.yinset;
      r.width += 2 * this.xinset;
      r.height += 2 * this.yinset;
      ColorModel srcCM = src.getColorModel();
      WritableRaster tmpR1 = null;
      WritableRaster tmpR2 = null;
      tmpR1 = srcCM.createCompatibleWritableRaster(r.width, r.height);
      WritableRaster fill = tmpR1.createWritableTranslatedChild(r.x, r.y);
      src.copyData(fill);
      if (srcCM.hasAlpha() && !srcCM.isAlphaPremultiplied()) {
         GraphicsUtil.coerceData(tmpR1, srcCM, true);
      }

      int skipX;
      if (this.xinset == 0) {
         skipX = 0;
      } else if (this.convOp[0] != null) {
         tmpR2 = this.getColorModel().createCompatibleWritableRaster(r.width, r.height);
         tmpR2 = this.convOp[0].filter(tmpR1, tmpR2);
         skipX = this.convOp[0].getKernel().getXOrigin();
         WritableRaster tmp = tmpR1;
         tmpR1 = tmpR2;
         tmpR2 = tmp;
      } else if ((this.dX & 1) == 0) {
         tmpR1 = this.boxFilterH(tmpR1, tmpR1, 0, 0, this.dX, this.dX / 2);
         tmpR1 = this.boxFilterH(tmpR1, tmpR1, this.dX / 2, 0, this.dX, this.dX / 2 - 1);
         tmpR1 = this.boxFilterH(tmpR1, tmpR1, this.dX - 1, 0, this.dX + 1, this.dX / 2);
         skipX = this.dX - 1 + this.dX / 2;
      } else {
         tmpR1 = this.boxFilterH(tmpR1, tmpR1, 0, 0, this.dX, this.dX / 2);
         tmpR1 = this.boxFilterH(tmpR1, tmpR1, this.dX / 2, 0, this.dX, this.dX / 2);
         tmpR1 = this.boxFilterH(tmpR1, tmpR1, this.dX - 2, 0, this.dX, this.dX / 2);
         skipX = this.dX - 2 + this.dX / 2;
      }

      if (this.yinset == 0) {
         tmpR2 = tmpR1;
      } else if (this.convOp[1] != null) {
         if (tmpR2 == null) {
            tmpR2 = this.getColorModel().createCompatibleWritableRaster(r.width, r.height);
         }

         tmpR2 = this.convOp[1].filter(tmpR1, tmpR2);
      } else {
         if ((this.dY & 1) == 0) {
            tmpR1 = this.boxFilterV(tmpR1, tmpR1, skipX, 0, this.dY, this.dY / 2);
            tmpR1 = this.boxFilterV(tmpR1, tmpR1, skipX, this.dY / 2, this.dY, this.dY / 2 - 1);
            tmpR1 = this.boxFilterV(tmpR1, tmpR1, skipX, this.dY - 1, this.dY + 1, this.dY / 2);
         } else {
            tmpR1 = this.boxFilterV(tmpR1, tmpR1, skipX, 0, this.dY, this.dY / 2);
            tmpR1 = this.boxFilterV(tmpR1, tmpR1, skipX, this.dY / 2, this.dY, this.dY / 2);
            tmpR1 = this.boxFilterV(tmpR1, tmpR1, skipX, this.dY - 2, this.dY, this.dY / 2);
         }

         tmpR2 = tmpR1;
      }

      tmpR2 = tmpR2.createWritableTranslatedChild(r.x, r.y);
      GraphicsUtil.copyData((Raster)tmpR2, (WritableRaster)wr);
      return wr;
   }

   private WritableRaster boxFilterH(Raster src, WritableRaster dest, int skipX, int skipY, int boxSz, int loc) {
      int w = src.getWidth();
      int h = src.getHeight();
      if (w < 2 * skipX + boxSz) {
         return dest;
      } else if (h < 2 * skipY) {
         return dest;
      } else {
         SinglePixelPackedSampleModel srcSPPSM = (SinglePixelPackedSampleModel)src.getSampleModel();
         SinglePixelPackedSampleModel dstSPPSM = (SinglePixelPackedSampleModel)dest.getSampleModel();
         int srcScanStride = srcSPPSM.getScanlineStride();
         int dstScanStride = dstSPPSM.getScanlineStride();
         DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
         DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
         int srcOff = srcDB.getOffset() + srcSPPSM.getOffset(src.getMinX() - src.getSampleModelTranslateX(), src.getMinY() - src.getSampleModelTranslateY());
         int dstOff = dstDB.getOffset() + dstSPPSM.getOffset(dest.getMinX() - dest.getSampleModelTranslateX(), dest.getMinY() - dest.getSampleModelTranslateY());
         int[] srcPixels = srcDB.getBankData()[0];
         int[] destPixels = dstDB.getBankData()[0];
         int[] buffer = new int[boxSz];
         int scale = 16777216 / boxSz;

         for(int y = skipY; y < h - skipY; ++y) {
            int sp = srcOff + y * srcScanStride;
            int dp = dstOff + y * dstScanStride;
            int rowEnd = sp + (w - skipX);
            int k = 0;
            int sumA = 0;
            int sumR = 0;
            int sumG = 0;
            int sumB = 0;
            sp += skipX;

            int curr;
            for(int end = sp + boxSz; sp < end; ++sp) {
               curr = buffer[k] = srcPixels[sp];
               sumA += curr >>> 24;
               sumR += curr >> 16 & 255;
               sumG += curr >> 8 & 255;
               sumB += curr & 255;
               ++k;
            }

            dp += skipX + loc;
            int prev = destPixels[dp] = sumA * scale & -16777216 | (sumR * scale & -16777216) >>> 8 | (sumG * scale & -16777216) >>> 16 | (sumB * scale & -16777216) >>> 24;
            ++dp;

            for(k = 0; sp < rowEnd; ++dp) {
               curr = buffer[k];
               if (curr == srcPixels[sp]) {
                  destPixels[dp] = prev;
               } else {
                  sumA -= curr >>> 24;
                  sumR -= curr >> 16 & 255;
                  sumG -= curr >> 8 & 255;
                  sumB -= curr & 255;
                  curr = buffer[k] = srcPixels[sp];
                  sumA += curr >>> 24;
                  sumR += curr >> 16 & 255;
                  sumG += curr >> 8 & 255;
                  sumB += curr & 255;
                  prev = destPixels[dp] = sumA * scale & -16777216 | (sumR * scale & -16777216) >>> 8 | (sumG * scale & -16777216) >>> 16 | (sumB * scale & -16777216) >>> 24;
               }

               k = (k + 1) % boxSz;
               ++sp;
            }
         }

         return dest;
      }
   }

   private WritableRaster boxFilterV(Raster src, WritableRaster dest, int skipX, int skipY, int boxSz, int loc) {
      int w = src.getWidth();
      int h = src.getHeight();
      if (w < 2 * skipX) {
         return dest;
      } else if (h < 2 * skipY + boxSz) {
         return dest;
      } else {
         SinglePixelPackedSampleModel srcSPPSM = (SinglePixelPackedSampleModel)src.getSampleModel();
         SinglePixelPackedSampleModel dstSPPSM = (SinglePixelPackedSampleModel)dest.getSampleModel();
         int srcScanStride = srcSPPSM.getScanlineStride();
         int dstScanStride = dstSPPSM.getScanlineStride();
         DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
         DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
         int srcOff = srcDB.getOffset() + srcSPPSM.getOffset(src.getMinX() - src.getSampleModelTranslateX(), src.getMinY() - src.getSampleModelTranslateY());
         int dstOff = dstDB.getOffset() + dstSPPSM.getOffset(dest.getMinX() - dest.getSampleModelTranslateX(), dest.getMinY() - dest.getSampleModelTranslateY());
         int[] srcPixels = srcDB.getBankData()[0];
         int[] destPixels = dstDB.getBankData()[0];
         int[] buffer = new int[boxSz];
         int scale = 16777216 / boxSz;

         for(int x = skipX; x < w - skipX; ++x) {
            int sp = srcOff + x;
            int dp = dstOff + x;
            int colEnd = sp + (h - skipY) * srcScanStride;
            int k = 0;
            int sumA = 0;
            int sumR = 0;
            int sumG = 0;
            int sumB = 0;
            sp += skipY * srcScanStride;

            int curr;
            for(int end = sp + boxSz * srcScanStride; sp < end; sp += srcScanStride) {
               curr = buffer[k] = srcPixels[sp];
               sumA += curr >>> 24;
               sumR += curr >> 16 & 255;
               sumG += curr >> 8 & 255;
               sumB += curr & 255;
               ++k;
            }

            dp += (skipY + loc) * dstScanStride;
            int prev = destPixels[dp] = sumA * scale & -16777216 | (sumR * scale & -16777216) >>> 8 | (sumG * scale & -16777216) >>> 16 | (sumB * scale & -16777216) >>> 24;
            dp += dstScanStride;

            for(k = 0; sp < colEnd; dp += dstScanStride) {
               curr = buffer[k];
               if (curr == srcPixels[sp]) {
                  destPixels[dp] = prev;
               } else {
                  sumA -= curr >>> 24;
                  sumR -= curr >> 16 & 255;
                  sumG -= curr >> 8 & 255;
                  sumB -= curr & 255;
                  curr = buffer[k] = srcPixels[sp];
                  sumA += curr >>> 24;
                  sumR += curr >> 16 & 255;
                  sumG += curr >> 8 & 255;
                  sumB += curr & 255;
                  prev = destPixels[dp] = sumA * scale & -16777216 | (sumR * scale & -16777216) >>> 8 | (sumG * scale & -16777216) >>> 16 | (sumB * scale & -16777216) >>> 24;
               }

               k = (k + 1) % boxSz;
               sp += srcScanStride;
            }
         }

         return dest;
      }
   }

   protected static ColorModel fixColorModel(CachableRed src) {
      ColorModel cm = src.getColorModel();
      int b = src.getSampleModel().getNumBands();
      int[] masks = new int[4];
      switch (b) {
         case 1:
            masks[0] = 255;
            break;
         case 2:
            masks[0] = 255;
            masks[3] = 65280;
            break;
         case 3:
            masks[0] = 16711680;
            masks[1] = 65280;
            masks[2] = 255;
            break;
         case 4:
            masks[0] = 16711680;
            masks[1] = 65280;
            masks[2] = 255;
            masks[3] = -16777216;
            break;
         default:
            throw new IllegalArgumentException("GaussianBlurRed8Bit only supports one to four band images");
      }

      ColorSpace cs = cm.getColorSpace();
      return new DirectColorModel(cs, 8 * b, masks[0], masks[1], masks[2], masks[3], true, 3);
   }

   static {
      DSQRT2PI = SQRT2PI * 3.0F / 4.0F;
   }
}
