package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class MorphologyOp implements BufferedImageOp, RasterOp {
   private int radiusX;
   private int radiusY;
   private boolean doDilation;
   private final int rangeX;
   private final int rangeY;
   private final ColorSpace sRGB = ColorSpace.getInstance(1000);
   private final ColorSpace lRGB = ColorSpace.getInstance(1004);

   public MorphologyOp(int radiusX, int radiusY, boolean doDilation) {
      if (radiusX > 0 && radiusY > 0) {
         this.radiusX = radiusX;
         this.radiusY = radiusY;
         this.doDilation = doDilation;
         this.rangeX = 2 * radiusX + 1;
         this.rangeY = 2 * radiusY + 1;
      } else {
         throw new IllegalArgumentException("The radius of X-axis or Y-axis should not be Zero or Negatives.");
      }
   }

   public Rectangle2D getBounds2D(Raster src) {
      this.checkCompatible(src.getSampleModel());
      return new Rectangle(src.getMinX(), src.getMinY(), src.getWidth(), src.getHeight());
   }

   public Rectangle2D getBounds2D(BufferedImage src) {
      return new Rectangle(0, 0, src.getWidth(), src.getHeight());
   }

   public Point2D getPoint2D(Point2D srcPt, Point2D destPt) {
      if (destPt == null) {
         destPt = new Point2D.Float();
      }

      ((Point2D)destPt).setLocation(srcPt.getX(), srcPt.getY());
      return (Point2D)destPt;
   }

   private void checkCompatible(ColorModel colorModel, SampleModel sampleModel) {
      ColorSpace cs = colorModel.getColorSpace();
      if (!cs.equals(this.sRGB) && !cs.equals(this.lRGB)) {
         throw new IllegalArgumentException("Expected CS_sRGB or CS_LINEAR_RGB color model");
      } else if (!(colorModel instanceof DirectColorModel)) {
         throw new IllegalArgumentException("colorModel should be an instance of DirectColorModel");
      } else if (sampleModel.getDataType() != 3) {
         throw new IllegalArgumentException("colorModel's transferType should be DataBuffer.TYPE_INT");
      } else {
         DirectColorModel dcm = (DirectColorModel)colorModel;
         if (dcm.getRedMask() != 16711680) {
            throw new IllegalArgumentException("red mask in source should be 0x00ff0000");
         } else if (dcm.getGreenMask() != 65280) {
            throw new IllegalArgumentException("green mask in source should be 0x0000ff00");
         } else if (dcm.getBlueMask() != 255) {
            throw new IllegalArgumentException("blue mask in source should be 0x000000ff");
         } else if (dcm.getAlphaMask() != -16777216) {
            throw new IllegalArgumentException("alpha mask in source should be 0xff000000");
         }
      }
   }

   private boolean isCompatible(ColorModel colorModel, SampleModel sampleModel) {
      ColorSpace cs = colorModel.getColorSpace();
      if (cs != ColorSpace.getInstance(1000) && cs != ColorSpace.getInstance(1004)) {
         return false;
      } else if (!(colorModel instanceof DirectColorModel)) {
         return false;
      } else if (sampleModel.getDataType() != 3) {
         return false;
      } else {
         DirectColorModel dcm = (DirectColorModel)colorModel;
         if (dcm.getRedMask() != 16711680) {
            return false;
         } else if (dcm.getGreenMask() != 65280) {
            return false;
         } else if (dcm.getBlueMask() != 255) {
            return false;
         } else {
            return dcm.getAlphaMask() == -16777216;
         }
      }
   }

   private void checkCompatible(SampleModel model) {
      if (!(model instanceof SinglePixelPackedSampleModel)) {
         throw new IllegalArgumentException("MorphologyOp only works with Rasters using SinglePixelPackedSampleModels");
      } else {
         int nBands = model.getNumBands();
         if (nBands != 4) {
            throw new IllegalArgumentException("MorphologyOp only words with Rasters having 4 bands");
         } else if (model.getDataType() != 3) {
            throw new IllegalArgumentException("MorphologyOp only works with Rasters using DataBufferInt");
         } else {
            int[] bitOffsets = ((SinglePixelPackedSampleModel)model).getBitOffsets();

            for(int i = 0; i < bitOffsets.length; ++i) {
               if (bitOffsets[i] % 8 != 0) {
                  throw new IllegalArgumentException("MorphologyOp only works with Rasters using 8 bits per band : " + i + " : " + bitOffsets[i]);
               }
            }

         }
      }
   }

   public RenderingHints getRenderingHints() {
      return null;
   }

   public WritableRaster createCompatibleDestRaster(Raster src) {
      this.checkCompatible(src.getSampleModel());
      return src.createCompatibleWritableRaster();
   }

   public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
      BufferedImage dest = null;
      if (destCM == null) {
         destCM = src.getColorModel();
      }

      WritableRaster wr = destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight());
      this.checkCompatible(destCM, wr.getSampleModel());
      dest = new BufferedImage(destCM, wr, destCM.isAlphaPremultiplied(), (Hashtable)null);
      return dest;
   }

   static final boolean isBetter(int v1, int v2, boolean doDilation) {
      if (v1 > v2) {
         return doDilation;
      } else if (v1 < v2) {
         return !doDilation;
      } else {
         return true;
      }
   }

   private void specialProcessRow(Raster src, WritableRaster dest) {
      int w = src.getWidth();
      int h = src.getHeight();
      DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
      DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)src.getSampleModel();
      int srcOff = srcDB.getOffset() + sppsm.getOffset(src.getMinX() - src.getSampleModelTranslateX(), src.getMinY() - src.getSampleModelTranslateY());
      sppsm = (SinglePixelPackedSampleModel)dest.getSampleModel();
      int dstOff = dstDB.getOffset() + sppsm.getOffset(dest.getMinX() - dest.getSampleModelTranslateX(), dest.getMinY() - dest.getSampleModelTranslateY());
      int srcScanStride = ((SinglePixelPackedSampleModel)src.getSampleModel()).getScanlineStride();
      int dstScanStride = ((SinglePixelPackedSampleModel)dest.getSampleModel()).getScanlineStride();
      int[] srcPixels = srcDB.getBankData()[0];
      int[] destPixels = dstDB.getBankData()[0];
      int sp;
      int dp;
      int pel;
      int currentPixel;
      int a;
      int r;
      int g;
      int b;
      int a1;
      int r1;
      int g1;
      int b1;
      if (w <= this.radiusX) {
         for(int i = 0; i < h; ++i) {
            sp = srcOff + i * srcScanStride;
            dp = dstOff + i * dstScanStride;
            pel = srcPixels[sp++];
            a = pel >>> 24;
            r = pel & 16711680;
            g = pel & '\uff00';
            b = pel & 255;

            int k;
            for(k = 1; k < w; ++k) {
               currentPixel = srcPixels[sp++];
               a1 = currentPixel >>> 24;
               r1 = currentPixel & 16711680;
               g1 = currentPixel & '\uff00';
               b1 = currentPixel & 255;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
               }

               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
               }

               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
               }

               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
               }
            }

            for(k = 0; k < w; ++k) {
               destPixels[dp++] = a << 24 | r | g | b;
            }
         }
      } else {
         int[] bufferA = new int[w];
         int[] bufferR = new int[w];
         int[] bufferG = new int[w];
         int[] bufferB = new int[w];

         for(int i = 0; i < h; ++i) {
            sp = srcOff + i * srcScanStride;
            dp = dstOff + i * dstScanStride;
            int bufferHead = 0;
            int maxIndexA = 0;
            int maxIndexR = 0;
            int maxIndexG = 0;
            int maxIndexB = 0;
            pel = srcPixels[sp++];
            a = pel >>> 24;
            r = pel & 16711680;
            g = pel & '\uff00';
            b = pel & 255;
            bufferA[0] = a;
            bufferR[0] = r;
            bufferG[0] = g;
            bufferB[0] = b;

            int j;
            for(j = 1; j <= this.radiusX; ++j) {
               currentPixel = srcPixels[sp++];
               a1 = currentPixel >>> 24;
               r1 = currentPixel & 16711680;
               g1 = currentPixel & '\uff00';
               b1 = currentPixel & 255;
               bufferA[j] = a1;
               bufferR[j] = r1;
               bufferG[j] = g1;
               bufferB[j] = b1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = j;
               }

               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = j;
               }

               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = j;
               }

               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = j;
               }
            }

            destPixels[dp++] = a << 24 | r | g | b;

            for(j = 1; j <= w - this.radiusX - 1; ++j) {
               int lastPixel = srcPixels[sp++];
               a = bufferA[maxIndexA];
               a1 = lastPixel >>> 24;
               bufferA[j + this.radiusX] = a1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = j + this.radiusX;
               }

               r = bufferR[maxIndexR];
               r1 = lastPixel & 16711680;
               bufferR[j + this.radiusX] = r1;
               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = j + this.radiusX;
               }

               g = bufferG[maxIndexG];
               g1 = lastPixel & '\uff00';
               bufferG[j + this.radiusX] = g1;
               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = j + this.radiusX;
               }

               b = bufferB[maxIndexB];
               b1 = lastPixel & 255;
               bufferB[j + this.radiusX] = b1;
               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = j + this.radiusX;
               }

               destPixels[dp++] = a << 24 | r | g | b;
            }

            for(j = w - this.radiusX; j <= this.radiusX; ++j) {
               destPixels[dp] = destPixels[dp - 1];
               ++dp;
            }

            for(j = this.radiusX + 1; j < w; ++j) {
               int m;
               if (maxIndexA == bufferHead) {
                  a = bufferA[bufferHead + 1];
                  maxIndexA = bufferHead + 1;

                  for(m = bufferHead + 2; m < w; ++m) {
                     a1 = bufferA[m];
                     if (isBetter(a1, a, this.doDilation)) {
                        a = a1;
                        maxIndexA = m;
                     }
                  }
               } else {
                  a = bufferA[maxIndexA];
               }

               if (maxIndexR == bufferHead) {
                  r = bufferR[bufferHead + 1];
                  maxIndexR = bufferHead + 1;

                  for(m = bufferHead + 2; m < w; ++m) {
                     r1 = bufferR[m];
                     if (isBetter(r1, r, this.doDilation)) {
                        r = r1;
                        maxIndexR = m;
                     }
                  }
               } else {
                  r = bufferR[maxIndexR];
               }

               if (maxIndexG == bufferHead) {
                  g = bufferG[bufferHead + 1];
                  maxIndexG = bufferHead + 1;

                  for(m = bufferHead + 2; m < w; ++m) {
                     g1 = bufferG[m];
                     if (isBetter(g1, g, this.doDilation)) {
                        g = g1;
                        maxIndexG = m;
                     }
                  }
               } else {
                  g = bufferG[maxIndexG];
               }

               if (maxIndexB == bufferHead) {
                  b = bufferB[bufferHead + 1];
                  maxIndexB = bufferHead + 1;

                  for(m = bufferHead + 2; m < w; ++m) {
                     b1 = bufferB[m];
                     if (isBetter(b1, b, this.doDilation)) {
                        b = b1;
                        maxIndexB = m;
                     }
                  }
               } else {
                  b = bufferB[maxIndexB];
               }

               ++bufferHead;
               destPixels[dp++] = a << 24 | r | g | b;
            }
         }
      }

   }

   private void specialProcessColumn(Raster src, WritableRaster dest) {
      int w = src.getWidth();
      int h = src.getHeight();
      DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
      int dstOff = dstDB.getOffset();
      int dstScanStride = ((SinglePixelPackedSampleModel)dest.getSampleModel()).getScanlineStride();
      int[] destPixels = dstDB.getBankData()[0];
      int dp;
      int cp;
      int pel;
      int currentPixel;
      int a;
      int r;
      int g;
      int b;
      int a1;
      int r1;
      int g1;
      int b1;
      if (h <= this.radiusY) {
         for(int j = 0; j < w; ++j) {
            dp = dstOff + j;
            cp = dstOff + j;
            pel = destPixels[cp];
            cp += dstScanStride;
            a = pel >>> 24;
            r = pel & 16711680;
            g = pel & '\uff00';
            b = pel & 255;

            int k;
            for(k = 1; k < h; ++k) {
               currentPixel = destPixels[cp];
               cp += dstScanStride;
               a1 = currentPixel >>> 24;
               r1 = currentPixel & 16711680;
               g1 = currentPixel & '\uff00';
               b1 = currentPixel & 255;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
               }

               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
               }

               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
               }

               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
               }
            }

            for(k = 0; k < h; ++k) {
               destPixels[dp] = a << 24 | r | g | b;
               dp += dstScanStride;
            }
         }
      } else {
         int[] bufferA = new int[h];
         int[] bufferR = new int[h];
         int[] bufferG = new int[h];
         int[] bufferB = new int[h];

         for(int j = 0; j < w; ++j) {
            dp = dstOff + j;
            cp = dstOff + j;
            int bufferHead = 0;
            int maxIndexA = 0;
            int maxIndexR = 0;
            int maxIndexG = 0;
            int maxIndexB = 0;
            pel = destPixels[cp];
            cp += dstScanStride;
            a = pel >>> 24;
            r = pel & 16711680;
            g = pel & '\uff00';
            b = pel & 255;
            bufferA[0] = a;
            bufferR[0] = r;
            bufferG[0] = g;
            bufferB[0] = b;

            int i;
            for(i = 1; i <= this.radiusY; ++i) {
               currentPixel = destPixels[cp];
               cp += dstScanStride;
               a1 = currentPixel >>> 24;
               r1 = currentPixel & 16711680;
               g1 = currentPixel & '\uff00';
               b1 = currentPixel & 255;
               bufferA[i] = a1;
               bufferR[i] = r1;
               bufferG[i] = g1;
               bufferB[i] = b1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = i;
               }

               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = i;
               }

               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = i;
               }

               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = i;
               }
            }

            destPixels[dp] = a << 24 | r | g | b;
            dp += dstScanStride;

            for(i = 1; i <= h - this.radiusY - 1; ++i) {
               int lastPixel = destPixels[cp];
               cp += dstScanStride;
               a = bufferA[maxIndexA];
               a1 = lastPixel >>> 24;
               bufferA[i + this.radiusY] = a1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = i + this.radiusY;
               }

               r = bufferR[maxIndexR];
               r1 = lastPixel & 16711680;
               bufferR[i + this.radiusY] = r1;
               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = i + this.radiusY;
               }

               g = bufferG[maxIndexG];
               g1 = lastPixel & '\uff00';
               bufferG[i + this.radiusY] = g1;
               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = i + this.radiusY;
               }

               b = bufferB[maxIndexB];
               b1 = lastPixel & 255;
               bufferB[i + this.radiusY] = b1;
               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = i + this.radiusY;
               }

               destPixels[dp] = a << 24 | r | g | b;
               dp += dstScanStride;
            }

            for(i = h - this.radiusY; i <= this.radiusY; ++i) {
               destPixels[dp] = destPixels[dp - dstScanStride];
               dp += dstScanStride;
            }

            for(i = this.radiusY + 1; i < h; ++i) {
               int m;
               if (maxIndexA == bufferHead) {
                  a = bufferA[bufferHead + 1];
                  maxIndexA = bufferHead + 1;

                  for(m = bufferHead + 2; m < h; ++m) {
                     a1 = bufferA[m];
                     if (isBetter(a1, a, this.doDilation)) {
                        a = a1;
                        maxIndexA = m;
                     }
                  }
               } else {
                  a = bufferA[maxIndexA];
               }

               if (maxIndexR == bufferHead) {
                  r = bufferR[bufferHead + 1];
                  maxIndexR = bufferHead + 1;

                  for(m = bufferHead + 2; m < h; ++m) {
                     r1 = bufferR[m];
                     if (isBetter(r1, r, this.doDilation)) {
                        r = r1;
                        maxIndexR = m;
                     }
                  }
               } else {
                  r = bufferR[maxIndexR];
               }

               if (maxIndexG == bufferHead) {
                  g = bufferG[bufferHead + 1];
                  maxIndexG = bufferHead + 1;

                  for(m = bufferHead + 2; m < h; ++m) {
                     g1 = bufferG[m];
                     if (isBetter(g1, g, this.doDilation)) {
                        g = g1;
                        maxIndexG = m;
                     }
                  }
               } else {
                  g = bufferG[maxIndexG];
               }

               if (maxIndexB == bufferHead) {
                  b = bufferB[bufferHead + 1];
                  maxIndexB = bufferHead + 1;

                  for(m = bufferHead + 2; m < h; ++m) {
                     b1 = bufferB[m];
                     if (isBetter(b1, b, this.doDilation)) {
                        b = b1;
                        maxIndexB = m;
                     }
                  }
               } else {
                  b = bufferB[maxIndexB];
               }

               ++bufferHead;
               destPixels[dp] = a << 24 | r | g | b;
               dp += dstScanStride;
            }
         }
      }

   }

   public WritableRaster filter(Raster src, WritableRaster dest) {
      if (dest != null) {
         this.checkCompatible(dest.getSampleModel());
      } else {
         if (src == null) {
            throw new IllegalArgumentException("src should not be null when dest is null");
         }

         dest = this.createCompatibleDestRaster(src);
      }

      int w = src.getWidth();
      int h = src.getHeight();
      DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
      DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
      int srcOff = srcDB.getOffset();
      int dstOff = dstDB.getOffset();
      int srcScanStride = ((SinglePixelPackedSampleModel)src.getSampleModel()).getScanlineStride();
      int dstScanStride = ((SinglePixelPackedSampleModel)dest.getSampleModel()).getScanlineStride();
      int[] srcPixels = srcDB.getBankData()[0];
      int[] destPixels = dstDB.getBankData()[0];
      int dp;
      int bufferHead;
      int maxIndexA;
      int maxIndexR;
      int maxIndexG;
      int maxIndexB;
      int pel;
      int currentPixel;
      int lastPixel;
      int a;
      int r;
      int g;
      int b;
      int a1;
      int r1;
      int g1;
      int b1;
      int[] bufferA;
      int[] bufferR;
      int[] bufferG;
      int[] bufferB;
      int j;
      int head;
      int m;
      int count;
      int i;
      int hd;
      int m;
      if (w <= 2 * this.radiusX) {
         this.specialProcessRow(src, dest);
      } else {
         bufferA = new int[this.rangeX];
         bufferR = new int[this.rangeX];
         bufferG = new int[this.rangeX];
         bufferB = new int[this.rangeX];

         for(j = 0; j < h; ++j) {
            int sp = srcOff + j * srcScanStride;
            dp = dstOff + j * dstScanStride;
            bufferHead = 0;
            maxIndexA = 0;
            maxIndexR = 0;
            maxIndexG = 0;
            maxIndexB = 0;
            pel = srcPixels[sp++];
            a = pel >>> 24;
            r = pel & 16711680;
            g = pel & '\uff00';
            b = pel & 255;
            bufferA[0] = a;
            bufferR[0] = r;
            bufferG[0] = g;
            bufferB[0] = b;

            for(head = 1; head <= this.radiusX; ++head) {
               currentPixel = srcPixels[sp++];
               a1 = currentPixel >>> 24;
               r1 = currentPixel & 16711680;
               g1 = currentPixel & '\uff00';
               b1 = currentPixel & 255;
               bufferA[head] = a1;
               bufferR[head] = r1;
               bufferG[head] = g1;
               bufferB[head] = b1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = head;
               }

               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = head;
               }

               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = head;
               }

               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = head;
               }
            }

            destPixels[dp++] = a << 24 | r | g | b;

            for(head = 1; head <= this.radiusX; ++head) {
               lastPixel = srcPixels[sp++];
               a = bufferA[maxIndexA];
               a1 = lastPixel >>> 24;
               bufferA[head + this.radiusX] = a1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = head + this.radiusX;
               }

               r = bufferR[maxIndexR];
               r1 = lastPixel & 16711680;
               bufferR[head + this.radiusX] = r1;
               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = head + this.radiusX;
               }

               g = bufferG[maxIndexG];
               g1 = lastPixel & '\uff00';
               bufferG[head + this.radiusX] = g1;
               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = head + this.radiusX;
               }

               b = bufferB[maxIndexB];
               b1 = lastPixel & 255;
               bufferB[head + this.radiusX] = b1;
               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = head + this.radiusX;
               }

               destPixels[dp++] = a << 24 | r | g | b;
            }

            for(head = this.radiusX + 1; head <= w - 1 - this.radiusX; ++head) {
               lastPixel = srcPixels[sp++];
               a1 = lastPixel >>> 24;
               r1 = lastPixel & 16711680;
               g1 = lastPixel & '\uff00';
               b1 = lastPixel & 255;
               bufferA[bufferHead] = a1;
               bufferR[bufferHead] = r1;
               bufferG[bufferHead] = g1;
               bufferB[bufferHead] = b1;
               if (maxIndexA == bufferHead) {
                  a = bufferA[0];
                  maxIndexA = 0;

                  for(m = 1; m < this.rangeX; ++m) {
                     a1 = bufferA[m];
                     if (isBetter(a1, a, this.doDilation)) {
                        a = a1;
                        maxIndexA = m;
                     }
                  }
               } else {
                  a = bufferA[maxIndexA];
                  if (isBetter(a1, a, this.doDilation)) {
                     a = a1;
                     maxIndexA = bufferHead;
                  }
               }

               if (maxIndexR == bufferHead) {
                  r = bufferR[0];
                  maxIndexR = 0;

                  for(m = 1; m < this.rangeX; ++m) {
                     r1 = bufferR[m];
                     if (isBetter(r1, r, this.doDilation)) {
                        r = r1;
                        maxIndexR = m;
                     }
                  }
               } else {
                  r = bufferR[maxIndexR];
                  if (isBetter(r1, r, this.doDilation)) {
                     r = r1;
                     maxIndexR = bufferHead;
                  }
               }

               if (maxIndexG == bufferHead) {
                  g = bufferG[0];
                  maxIndexG = 0;

                  for(m = 1; m < this.rangeX; ++m) {
                     g1 = bufferG[m];
                     if (isBetter(g1, g, this.doDilation)) {
                        g = g1;
                        maxIndexG = m;
                     }
                  }
               } else {
                  g = bufferG[maxIndexG];
                  if (isBetter(g1, g, this.doDilation)) {
                     g = g1;
                     maxIndexG = bufferHead;
                  }
               }

               if (maxIndexB == bufferHead) {
                  b = bufferB[0];
                  maxIndexB = 0;

                  for(m = 1; m < this.rangeX; ++m) {
                     b1 = bufferB[m];
                     if (isBetter(b1, b, this.doDilation)) {
                        b = b1;
                        maxIndexB = m;
                     }
                  }
               } else {
                  b = bufferB[maxIndexB];
                  if (isBetter(b1, b, this.doDilation)) {
                     b = b1;
                     maxIndexB = bufferHead;
                  }
               }

               destPixels[dp++] = a << 24 | r | g | b;
               bufferHead = (bufferHead + 1) % this.rangeX;
            }

            m = bufferHead == 0 ? this.rangeX - 1 : bufferHead - 1;
            count = this.rangeX - 1;

            for(i = w - this.radiusX; i < w; ++i) {
               head = (bufferHead + 1) % this.rangeX;
               if (maxIndexA == bufferHead) {
                  a = bufferA[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     a1 = bufferA[hd];
                     if (isBetter(a1, a, this.doDilation)) {
                        a = a1;
                        maxIndexA = hd;
                     }

                     hd = (hd + 1) % this.rangeX;
                  }
               }

               if (maxIndexR == bufferHead) {
                  r = bufferR[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     r1 = bufferR[hd];
                     if (isBetter(r1, r, this.doDilation)) {
                        r = r1;
                        maxIndexR = hd;
                     }

                     hd = (hd + 1) % this.rangeX;
                  }
               }

               if (maxIndexG == bufferHead) {
                  g = bufferG[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     g1 = bufferG[hd];
                     if (isBetter(g1, g, this.doDilation)) {
                        g = g1;
                        maxIndexG = hd;
                     }

                     hd = (hd + 1) % this.rangeX;
                  }
               }

               if (maxIndexB == bufferHead) {
                  b = bufferB[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     b1 = bufferB[hd];
                     if (isBetter(b1, b, this.doDilation)) {
                        b = b1;
                        maxIndexB = hd;
                     }

                     hd = (hd + 1) % this.rangeX;
                  }
               }

               destPixels[dp++] = a << 24 | r | g | b;
               bufferHead = (bufferHead + 1) % this.rangeX;
               --count;
            }
         }
      }

      if (h <= 2 * this.radiusY) {
         this.specialProcessColumn(src, dest);
      } else {
         bufferA = new int[this.rangeY];
         bufferR = new int[this.rangeY];
         bufferG = new int[this.rangeY];
         bufferB = new int[this.rangeY];

         for(j = 0; j < w; ++j) {
            dp = dstOff + j;
            int cp = dstOff + j;
            bufferHead = 0;
            maxIndexA = 0;
            maxIndexR = 0;
            maxIndexG = 0;
            maxIndexB = 0;
            pel = destPixels[cp];
            cp += dstScanStride;
            a = pel >>> 24;
            r = pel & 16711680;
            g = pel & '\uff00';
            b = pel & 255;
            bufferA[0] = a;
            bufferR[0] = r;
            bufferG[0] = g;
            bufferB[0] = b;

            for(head = 1; head <= this.radiusY; ++head) {
               currentPixel = destPixels[cp];
               cp += dstScanStride;
               a1 = currentPixel >>> 24;
               r1 = currentPixel & 16711680;
               g1 = currentPixel & '\uff00';
               b1 = currentPixel & 255;
               bufferA[head] = a1;
               bufferR[head] = r1;
               bufferG[head] = g1;
               bufferB[head] = b1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = head;
               }

               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = head;
               }

               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = head;
               }

               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = head;
               }
            }

            destPixels[dp] = a << 24 | r | g | b;
            dp += dstScanStride;

            for(head = 1; head <= this.radiusY; ++head) {
               m = head + this.radiusY;
               lastPixel = destPixels[cp];
               cp += dstScanStride;
               a = bufferA[maxIndexA];
               a1 = lastPixel >>> 24;
               bufferA[m] = a1;
               if (isBetter(a1, a, this.doDilation)) {
                  a = a1;
                  maxIndexA = m;
               }

               r = bufferR[maxIndexR];
               r1 = lastPixel & 16711680;
               bufferR[m] = r1;
               if (isBetter(r1, r, this.doDilation)) {
                  r = r1;
                  maxIndexR = m;
               }

               g = bufferG[maxIndexG];
               g1 = lastPixel & '\uff00';
               bufferG[m] = g1;
               if (isBetter(g1, g, this.doDilation)) {
                  g = g1;
                  maxIndexG = m;
               }

               b = bufferB[maxIndexB];
               b1 = lastPixel & 255;
               bufferB[m] = b1;
               if (isBetter(b1, b, this.doDilation)) {
                  b = b1;
                  maxIndexB = m;
               }

               destPixels[dp] = a << 24 | r | g | b;
               dp += dstScanStride;
            }

            for(head = this.radiusY + 1; head <= h - 1 - this.radiusY; ++head) {
               lastPixel = destPixels[cp];
               cp += dstScanStride;
               a1 = lastPixel >>> 24;
               r1 = lastPixel & 16711680;
               g1 = lastPixel & '\uff00';
               b1 = lastPixel & 255;
               bufferA[bufferHead] = a1;
               bufferR[bufferHead] = r1;
               bufferG[bufferHead] = g1;
               bufferB[bufferHead] = b1;
               if (maxIndexA == bufferHead) {
                  a = bufferA[0];
                  maxIndexA = 0;

                  for(m = 1; m <= 2 * this.radiusY; ++m) {
                     a1 = bufferA[m];
                     if (isBetter(a1, a, this.doDilation)) {
                        a = a1;
                        maxIndexA = m;
                     }
                  }
               } else {
                  a = bufferA[maxIndexA];
                  if (isBetter(a1, a, this.doDilation)) {
                     a = a1;
                     maxIndexA = bufferHead;
                  }
               }

               if (maxIndexR == bufferHead) {
                  r = bufferR[0];
                  maxIndexR = 0;

                  for(m = 1; m <= 2 * this.radiusY; ++m) {
                     r1 = bufferR[m];
                     if (isBetter(r1, r, this.doDilation)) {
                        r = r1;
                        maxIndexR = m;
                     }
                  }
               } else {
                  r = bufferR[maxIndexR];
                  if (isBetter(r1, r, this.doDilation)) {
                     r = r1;
                     maxIndexR = bufferHead;
                  }
               }

               if (maxIndexG == bufferHead) {
                  g = bufferG[0];
                  maxIndexG = 0;

                  for(m = 1; m <= 2 * this.radiusY; ++m) {
                     g1 = bufferG[m];
                     if (isBetter(g1, g, this.doDilation)) {
                        g = g1;
                        maxIndexG = m;
                     }
                  }
               } else {
                  g = bufferG[maxIndexG];
                  if (isBetter(g1, g, this.doDilation)) {
                     g = g1;
                     maxIndexG = bufferHead;
                  }
               }

               if (maxIndexB == bufferHead) {
                  b = bufferB[0];
                  maxIndexB = 0;

                  for(m = 1; m <= 2 * this.radiusY; ++m) {
                     b1 = bufferB[m];
                     if (isBetter(b1, b, this.doDilation)) {
                        b = b1;
                        maxIndexB = m;
                     }
                  }
               } else {
                  b = bufferB[maxIndexB];
                  if (isBetter(b1, b, this.doDilation)) {
                     b = b1;
                     maxIndexB = bufferHead;
                  }
               }

               destPixels[dp] = a << 24 | r | g | b;
               dp += dstScanStride;
               bufferHead = (bufferHead + 1) % this.rangeY;
            }

            m = bufferHead == 0 ? 2 * this.radiusY : bufferHead - 1;
            count = this.rangeY - 1;

            for(i = h - this.radiusY; i < h - 1; ++i) {
               head = (bufferHead + 1) % this.rangeY;
               if (maxIndexA == bufferHead) {
                  a = bufferA[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     a1 = bufferA[hd];
                     if (isBetter(a1, a, this.doDilation)) {
                        a = a1;
                        maxIndexA = hd;
                     }

                     hd = (hd + 1) % this.rangeY;
                  }
               }

               if (maxIndexR == bufferHead) {
                  r = bufferR[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     r1 = bufferR[hd];
                     if (isBetter(r1, r, this.doDilation)) {
                        r = r1;
                        maxIndexR = hd;
                     }

                     hd = (hd + 1) % this.rangeY;
                  }
               }

               if (maxIndexG == bufferHead) {
                  g = bufferG[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     g1 = bufferG[hd];
                     if (isBetter(g1, g, this.doDilation)) {
                        g = g1;
                        maxIndexG = hd;
                     }

                     hd = (hd + 1) % this.rangeY;
                  }
               }

               if (maxIndexB == bufferHead) {
                  b = bufferB[m];
                  hd = head;

                  for(m = 1; m < count; ++m) {
                     b1 = bufferB[hd];
                     if (isBetter(b1, b, this.doDilation)) {
                        b = b1;
                        maxIndexB = hd;
                     }

                     hd = (hd + 1) % this.rangeY;
                  }
               }

               destPixels[dp] = a << 24 | r | g | b;
               dp += dstScanStride;
               bufferHead = (bufferHead + 1) % this.rangeY;
               --count;
            }
         }
      }

      return dest;
   }

   public BufferedImage filter(BufferedImage src, BufferedImage dest) {
      if (src == null) {
         throw new NullPointerException("Source image should not be null");
      } else {
         BufferedImage origSrc = src;
         BufferedImage finalDest = dest;
         ColorModel dstCM;
         ColorModel dstCMPre;
         if (!this.isCompatible(src.getColorModel(), src.getSampleModel())) {
            src = new BufferedImage(src.getWidth(), src.getHeight(), 3);
            GraphicsUtil.copyData(origSrc, src);
         } else if (!src.isAlphaPremultiplied()) {
            dstCM = src.getColorModel();
            dstCMPre = GraphicsUtil.coerceColorModel(dstCM, true);
            src = new BufferedImage(dstCMPre, src.getRaster(), true, (Hashtable)null);
            GraphicsUtil.copyData(origSrc, src);
         }

         if (dest == null) {
            dest = this.createCompatibleDestImage(src, (ColorModel)null);
            finalDest = dest;
         } else if (!this.isCompatible(dest.getColorModel(), dest.getSampleModel())) {
            dest = this.createCompatibleDestImage(src, (ColorModel)null);
         } else if (!dest.isAlphaPremultiplied()) {
            dstCM = dest.getColorModel();
            dstCMPre = GraphicsUtil.coerceColorModel(dstCM, true);
            dest = new BufferedImage(dstCMPre, dest.getRaster(), true, (Hashtable)null);
         }

         this.filter((Raster)src.getRaster(), (WritableRaster)dest.getRaster());
         if (src.getRaster() == origSrc.getRaster() && src.isAlphaPremultiplied() != origSrc.isAlphaPremultiplied()) {
            GraphicsUtil.copyData(src, origSrc);
         }

         if (dest.getRaster() != finalDest.getRaster() || dest.isAlphaPremultiplied() != finalDest.isAlphaPremultiplied()) {
            GraphicsUtil.copyData(dest, finalDest);
         }

         return finalDest;
      }
   }
}
