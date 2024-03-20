package org.apache.batik.ext.awt;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.lang.ref.WeakReference;
import org.apache.batik.ext.awt.image.GraphicsUtil;

abstract class MultipleGradientPaintContext implements PaintContext {
   protected static final boolean DEBUG = false;
   protected ColorModel dataModel;
   protected ColorModel model;
   private static ColorModel lrgbmodel_NA = new DirectColorModel(ColorSpace.getInstance(1004), 24, 16711680, 65280, 255, 0, false, 3);
   private static ColorModel srgbmodel_NA = new DirectColorModel(ColorSpace.getInstance(1000), 24, 16711680, 65280, 255, 0, false, 3);
   private static ColorModel lrgbmodel_A = new DirectColorModel(ColorSpace.getInstance(1004), 32, 16711680, 65280, 255, -16777216, false, 3);
   private static ColorModel srgbmodel_A = new DirectColorModel(ColorSpace.getInstance(1000), 32, 16711680, 65280, 255, -16777216, false, 3);
   protected static ColorModel cachedModel;
   protected static WeakReference cached;
   protected WritableRaster saved;
   protected MultipleGradientPaint.CycleMethodEnum cycleMethod;
   protected MultipleGradientPaint.ColorSpaceEnum colorSpace;
   protected float a00;
   protected float a01;
   protected float a10;
   protected float a11;
   protected float a02;
   protected float a12;
   protected boolean isSimpleLookup = true;
   protected boolean hasDiscontinuity = false;
   protected int fastGradientArraySize;
   protected int[] gradient;
   protected int[][] gradients;
   protected int gradientAverage;
   protected int gradientUnderflow;
   protected int gradientOverflow;
   protected int gradientsLength;
   protected float[] normalizedIntervals;
   protected float[] fractions;
   private int transparencyTest;
   private static final int[] SRGBtoLinearRGB = new int[256];
   private static final int[] LinearRGBtoSRGB = new int[256];
   protected static final int GRADIENT_SIZE = 256;
   protected static final int GRADIENT_SIZE_INDEX = 255;
   private static final int MAX_GRADIENT_ARRAY_SIZE = 5000;

   protected MultipleGradientPaintContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform t, RenderingHints hints, float[] fractions, Color[] colors, MultipleGradientPaint.CycleMethodEnum cycleMethod, MultipleGradientPaint.ColorSpaceEnum colorSpace) throws NoninvertibleTransformException {
      boolean fixFirst = false;
      boolean fixLast = false;
      int len = fractions.length;
      if (fractions[0] != 0.0F) {
         fixFirst = true;
         ++len;
      }

      if (fractions[fractions.length - 1] != 1.0F) {
         fixLast = true;
         ++len;
      }

      for(int i = 0; i < fractions.length - 1; ++i) {
         if (fractions[i] == fractions[i + 1]) {
            --len;
         }
      }

      this.fractions = new float[len];
      Color[] loColors = new Color[len - 1];
      Color[] hiColors = new Color[len - 1];
      this.normalizedIntervals = new float[len - 1];
      this.gradientUnderflow = colors[0].getRGB();
      this.gradientOverflow = colors[colors.length - 1].getRGB();
      int idx = 0;
      if (fixFirst) {
         this.fractions[0] = 0.0F;
         loColors[0] = colors[0];
         hiColors[0] = colors[0];
         this.normalizedIntervals[0] = fractions[0];
         ++idx;
      }

      for(int i = 0; i < fractions.length - 1; ++i) {
         if (fractions[i] == fractions[i + 1]) {
            if (!colors[i].equals(colors[i + 1])) {
               this.hasDiscontinuity = true;
            }
         } else {
            this.fractions[idx] = fractions[i];
            loColors[idx] = colors[i];
            hiColors[idx] = colors[i + 1];
            this.normalizedIntervals[idx] = fractions[i + 1] - fractions[i];
            ++idx;
         }
      }

      this.fractions[idx] = fractions[fractions.length - 1];
      if (fixLast) {
         loColors[idx] = hiColors[idx] = colors[colors.length - 1];
         this.normalizedIntervals[idx] = 1.0F - fractions[fractions.length - 1];
         ++idx;
         this.fractions[idx] = 1.0F;
      }

      AffineTransform tInv = t.createInverse();
      double[] m = new double[6];
      tInv.getMatrix(m);
      this.a00 = (float)m[0];
      this.a10 = (float)m[1];
      this.a01 = (float)m[2];
      this.a11 = (float)m[3];
      this.a02 = (float)m[4];
      this.a12 = (float)m[5];
      this.cycleMethod = cycleMethod;
      this.colorSpace = colorSpace;
      if (cm.getColorSpace() == lrgbmodel_A.getColorSpace()) {
         this.dataModel = lrgbmodel_A;
      } else {
         if (cm.getColorSpace() != srgbmodel_A.getColorSpace()) {
            throw new IllegalArgumentException("Unsupported ColorSpace for interpolation");
         }

         this.dataModel = srgbmodel_A;
      }

      this.calculateGradientFractions(loColors, hiColors);
      this.model = GraphicsUtil.coerceColorModel(this.dataModel, cm.isAlphaPremultiplied());
   }

   protected final void calculateGradientFractions(Color[] loColors, Color[] hiColors) {
      if (this.colorSpace == LinearGradientPaint.LINEAR_RGB) {
         int[] workTbl = SRGBtoLinearRGB;

         for(int i = 0; i < loColors.length; ++i) {
            loColors[i] = interpolateColor(workTbl, loColors[i]);
            hiColors[i] = interpolateColor(workTbl, hiColors[i]);
         }
      }

      this.transparencyTest = -16777216;
      if (this.cycleMethod == MultipleGradientPaint.NO_CYCLE) {
         this.transparencyTest &= this.gradientUnderflow;
         this.transparencyTest &= this.gradientOverflow;
      }

      this.gradients = new int[this.fractions.length - 1][];
      this.gradientsLength = this.gradients.length;
      int n = this.normalizedIntervals.length;
      float Imin = 1.0F;
      float[] workTbl = this.normalizedIntervals;

      int estimatedSize;
      for(estimatedSize = 0; estimatedSize < n; ++estimatedSize) {
         Imin = Imin > workTbl[estimatedSize] ? workTbl[estimatedSize] : Imin;
      }

      estimatedSize = 0;
      if (Imin == 0.0F) {
         estimatedSize = Integer.MAX_VALUE;
         this.hasDiscontinuity = true;
      } else {
         float[] var7 = workTbl;
         int var8 = workTbl.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            float aWorkTbl = var7[var9];
            estimatedSize = (int)((float)estimatedSize + aWorkTbl / Imin * 256.0F);
         }
      }

      if (estimatedSize > 5000) {
         this.calculateMultipleArrayGradient(loColors, hiColors);
         if (this.cycleMethod == MultipleGradientPaint.REPEAT && this.gradients[0][0] != this.gradients[this.gradients.length - 1][255]) {
            this.hasDiscontinuity = true;
         }
      } else {
         this.calculateSingleArrayGradient(loColors, hiColors, Imin);
         if (this.cycleMethod == MultipleGradientPaint.REPEAT && this.gradient[0] != this.gradient[this.fastGradientArraySize]) {
            this.hasDiscontinuity = true;
         }
      }

      if (this.transparencyTest >>> 24 == 255) {
         if (this.dataModel.getColorSpace() == lrgbmodel_NA.getColorSpace()) {
            this.dataModel = lrgbmodel_NA;
         } else if (this.dataModel.getColorSpace() == srgbmodel_NA.getColorSpace()) {
            this.dataModel = srgbmodel_NA;
         }

         this.model = this.dataModel;
      }

   }

   private static Color interpolateColor(int[] workTbl, Color inColor) {
      int oldColor = inColor.getRGB();
      int newColorValue = (workTbl[oldColor >> 24 & 255] & 255) << 24 | (workTbl[oldColor >> 16 & 255] & 255) << 16 | (workTbl[oldColor >> 8 & 255] & 255) << 8 | workTbl[oldColor & 255] & 255;
      return new Color(newColorValue, true);
   }

   private void calculateSingleArrayGradient(Color[] loColors, Color[] hiColors, float Imin) {
      this.isSimpleLookup = true;
      int gradientsTot = 1;
      int aveA = 32768;
      int aveR = 32768;
      int aveG = 32768;
      int aveB = 32768;

      int curOffset;
      int i;
      int rgb1;
      int rgb2;
      for(curOffset = 0; curOffset < this.gradients.length; ++curOffset) {
         i = (int)(this.normalizedIntervals[curOffset] / Imin * 255.0F);
         gradientsTot += i;
         this.gradients[curOffset] = new int[i];
         rgb1 = loColors[curOffset].getRGB();
         rgb2 = hiColors[curOffset].getRGB();
         this.interpolate(rgb1, rgb2, this.gradients[curOffset]);
         int argb = this.gradients[curOffset][128];
         float norm = this.normalizedIntervals[curOffset];
         aveA += (int)((float)(argb >> 8 & 16711680) * norm);
         aveR += (int)((float)(argb & 16711680) * norm);
         aveG += (int)((float)(argb << 8 & 16711680) * norm);
         aveB += (int)((float)(argb << 16 & 16711680) * norm);
         this.transparencyTest &= rgb1 & rgb2;
      }

      this.gradientAverage = (aveA & 16711680) << 8 | aveR & 16711680 | (aveG & 16711680) >> 8 | (aveB & 16711680) >> 16;
      this.gradient = new int[gradientsTot];
      curOffset = 0;
      int[][] var15 = this.gradients;
      rgb1 = var15.length;

      for(rgb2 = 0; rgb2 < rgb1; ++rgb2) {
         int[] gradient1 = var15[rgb2];
         System.arraycopy(gradient1, 0, this.gradient, curOffset, gradient1.length);
         curOffset += gradient1.length;
      }

      this.gradient[this.gradient.length - 1] = hiColors[hiColors.length - 1].getRGB();
      if (this.colorSpace == LinearGradientPaint.LINEAR_RGB) {
         if (this.dataModel.getColorSpace() == ColorSpace.getInstance(1000)) {
            for(i = 0; i < this.gradient.length; ++i) {
               this.gradient[i] = convertEntireColorLinearRGBtoSRGB(this.gradient[i]);
            }

            this.gradientAverage = convertEntireColorLinearRGBtoSRGB(this.gradientAverage);
         }
      } else if (this.dataModel.getColorSpace() == ColorSpace.getInstance(1004)) {
         for(i = 0; i < this.gradient.length; ++i) {
            this.gradient[i] = convertEntireColorSRGBtoLinearRGB(this.gradient[i]);
         }

         this.gradientAverage = convertEntireColorSRGBtoLinearRGB(this.gradientAverage);
      }

      this.fastGradientArraySize = this.gradient.length - 1;
   }

   private void calculateMultipleArrayGradient(Color[] loColors, Color[] hiColors) {
      this.isSimpleLookup = false;
      int aveA = 32768;
      int aveR = 32768;
      int aveG = 32768;
      int aveB = 32768;

      int j;
      int i;
      for(j = 0; j < this.gradients.length; ++j) {
         if (this.normalizedIntervals[j] != 0.0F) {
            this.gradients[j] = new int[256];
            int rgb1 = loColors[j].getRGB();
            int rgb2 = hiColors[j].getRGB();
            this.interpolate(rgb1, rgb2, this.gradients[j]);
            i = this.gradients[j][128];
            float norm = this.normalizedIntervals[j];
            aveA += (int)((float)(i >> 8 & 16711680) * norm);
            aveR += (int)((float)(i & 16711680) * norm);
            aveG += (int)((float)(i << 8 & 16711680) * norm);
            aveB += (int)((float)(i << 16 & 16711680) * norm);
            this.transparencyTest &= rgb1;
            this.transparencyTest &= rgb2;
         }
      }

      this.gradientAverage = (aveA & 16711680) << 8 | aveR & 16711680 | (aveG & 16711680) >> 8 | (aveB & 16711680) >> 16;
      if (this.colorSpace == LinearGradientPaint.LINEAR_RGB) {
         if (this.dataModel.getColorSpace() == ColorSpace.getInstance(1000)) {
            for(j = 0; j < this.gradients.length; ++j) {
               for(i = 0; i < this.gradients[j].length; ++i) {
                  this.gradients[j][i] = convertEntireColorLinearRGBtoSRGB(this.gradients[j][i]);
               }
            }

            this.gradientAverage = convertEntireColorLinearRGBtoSRGB(this.gradientAverage);
         }
      } else if (this.dataModel.getColorSpace() == ColorSpace.getInstance(1004)) {
         for(j = 0; j < this.gradients.length; ++j) {
            for(i = 0; i < this.gradients[j].length; ++i) {
               this.gradients[j][i] = convertEntireColorSRGBtoLinearRGB(this.gradients[j][i]);
            }
         }

         this.gradientAverage = convertEntireColorSRGBtoLinearRGB(this.gradientAverage);
      }

   }

   private void interpolate(int rgb1, int rgb2, int[] output) {
      int nSteps = output.length;
      float stepSize = 1.0F / (float)nSteps;
      int a1 = rgb1 >> 24 & 255;
      int r1 = rgb1 >> 16 & 255;
      int g1 = rgb1 >> 8 & 255;
      int b1 = rgb1 & 255;
      int da = (rgb2 >> 24 & 255) - a1;
      int dr = (rgb2 >> 16 & 255) - r1;
      int dg = (rgb2 >> 8 & 255) - g1;
      int db = (rgb2 & 255) - b1;
      float tempA = 2.0F * (float)da * stepSize;
      float tempR = 2.0F * (float)dr * stepSize;
      float tempG = 2.0F * (float)dg * stepSize;
      float tempB = 2.0F * (float)db * stepSize;
      output[0] = rgb1;
      --nSteps;
      output[nSteps] = rgb2;

      for(int i = 1; i < nSteps; ++i) {
         float fI = (float)i;
         output[i] = (a1 + ((int)(fI * tempA) + 1 >> 1) & 255) << 24 | (r1 + ((int)(fI * tempR) + 1 >> 1) & 255) << 16 | (g1 + ((int)(fI * tempG) + 1 >> 1) & 255) << 8 | b1 + ((int)(fI * tempB) + 1 >> 1) & 255;
      }

   }

   private static int convertEntireColorLinearRGBtoSRGB(int rgb) {
      int a1 = rgb >> 24 & 255;
      int r1 = rgb >> 16 & 255;
      int g1 = rgb >> 8 & 255;
      int b1 = rgb & 255;
      int[] workTbl = LinearRGBtoSRGB;
      r1 = workTbl[r1];
      g1 = workTbl[g1];
      b1 = workTbl[b1];
      return a1 << 24 | r1 << 16 | g1 << 8 | b1;
   }

   private static int convertEntireColorSRGBtoLinearRGB(int rgb) {
      int a1 = rgb >> 24 & 255;
      int r1 = rgb >> 16 & 255;
      int g1 = rgb >> 8 & 255;
      int b1 = rgb & 255;
      int[] workTbl = SRGBtoLinearRGB;
      r1 = workTbl[r1];
      g1 = workTbl[g1];
      b1 = workTbl[b1];
      return a1 << 24 | r1 << 16 | g1 << 8 | b1;
   }

   protected final int indexIntoGradientsArrays(float position) {
      int w;
      int c2;
      if (this.cycleMethod == MultipleGradientPaint.NO_CYCLE) {
         if (position >= 1.0F) {
            return this.gradientOverflow;
         }

         if (position <= 0.0F) {
            return this.gradientUnderflow;
         }
      } else {
         if (this.cycleMethod == MultipleGradientPaint.REPEAT) {
            position -= (float)((int)position);
            if (position < 0.0F) {
               ++position;
            }

            w = 0;
            int c1 = 0;
            c2 = 0;
            int i;
            if (this.isSimpleLookup) {
               position *= (float)this.gradient.length;
               i = (int)position;
               if (i + 1 < this.gradient.length) {
                  return this.gradient[i];
               }

               w = (int)((position - (float)i) * 65536.0F);
               c1 = this.gradient[i];
               c2 = this.gradient[0];
            } else {
               for(i = 0; i < this.gradientsLength; ++i) {
                  if (position < this.fractions[i + 1]) {
                     float delta = position - this.fractions[i];
                     delta = delta / this.normalizedIntervals[i] * 256.0F;
                     int index = (int)delta;
                     if (index + 1 < this.gradients[i].length || i + 1 < this.gradientsLength) {
                        return this.gradients[i][index];
                     }

                     w = (int)((delta - (float)index) * 65536.0F);
                     c1 = this.gradients[i][index];
                     c2 = this.gradients[0][0];
                     break;
                  }
               }
            }

            return ((c1 >> 8 & 16711680) + ((c2 >>> 24) - (c1 >>> 24)) * w & 16711680) << 8 | (c1 & 16711680) + ((c2 >> 16 & 255) - (c1 >> 16 & 255)) * w & 16711680 | ((c1 << 8 & 16711680) + ((c2 >> 8 & 255) - (c1 >> 8 & 255)) * w & 16711680) >> 8 | ((c1 << 16 & 16711680) + ((c2 & 255) - (c1 & 255)) * w & 16711680) >> 16;
         }

         if (position < 0.0F) {
            position = -position;
         }

         w = (int)position;
         position -= (float)w;
         if ((w & 1) == 1) {
            position = 1.0F - position;
         }
      }

      if (this.isSimpleLookup) {
         return this.gradient[(int)(position * (float)this.fastGradientArraySize)];
      } else {
         for(w = 0; w < this.gradientsLength; ++w) {
            if (position < this.fractions[w + 1]) {
               float delta = position - this.fractions[w];
               c2 = (int)(delta / this.normalizedIntervals[w] * 255.0F);
               return this.gradients[w][c2];
            }
         }

         return this.gradientOverflow;
      }
   }

   protected final int indexGradientAntiAlias(float position, float sz) {
      float weight;
      float p2;
      int norm;
      int pA;
      if (this.cycleMethod == MultipleGradientPaint.NO_CYCLE) {
         float p1 = position - sz / 2.0F;
         weight = position + sz / 2.0F;
         if (p1 >= 1.0F) {
            return this.gradientOverflow;
         } else if (weight <= 0.0F) {
            return this.gradientUnderflow;
         } else {
            p2 = 0.0F;
            float bottom_weight = 0.0F;
            int interior;
            float frac;
            if (weight >= 1.0F) {
               p2 = (weight - 1.0F) / sz;
               if (p1 <= 0.0F) {
                  bottom_weight = -p1 / sz;
                  frac = 1.0F;
                  interior = this.gradientAverage;
               } else {
                  frac = 1.0F - p1;
                  interior = this.getAntiAlias(p1, true, 1.0F, false, 1.0F - p1, 1.0F);
               }
            } else {
               if (!(p1 <= 0.0F)) {
                  return this.getAntiAlias(p1, true, weight, false, sz, 1.0F);
               }

               bottom_weight = -p1 / sz;
               frac = weight;
               interior = this.getAntiAlias(0.0F, true, weight, false, weight, 1.0F);
            }

            norm = (int)(65536.0F * frac / sz);
            pA = (interior >>> 20 & 4080) * norm >> 16;
            int pR = (interior >> 12 & 4080) * norm >> 16;
            int pG = (interior >> 4 & 4080) * norm >> 16;
            int pB = (interior << 4 & 4080) * norm >> 16;
            int tPix;
            if (bottom_weight != 0.0F) {
               tPix = this.gradientUnderflow;
               norm = (int)(65536.0F * bottom_weight);
               pA += (tPix >>> 20 & 4080) * norm >> 16;
               pR += (tPix >> 12 & 4080) * norm >> 16;
               pG += (tPix >> 4 & 4080) * norm >> 16;
               pB += (tPix << 4 & 4080) * norm >> 16;
            }

            if (p2 != 0.0F) {
               tPix = this.gradientOverflow;
               norm = (int)(65536.0F * p2);
               pA += (tPix >>> 20 & 4080) * norm >> 16;
               pR += (tPix >> 12 & 4080) * norm >> 16;
               pG += (tPix >> 4 & 4080) * norm >> 16;
               pB += (tPix << 4 & 4080) * norm >> 16;
            }

            return (pA & 4080) << 20 | (pR & 4080) << 12 | (pG & 4080) << 4 | (pB & 4080) >> 4;
         }
      } else {
         int intSz = (int)sz;
         weight = 1.0F;
         if (intSz != 0) {
            sz -= (float)intSz;
            weight = sz / ((float)intSz + sz);
            if ((double)weight < 0.1) {
               return this.gradientAverage;
            }
         }

         if ((double)sz > 0.99) {
            return this.gradientAverage;
         } else {
            float p1 = position - sz / 2.0F;
            p2 = position + sz / 2.0F;
            boolean p1_up = true;
            boolean p2_up = false;
            if (this.cycleMethod == MultipleGradientPaint.REPEAT) {
               p1 -= (float)((int)p1);
               p2 -= (float)((int)p2);
               if (p1 < 0.0F) {
                  ++p1;
               }

               if (p2 < 0.0F) {
                  ++p2;
               }
            } else {
               if (p2 < 0.0F) {
                  p1 = -p1;
                  p1_up = !p1_up;
                  p2 = -p2;
                  p2_up = !p2_up;
               } else if (p1 < 0.0F) {
                  p1 = -p1;
                  p1_up = !p1_up;
               }

               norm = (int)p1;
               p1 -= (float)norm;
               pA = (int)p2;
               p2 -= (float)pA;
               if ((norm & 1) == 1) {
                  p1 = 1.0F - p1;
                  p1_up = !p1_up;
               }

               if ((pA & 1) == 1) {
                  p2 = 1.0F - p2;
                  p2_up = !p2_up;
               }

               if (p1 > p2 && !p1_up && p2_up) {
                  float t = p1;
                  p1 = p2;
                  p2 = t;
                  p1_up = true;
                  p2_up = false;
               }
            }

            return this.getAntiAlias(p1, p1_up, p2, p2_up, sz, weight);
         }
      }
   }

   private final int getAntiAlias(float p1, boolean p1_up, float p2, boolean p2_up, float sz, float weight) {
      int ach = 0;
      int rch = 0;
      int gch = 0;
      int bch = 0;
      int aveW;
      int idx2;
      int i1;
      int i2;
      int aveB;
      int iw;
      if (this.isSimpleLookup) {
         p1 *= (float)this.fastGradientArraySize;
         p2 *= (float)this.fastGradientArraySize;
         aveW = (int)p1;
         idx2 = (int)p2;
         if (p1_up && !p2_up && aveW <= idx2) {
            if (aveW == idx2) {
               return this.gradient[aveW];
            }

            for(i1 = aveW + 1; i1 < idx2; ++i1) {
               i2 = this.gradient[i1];
               ach += i2 >>> 20 & 4080;
               rch += i2 >>> 12 & 4080;
               gch += i2 >>> 4 & 4080;
               bch += i2 << 4 & 4080;
            }
         } else {
            if (p1_up) {
               aveB = aveW + 1;
               iw = this.fastGradientArraySize;
            } else {
               aveB = 0;
               iw = aveW;
            }

            for(i1 = aveB; i1 < iw; ++i1) {
               i2 = this.gradient[i1];
               ach += i2 >>> 20 & 4080;
               rch += i2 >>> 12 & 4080;
               gch += i2 >>> 4 & 4080;
               bch += i2 << 4 & 4080;
            }

            if (p2_up) {
               aveB = idx2 + 1;
               iw = this.fastGradientArraySize;
            } else {
               aveB = 0;
               iw = idx2;
            }

            for(i1 = aveB; i1 < iw; ++i1) {
               i2 = this.gradient[i1];
               ach += i2 >>> 20 & 4080;
               rch += i2 >>> 12 & 4080;
               gch += i2 >>> 4 & 4080;
               bch += i2 << 4 & 4080;
            }
         }

         iw = (int)(65536.0F / (sz * (float)this.fastGradientArraySize));
         ach = ach * iw >> 16;
         rch = rch * iw >> 16;
         gch = gch * iw >> 16;
         bch = bch * iw >> 16;
         if (p1_up) {
            aveB = (int)((1.0F - (p1 - (float)aveW)) * (float)iw);
         } else {
            aveB = (int)((p1 - (float)aveW) * (float)iw);
         }

         i2 = this.gradient[aveW];
         ach += (i2 >>> 20 & 4080) * aveB >> 16;
         rch += (i2 >>> 12 & 4080) * aveB >> 16;
         gch += (i2 >>> 4 & 4080) * aveB >> 16;
         bch += (i2 << 4 & 4080) * aveB >> 16;
         if (p2_up) {
            aveB = (int)((1.0F - (p2 - (float)idx2)) * (float)iw);
         } else {
            aveB = (int)((p2 - (float)idx2) * (float)iw);
         }

         i2 = this.gradient[idx2];
         ach += (i2 >>> 20 & 4080) * aveB >> 16;
         rch += (i2 >>> 12 & 4080) * aveB >> 16;
         gch += (i2 >>> 4 & 4080) * aveB >> 16;
         bch += (i2 << 4 & 4080) * aveB >> 16;
         ach = ach + 8 >> 4;
         rch = rch + 8 >> 4;
         gch = gch + 8 >> 4;
         bch = bch + 8 >> 4;
      } else {
         aveW = 0;
         idx2 = 0;
         i1 = -1;
         i2 = -1;
         float f1 = 0.0F;
         float f2 = 0.0F;

         int pix;
         for(pix = 0; pix < this.gradientsLength; ++pix) {
            if (p1 < this.fractions[pix + 1] && i1 == -1) {
               i1 = pix;
               f1 = p1 - this.fractions[pix];
               f1 = f1 / this.normalizedIntervals[pix] * 255.0F;
               aveW = (int)f1;
               if (i2 != -1) {
                  break;
               }
            }

            if (p2 < this.fractions[pix + 1] && i2 == -1) {
               i2 = pix;
               f2 = p2 - this.fractions[pix];
               f2 = f2 / this.normalizedIntervals[pix] * 255.0F;
               idx2 = (int)f2;
               if (i1 != -1) {
                  break;
               }
            }
         }

         if (i1 == -1) {
            i1 = this.gradients.length - 1;
            aveW = 255;
            f1 = (float)255;
         }

         if (i2 == -1) {
            i2 = this.gradients.length - 1;
            idx2 = 255;
            f2 = (float)255;
         }

         if (i1 == i2 && aveW <= idx2 && p1_up && !p2_up) {
            return this.gradients[i1][aveW + idx2 + 1 >> 1];
         }

         int base = (int)(65536.0F / sz);
         int norm;
         int iStart;
         if (i1 < i2 && p1_up && !p2_up) {
            norm = (int)((float)base * this.normalizedIntervals[i1] * (255.0F - f1) / 255.0F);
            pix = this.gradients[i1][aveW + 256 >> 1];
            ach += (pix >>> 20 & 4080) * norm >> 16;
            rch += (pix >>> 12 & 4080) * norm >> 16;
            gch += (pix >>> 4 & 4080) * norm >> 16;
            bch += (pix << 4 & 4080) * norm >> 16;

            for(iStart = i1 + 1; iStart < i2; ++iStart) {
               norm = (int)((float)base * this.normalizedIntervals[iStart]);
               pix = this.gradients[iStart][128];
               ach += (pix >>> 20 & 4080) * norm >> 16;
               rch += (pix >>> 12 & 4080) * norm >> 16;
               gch += (pix >>> 4 & 4080) * norm >> 16;
               bch += (pix << 4 & 4080) * norm >> 16;
            }

            norm = (int)((float)base * this.normalizedIntervals[i2] * f2 / 255.0F);
            pix = this.gradients[i2][idx2 + 1 >> 1];
            ach += (pix >>> 20 & 4080) * norm >> 16;
            rch += (pix >>> 12 & 4080) * norm >> 16;
            gch += (pix >>> 4 & 4080) * norm >> 16;
            bch += (pix << 4 & 4080) * norm >> 16;
         } else {
            if (p1_up) {
               norm = (int)((float)base * this.normalizedIntervals[i1] * (255.0F - f1) / 255.0F);
               pix = this.gradients[i1][aveW + 256 >> 1];
            } else {
               norm = (int)((float)base * this.normalizedIntervals[i1] * f1 / 255.0F);
               pix = this.gradients[i1][aveW + 1 >> 1];
            }

            ach += (pix >>> 20 & 4080) * norm >> 16;
            rch += (pix >>> 12 & 4080) * norm >> 16;
            gch += (pix >>> 4 & 4080) * norm >> 16;
            bch += (pix << 4 & 4080) * norm >> 16;
            if (p2_up) {
               norm = (int)((float)base * this.normalizedIntervals[i2] * (255.0F - f2) / 255.0F);
               pix = this.gradients[i2][idx2 + 256 >> 1];
            } else {
               norm = (int)((float)base * this.normalizedIntervals[i2] * f2 / 255.0F);
               pix = this.gradients[i2][idx2 + 1 >> 1];
            }

            ach += (pix >>> 20 & 4080) * norm >> 16;
            rch += (pix >>> 12 & 4080) * norm >> 16;
            gch += (pix >>> 4 & 4080) * norm >> 16;
            bch += (pix << 4 & 4080) * norm >> 16;
            int iEnd;
            if (p1_up) {
               iStart = i1 + 1;
               iEnd = this.gradientsLength;
            } else {
               iStart = 0;
               iEnd = i1;
            }

            int i;
            for(i = iStart; i < iEnd; ++i) {
               norm = (int)((float)base * this.normalizedIntervals[i]);
               pix = this.gradients[i][128];
               ach += (pix >>> 20 & 4080) * norm >> 16;
               rch += (pix >>> 12 & 4080) * norm >> 16;
               gch += (pix >>> 4 & 4080) * norm >> 16;
               bch += (pix << 4 & 4080) * norm >> 16;
            }

            if (p2_up) {
               iStart = i2 + 1;
               iEnd = this.gradientsLength;
            } else {
               iStart = 0;
               iEnd = i2;
            }

            for(i = iStart; i < iEnd; ++i) {
               norm = (int)((float)base * this.normalizedIntervals[i]);
               pix = this.gradients[i][128];
               ach += (pix >>> 20 & 4080) * norm >> 16;
               rch += (pix >>> 12 & 4080) * norm >> 16;
               gch += (pix >>> 4 & 4080) * norm >> 16;
               bch += (pix << 4 & 4080) * norm >> 16;
            }
         }

         ach = ach + 8 >> 4;
         rch = rch + 8 >> 4;
         gch = gch + 8 >> 4;
         bch = bch + 8 >> 4;
      }

      if (weight != 1.0F) {
         aveW = (int)(65536.0F * (1.0F - weight));
         idx2 = (this.gradientAverage >>> 24 & 255) * aveW;
         i1 = (this.gradientAverage >> 16 & 255) * aveW;
         i2 = (this.gradientAverage >> 8 & 255) * aveW;
         aveB = (this.gradientAverage & 255) * aveW;
         iw = (int)(weight * 65536.0F);
         ach = ach * iw + idx2 >> 16;
         rch = rch * iw + i1 >> 16;
         gch = gch * iw + i2 >> 16;
         bch = bch * iw + aveB >> 16;
      }

      return ach << 24 | rch << 16 | gch << 8 | bch;
   }

   private static int convertSRGBtoLinearRGB(int color) {
      float input = (float)color / 255.0F;
      float output;
      if (input <= 0.04045F) {
         output = input / 12.92F;
      } else {
         output = (float)Math.pow(((double)input + 0.055) / 1.055, 2.4);
      }

      int o = Math.round(output * 255.0F);
      return o;
   }

   private static int convertLinearRGBtoSRGB(int color) {
      float input = (float)color / 255.0F;
      float output;
      if (input <= 0.0031308F) {
         output = input * 12.92F;
      } else {
         output = 1.055F * (float)Math.pow((double)input, 0.4166666666666667) - 0.055F;
      }

      int o = Math.round(output * 255.0F);
      return o;
   }

   public final Raster getRaster(int x, int y, int w, int h) {
      if (w != 0 && h != 0) {
         WritableRaster raster = this.saved;
         if (raster == null || raster.getWidth() < w || raster.getHeight() < h) {
            raster = getCachedRaster(this.dataModel, w, h);
            this.saved = raster;
            raster = raster.createWritableChild(raster.getMinX(), raster.getMinY(), w, h, 0, 0, (int[])null);
         }

         DataBufferInt rasterDB = (DataBufferInt)raster.getDataBuffer();
         int[] pixels = rasterDB.getBankData()[0];
         int off = rasterDB.getOffset();
         int scanlineStride = ((SinglePixelPackedSampleModel)raster.getSampleModel()).getScanlineStride();
         int adjust = scanlineStride - w;
         this.fillRaster(pixels, off, adjust, x, y, w, h);
         GraphicsUtil.coerceData(raster, this.dataModel, this.model.isAlphaPremultiplied());
         return raster;
      } else {
         return null;
      }
   }

   protected abstract void fillRaster(int[] var1, int var2, int var3, int var4, int var5, int var6, int var7);

   protected static final synchronized WritableRaster getCachedRaster(ColorModel cm, int w, int h) {
      if (cm == cachedModel && cached != null) {
         WritableRaster ras = (WritableRaster)cached.get();
         if (ras != null && ras.getWidth() >= w && ras.getHeight() >= h) {
            cached = null;
            return ras;
         }
      }

      if (w < 32) {
         w = 32;
      }

      if (h < 32) {
         h = 32;
      }

      return cm.createCompatibleWritableRaster(w, h);
   }

   protected static final synchronized void putCachedRaster(ColorModel cm, WritableRaster ras) {
      if (cached != null) {
         WritableRaster cras = (WritableRaster)cached.get();
         if (cras != null) {
            int cw = cras.getWidth();
            int ch = cras.getHeight();
            int iw = ras.getWidth();
            int ih = ras.getHeight();
            if (cw >= iw && ch >= ih) {
               return;
            }

            if (cw * ch >= iw * ih) {
               return;
            }
         }
      }

      cachedModel = cm;
      cached = new WeakReference(ras);
   }

   public final void dispose() {
      if (this.saved != null) {
         putCachedRaster(this.model, this.saved);
         this.saved = null;
      }

   }

   public final ColorModel getColorModel() {
      return this.model;
   }

   static {
      for(int k = 0; k < 256; ++k) {
         SRGBtoLinearRGB[k] = convertSRGBtoLinearRGB(k);
         LinearRGBtoSRGB[k] = convertLinearRGBtoSRGB(k);
      }

   }
}
