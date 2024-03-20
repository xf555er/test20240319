package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;

public final class TurbulencePatternRed extends AbstractRed {
   private StitchInfo stitchInfo = null;
   private static final AffineTransform IDENTITY = new AffineTransform();
   private double baseFrequencyX;
   private double baseFrequencyY;
   private int numOctaves;
   private int seed;
   private Rectangle2D tile;
   private AffineTransform txf;
   private boolean isFractalNoise;
   private int[] channels;
   double[] tx = new double[]{1.0, 0.0};
   double[] ty = new double[]{0.0, 1.0};
   private static final int RAND_m = Integer.MAX_VALUE;
   private static final int RAND_a = 16807;
   private static final int RAND_q = 127773;
   private static final int RAND_r = 2836;
   private static final int BSize = 256;
   private static final int BM = 255;
   private static final double PerlinN = 4096.0;
   private final int[] latticeSelector = new int[257];
   private final double[] gradient = new double[2056];

   public double getBaseFrequencyX() {
      return this.baseFrequencyX;
   }

   public double getBaseFrequencyY() {
      return this.baseFrequencyY;
   }

   public int getNumOctaves() {
      return this.numOctaves;
   }

   public int getSeed() {
      return this.seed;
   }

   public Rectangle2D getTile() {
      return (Rectangle2D)this.tile.clone();
   }

   public boolean isFractalNoise() {
      return this.isFractalNoise;
   }

   public boolean[] getChannels() {
      boolean[] channels = new boolean[4];
      int[] var2 = this.channels;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int channel = var2[var4];
         channels[channel] = true;
      }

      return channels;
   }

   public final int setupSeed(int seed) {
      if (seed <= 0) {
         seed = -(seed % 2147483646) + 1;
      }

      if (seed > 2147483646) {
         seed = 2147483646;
      }

      return seed;
   }

   public final int random(int seed) {
      int result = 16807 * (seed % 127773) - 2836 * (seed / 127773);
      if (result <= 0) {
         result += Integer.MAX_VALUE;
      }

      return result;
   }

   private void initLattice(int seed) {
      seed = this.setupSeed(seed);

      double s;
      int i;
      int k;
      for(k = 0; k < 4; ++k) {
         for(i = 0; i < 256; ++i) {
            double u = (double)((seed = this.random(seed)) % 512 - 256);
            double v = (double)((seed = this.random(seed)) % 512 - 256);
            s = 1.0 / Math.sqrt(u * u + v * v);
            this.gradient[i * 8 + k * 2] = u * s;
            this.gradient[i * 8 + k * 2 + 1] = v * s;
         }
      }

      for(i = 0; i < 256; this.latticeSelector[i] = i++) {
      }

      while(true) {
         --i;
         int j;
         if (i <= 0) {
            this.latticeSelector[256] = this.latticeSelector[0];

            for(j = 0; j < 8; ++j) {
               this.gradient[2048 + j] = this.gradient[j];
            }

            return;
         }

         k = this.latticeSelector[i];
         j = (seed = this.random(seed)) % 256;
         this.latticeSelector[i] = this.latticeSelector[j];
         this.latticeSelector[j] = k;
         int s1 = i << 3;
         int s2 = j << 3;

         for(j = 0; j < 8; ++j) {
            s = this.gradient[s1 + j];
            this.gradient[s1 + j] = this.gradient[s2 + j];
            this.gradient[s2 + j] = s;
         }
      }
   }

   private static final double s_curve(double t) {
      return t * t * (3.0 - 2.0 * t);
   }

   private static final double lerp(double t, double a, double b) {
      return a + t * (b - a);
   }

   private final void noise2(double[] noise, double vec0, double vec1) {
      vec0 += 4096.0;
      int b0 = (int)vec0 & 255;
      int i = this.latticeSelector[b0];
      int j = this.latticeSelector[b0 + 1];
      double rx0 = vec0 - (double)((int)vec0);
      double rx1 = rx0 - 1.0;
      double sx = s_curve(rx0);
      vec1 += 4096.0;
      b0 = (int)vec1;
      int b1 = (j + b0 & 255) << 3;
      b0 = (i + b0 & 255) << 3;
      double ry0 = vec1 - (double)((int)vec1);
      double ry1 = ry0 - 1.0;
      double sy = s_curve(ry0);
      switch (this.channels.length) {
         case 4:
            noise[3] = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 6] + ry0 * this.gradient[b0 + 7], rx1 * this.gradient[b1 + 6] + ry0 * this.gradient[b1 + 7]), lerp(sx, rx0 * this.gradient[b0 + 8 + 6] + ry1 * this.gradient[b0 + 8 + 7], rx1 * this.gradient[b1 + 8 + 6] + ry1 * this.gradient[b1 + 8 + 7]));
         case 3:
            noise[2] = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 4] + ry0 * this.gradient[b0 + 5], rx1 * this.gradient[b1 + 4] + ry0 * this.gradient[b1 + 5]), lerp(sx, rx0 * this.gradient[b0 + 8 + 4] + ry1 * this.gradient[b0 + 8 + 5], rx1 * this.gradient[b1 + 8 + 4] + ry1 * this.gradient[b1 + 8 + 5]));
         case 2:
            noise[1] = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 2] + ry0 * this.gradient[b0 + 3], rx1 * this.gradient[b1 + 2] + ry0 * this.gradient[b1 + 3]), lerp(sx, rx0 * this.gradient[b0 + 8 + 2] + ry1 * this.gradient[b0 + 8 + 3], rx1 * this.gradient[b1 + 8 + 2] + ry1 * this.gradient[b1 + 8 + 3]));
         case 1:
            noise[0] = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 0] + ry0 * this.gradient[b0 + 1], rx1 * this.gradient[b1 + 0] + ry0 * this.gradient[b1 + 1]), lerp(sx, rx0 * this.gradient[b0 + 8 + 0] + ry1 * this.gradient[b0 + 8 + 1], rx1 * this.gradient[b1 + 8 + 0] + ry1 * this.gradient[b1 + 8 + 1]));
         default:
      }
   }

   private final void noise2Stitch(double[] noise, double vec0, double vec1, StitchInfo stitchInfo) {
      double t = vec0 + 4096.0;
      int b0 = (int)t;
      int b1 = b0 + 1;
      if (b1 >= stitchInfo.wrapX) {
         if (b0 >= stitchInfo.wrapX) {
            b0 -= stitchInfo.width;
            b1 -= stitchInfo.width;
         } else {
            b1 -= stitchInfo.width;
         }
      }

      int i = this.latticeSelector[b0 & 255];
      int j = this.latticeSelector[b1 & 255];
      double rx0 = t - (double)((int)t);
      double rx1 = rx0 - 1.0;
      double sx = s_curve(rx0);
      t = vec1 + 4096.0;
      b0 = (int)t;
      b1 = b0 + 1;
      if (b1 >= stitchInfo.wrapY) {
         if (b0 >= stitchInfo.wrapY) {
            b0 -= stitchInfo.height;
            b1 -= stitchInfo.height;
         } else {
            b1 -= stitchInfo.height;
         }
      }

      int b00 = (i + b0 & 255) << 3;
      int b10 = (j + b0 & 255) << 3;
      int b01 = (i + b1 & 255) << 3;
      int b11 = (j + b1 & 255) << 3;
      double ry0 = t - (double)((int)t);
      double ry1 = ry0 - 1.0;
      double sy = s_curve(ry0);
      switch (this.channels.length) {
         case 4:
            noise[3] = lerp(sy, lerp(sx, rx0 * this.gradient[b00 + 6] + ry0 * this.gradient[b00 + 7], rx1 * this.gradient[b10 + 6] + ry0 * this.gradient[b10 + 7]), lerp(sx, rx0 * this.gradient[b01 + 6] + ry1 * this.gradient[b01 + 7], rx1 * this.gradient[b11 + 6] + ry1 * this.gradient[b11 + 7]));
         case 3:
            noise[2] = lerp(sy, lerp(sx, rx0 * this.gradient[b00 + 4] + ry0 * this.gradient[b00 + 5], rx1 * this.gradient[b10 + 4] + ry0 * this.gradient[b10 + 5]), lerp(sx, rx0 * this.gradient[b01 + 4] + ry1 * this.gradient[b01 + 5], rx1 * this.gradient[b11 + 4] + ry1 * this.gradient[b11 + 5]));
         case 2:
            noise[1] = lerp(sy, lerp(sx, rx0 * this.gradient[b00 + 2] + ry0 * this.gradient[b00 + 3], rx1 * this.gradient[b10 + 2] + ry0 * this.gradient[b10 + 3]), lerp(sx, rx0 * this.gradient[b01 + 2] + ry1 * this.gradient[b01 + 3], rx1 * this.gradient[b11 + 2] + ry1 * this.gradient[b11 + 3]));
         case 1:
            noise[0] = lerp(sy, lerp(sx, rx0 * this.gradient[b00 + 0] + ry0 * this.gradient[b00 + 1], rx1 * this.gradient[b10 + 0] + ry0 * this.gradient[b10 + 1]), lerp(sx, rx0 * this.gradient[b01 + 0] + ry1 * this.gradient[b01 + 1], rx1 * this.gradient[b11 + 0] + ry1 * this.gradient[b11 + 1]));
         default:
      }
   }

   private final int turbulence_4(double pointX, double pointY, double[] fSum) {
      double ratio = 255.0;
      pointX *= this.baseFrequencyX;
      pointY *= this.baseFrequencyY;
      fSum[0] = fSum[1] = fSum[2] = fSum[3] = 0.0;

      int i;
      int j;
      for(int nOctave = this.numOctaves; nOctave > 0; --nOctave) {
         double px = pointX + 4096.0;
         int b0 = (int)px & 255;
         i = this.latticeSelector[b0];
         j = this.latticeSelector[b0 + 1];
         double rx0 = px - (double)((int)px);
         double rx1 = rx0 - 1.0;
         double sx = s_curve(rx0);
         double py = pointY + 4096.0;
         b0 = (int)py & 255;
         int b1 = b0 + 1 & 255;
         b1 = (j + b0 & 255) << 3;
         b0 = (i + b0 & 255) << 3;
         double ry0 = py - (double)((int)py);
         double ry1 = ry0 - 1.0;
         double sy = s_curve(ry0);
         double n = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 0] + ry0 * this.gradient[b0 + 1], rx1 * this.gradient[b1 + 0] + ry0 * this.gradient[b1 + 1]), lerp(sx, rx0 * this.gradient[b0 + 8 + 0] + ry1 * this.gradient[b0 + 8 + 1], rx1 * this.gradient[b1 + 8 + 0] + ry1 * this.gradient[b1 + 8 + 1]));
         if (n < 0.0) {
            fSum[0] -= n * ratio;
         } else {
            fSum[0] += n * ratio;
         }

         n = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 2] + ry0 * this.gradient[b0 + 3], rx1 * this.gradient[b1 + 2] + ry0 * this.gradient[b1 + 3]), lerp(sx, rx0 * this.gradient[b0 + 8 + 2] + ry1 * this.gradient[b0 + 8 + 3], rx1 * this.gradient[b1 + 8 + 2] + ry1 * this.gradient[b1 + 8 + 3]));
         if (n < 0.0) {
            fSum[1] -= n * ratio;
         } else {
            fSum[1] += n * ratio;
         }

         n = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 4] + ry0 * this.gradient[b0 + 5], rx1 * this.gradient[b1 + 4] + ry0 * this.gradient[b1 + 5]), lerp(sx, rx0 * this.gradient[b0 + 8 + 4] + ry1 * this.gradient[b0 + 8 + 5], rx1 * this.gradient[b1 + 8 + 4] + ry1 * this.gradient[b1 + 8 + 5]));
         if (n < 0.0) {
            fSum[2] -= n * ratio;
         } else {
            fSum[2] += n * ratio;
         }

         n = lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 6] + ry0 * this.gradient[b0 + 7], rx1 * this.gradient[b1 + 6] + ry0 * this.gradient[b1 + 7]), lerp(sx, rx0 * this.gradient[b0 + 8 + 6] + ry1 * this.gradient[b0 + 8 + 7], rx1 * this.gradient[b1 + 8 + 6] + ry1 * this.gradient[b1 + 8 + 7]));
         if (n < 0.0) {
            fSum[3] -= n * ratio;
         } else {
            fSum[3] += n * ratio;
         }

         ratio *= 0.5;
         pointX *= 2.0;
         pointY *= 2.0;
      }

      i = (int)fSum[0];
      if ((i & -256) == 0) {
         j = i << 16;
      } else {
         j = (i & Integer.MIN_VALUE) != 0 ? 0 : 16711680;
      }

      i = (int)fSum[1];
      if ((i & -256) == 0) {
         j |= i << 8;
      } else {
         j |= (i & Integer.MIN_VALUE) != 0 ? 0 : '\uff00';
      }

      i = (int)fSum[2];
      if ((i & -256) == 0) {
         j |= i;
      } else {
         j |= (i & Integer.MIN_VALUE) != 0 ? 0 : 255;
      }

      i = (int)fSum[3];
      if ((i & -256) == 0) {
         j |= i << 24;
      } else {
         j |= (i & Integer.MIN_VALUE) != 0 ? 0 : -16777216;
      }

      return j;
   }

   private final void turbulence(int[] rgb, double pointX, double pointY, double[] fSum, double[] noise) {
      fSum[0] = fSum[1] = fSum[2] = fSum[3] = 0.0;
      double ratio = 255.0;
      pointX *= this.baseFrequencyX;
      pointY *= this.baseFrequencyY;
      int nOctave;
      switch (this.channels.length) {
         case 1:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2(noise, pointX, pointY);
               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
            }

            rgb[0] = (int)fSum[0];
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
            break;
         case 2:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2(noise, pointX, pointY);
               if (noise[1] < 0.0) {
                  fSum[1] -= noise[1] * ratio;
               } else {
                  fSum[1] += noise[1] * ratio;
               }

               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
            }

            rgb[1] = (int)fSum[1];
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[0] = (int)fSum[0];
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
            break;
         case 3:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2(noise, pointX, pointY);
               if (noise[2] < 0.0) {
                  fSum[2] -= noise[2] * ratio;
               } else {
                  fSum[2] += noise[2] * ratio;
               }

               if (noise[1] < 0.0) {
                  fSum[1] -= noise[1] * ratio;
               } else {
                  fSum[1] += noise[1] * ratio;
               }

               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
            }

            rgb[2] = (int)fSum[2];
            if ((rgb[2] & -256) != 0) {
               rgb[2] = (rgb[2] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[1] = (int)fSum[1];
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[0] = (int)fSum[0];
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
            break;
         case 4:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2(noise, pointX, pointY);
               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               if (noise[1] < 0.0) {
                  fSum[1] -= noise[1] * ratio;
               } else {
                  fSum[1] += noise[1] * ratio;
               }

               if (noise[2] < 0.0) {
                  fSum[2] -= noise[2] * ratio;
               } else {
                  fSum[2] += noise[2] * ratio;
               }

               if (noise[3] < 0.0) {
                  fSum[3] -= noise[3] * ratio;
               } else {
                  fSum[3] += noise[3] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
            }

            rgb[0] = (int)fSum[0];
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[1] = (int)fSum[1];
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[2] = (int)fSum[2];
            if ((rgb[2] & -256) != 0) {
               rgb[2] = (rgb[2] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[3] = (int)fSum[3];
            if ((rgb[3] & -256) != 0) {
               rgb[3] = (rgb[3] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
      }

   }

   private final void turbulenceStitch(int[] rgb, double pointX, double pointY, double[] fSum, double[] noise, StitchInfo stitchInfo) {
      double ratio = 1.0;
      pointX *= this.baseFrequencyX;
      pointY *= this.baseFrequencyY;
      fSum[0] = fSum[1] = fSum[2] = fSum[3] = 0.0;
      int nOctave;
      switch (this.channels.length) {
         case 1:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2Stitch(noise, pointX, pointY, stitchInfo);
               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
               stitchInfo.doubleFrequency();
            }

            rgb[0] = (int)(fSum[0] * 255.0);
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
            break;
         case 2:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2Stitch(noise, pointX, pointY, stitchInfo);
               if (noise[1] < 0.0) {
                  fSum[1] -= noise[1] * ratio;
               } else {
                  fSum[1] += noise[1] * ratio;
               }

               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
               stitchInfo.doubleFrequency();
            }

            rgb[1] = (int)(fSum[1] * 255.0);
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[0] = (int)(fSum[0] * 255.0);
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
            break;
         case 3:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2Stitch(noise, pointX, pointY, stitchInfo);
               if (noise[2] < 0.0) {
                  fSum[2] -= noise[2] * ratio;
               } else {
                  fSum[2] += noise[2] * ratio;
               }

               if (noise[1] < 0.0) {
                  fSum[1] -= noise[1] * ratio;
               } else {
                  fSum[1] += noise[1] * ratio;
               }

               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
               stitchInfo.doubleFrequency();
            }

            rgb[2] = (int)(fSum[2] * 255.0);
            if ((rgb[2] & -256) != 0) {
               rgb[2] = (rgb[2] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[1] = (int)(fSum[1] * 255.0);
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[0] = (int)(fSum[0] * 255.0);
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
            break;
         case 4:
            for(nOctave = 0; nOctave < this.numOctaves; ++nOctave) {
               this.noise2Stitch(noise, pointX, pointY, stitchInfo);
               if (noise[3] < 0.0) {
                  fSum[3] -= noise[3] * ratio;
               } else {
                  fSum[3] += noise[3] * ratio;
               }

               if (noise[2] < 0.0) {
                  fSum[2] -= noise[2] * ratio;
               } else {
                  fSum[2] += noise[2] * ratio;
               }

               if (noise[1] < 0.0) {
                  fSum[1] -= noise[1] * ratio;
               } else {
                  fSum[1] += noise[1] * ratio;
               }

               if (noise[0] < 0.0) {
                  fSum[0] -= noise[0] * ratio;
               } else {
                  fSum[0] += noise[0] * ratio;
               }

               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
               stitchInfo.doubleFrequency();
            }

            rgb[3] = (int)(fSum[3] * 255.0);
            if ((rgb[3] & -256) != 0) {
               rgb[3] = (rgb[3] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[2] = (int)(fSum[2] * 255.0);
            if ((rgb[2] & -256) != 0) {
               rgb[2] = (rgb[2] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[1] = (int)(fSum[1] * 255.0);
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }

            rgb[0] = (int)(fSum[0] * 255.0);
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
      }

   }

   private final int turbulenceFractal_4(double pointX, double pointY, double[] fSum) {
      double ratio = 127.5;
      pointX *= this.baseFrequencyX;
      pointY *= this.baseFrequencyY;
      fSum[0] = fSum[1] = fSum[2] = fSum[3] = 127.5;

      int i;
      int j;
      for(int nOctave = this.numOctaves; nOctave > 0; --nOctave) {
         double px = pointX + 4096.0;
         int b0 = (int)px & 255;
         i = this.latticeSelector[b0];
         j = this.latticeSelector[b0 + 1];
         double rx0 = px - (double)((int)px);
         double rx1 = rx0 - 1.0;
         double sx = s_curve(rx0);
         double py = pointY + 4096.0;
         b0 = (int)py & 255;
         int b1 = b0 + 1 & 255;
         b1 = (j + b0 & 255) << 3;
         b0 = (i + b0 & 255) << 3;
         double ry0 = py - (double)((int)py);
         double ry1 = ry0 - 1.0;
         double sy = s_curve(ry0);
         fSum[0] += lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 0] + ry0 * this.gradient[b0 + 1], rx1 * this.gradient[b1 + 0] + ry0 * this.gradient[b1 + 1]), lerp(sx, rx0 * this.gradient[b0 + 8 + 0] + ry1 * this.gradient[b0 + 8 + 1], rx1 * this.gradient[b1 + 8 + 0] + ry1 * this.gradient[b1 + 8 + 1])) * ratio;
         fSum[1] += lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 2] + ry0 * this.gradient[b0 + 3], rx1 * this.gradient[b1 + 2] + ry0 * this.gradient[b1 + 3]), lerp(sx, rx0 * this.gradient[b0 + 8 + 2] + ry1 * this.gradient[b0 + 8 + 3], rx1 * this.gradient[b1 + 8 + 2] + ry1 * this.gradient[b1 + 8 + 3])) * ratio;
         fSum[2] += lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 4] + ry0 * this.gradient[b0 + 5], rx1 * this.gradient[b1 + 4] + ry0 * this.gradient[b1 + 5]), lerp(sx, rx0 * this.gradient[b0 + 8 + 4] + ry1 * this.gradient[b0 + 8 + 5], rx1 * this.gradient[b1 + 8 + 4] + ry1 * this.gradient[b1 + 8 + 5])) * ratio;
         fSum[3] += lerp(sy, lerp(sx, rx0 * this.gradient[b0 + 6] + ry0 * this.gradient[b0 + 7], rx1 * this.gradient[b1 + 6] + ry0 * this.gradient[b1 + 7]), lerp(sx, rx0 * this.gradient[b0 + 8 + 6] + ry1 * this.gradient[b0 + 8 + 7], rx1 * this.gradient[b1 + 8 + 6] + ry1 * this.gradient[b1 + 8 + 7])) * ratio;
         ratio *= 0.5;
         pointX *= 2.0;
         pointY *= 2.0;
      }

      i = (int)fSum[0];
      if ((i & -256) == 0) {
         j = i << 16;
      } else {
         j = (i & Integer.MIN_VALUE) != 0 ? 0 : 16711680;
      }

      i = (int)fSum[1];
      if ((i & -256) == 0) {
         j |= i << 8;
      } else {
         j |= (i & Integer.MIN_VALUE) != 0 ? 0 : '\uff00';
      }

      i = (int)fSum[2];
      if ((i & -256) == 0) {
         j |= i;
      } else {
         j |= (i & Integer.MIN_VALUE) != 0 ? 0 : 255;
      }

      i = (int)fSum[3];
      if ((i & -256) == 0) {
         j |= i << 24;
      } else {
         j |= (i & Integer.MIN_VALUE) != 0 ? 0 : -16777216;
      }

      return j;
   }

   private final void turbulenceFractal(int[] rgb, double pointX, double pointY, double[] fSum, double[] noise) {
      double ratio = 127.5;
      fSum[0] = fSum[1] = fSum[2] = fSum[3] = 127.5;
      pointX *= this.baseFrequencyX;
      pointY *= this.baseFrequencyY;
      int nOctave = this.numOctaves;

      while(nOctave > 0) {
         this.noise2(noise, pointX, pointY);
         switch (this.channels.length) {
            case 4:
               fSum[3] += noise[3] * ratio;
            case 3:
               fSum[2] += noise[2] * ratio;
            case 2:
               fSum[1] += noise[1] * ratio;
            case 1:
               fSum[0] += noise[0] * ratio;
            default:
               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
               --nOctave;
         }
      }

      switch (this.channels.length) {
         case 4:
            rgb[3] = (int)fSum[3];
            if ((rgb[3] & -256) != 0) {
               rgb[3] = (rgb[3] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         case 3:
            rgb[2] = (int)fSum[2];
            if ((rgb[2] & -256) != 0) {
               rgb[2] = (rgb[2] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         case 2:
            rgb[1] = (int)fSum[1];
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         case 1:
            rgb[0] = (int)fSum[0];
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         default:
      }
   }

   private final void turbulenceFractalStitch(int[] rgb, double pointX, double pointY, double[] fSum, double[] noise, StitchInfo stitchInfo) {
      double ratio = 127.5;
      fSum[0] = fSum[1] = fSum[2] = fSum[3] = 127.5;
      pointX *= this.baseFrequencyX;
      pointY *= this.baseFrequencyY;
      int nOctave = this.numOctaves;

      while(nOctave > 0) {
         this.noise2Stitch(noise, pointX, pointY, stitchInfo);
         switch (this.channels.length) {
            case 4:
               fSum[3] += noise[3] * ratio;
            case 3:
               fSum[2] += noise[2] * ratio;
            case 2:
               fSum[1] += noise[1] * ratio;
            case 1:
               fSum[0] += noise[0] * ratio;
            default:
               ratio *= 0.5;
               pointX *= 2.0;
               pointY *= 2.0;
               stitchInfo.doubleFrequency();
               --nOctave;
         }
      }

      switch (this.channels.length) {
         case 4:
            rgb[3] = (int)fSum[3];
            if ((rgb[3] & -256) != 0) {
               rgb[3] = (rgb[3] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         case 3:
            rgb[2] = (int)fSum[2];
            if ((rgb[2] & -256) != 0) {
               rgb[2] = (rgb[2] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         case 2:
            rgb[1] = (int)fSum[1];
            if ((rgb[1] & -256) != 0) {
               rgb[1] = (rgb[1] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         case 1:
            rgb[0] = (int)fSum[0];
            if ((rgb[0] & -256) != 0) {
               rgb[0] = (rgb[0] & Integer.MIN_VALUE) != 0 ? 0 : 255;
            }
         default:
      }
   }

   public WritableRaster copyData(WritableRaster dest) {
      if (dest == null) {
         throw new IllegalArgumentException("Cannot generate a noise pattern into a null raster");
      } else {
         int w = dest.getWidth();
         int h = dest.getHeight();
         DataBufferInt dstDB = (DataBufferInt)dest.getDataBuffer();
         int minX = dest.getMinX();
         int minY = dest.getMinY();
         SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)dest.getSampleModel();
         int dstOff = dstDB.getOffset() + sppsm.getOffset(minX - dest.getSampleModelTranslateX(), minY - dest.getSampleModelTranslateY());
         int[] destPixels = dstDB.getBankData()[0];
         int dstAdjust = sppsm.getScanlineStride() - w;
         int dp = dstOff;
         int[] rgb = new int[4];
         double[] fSum = new double[]{0.0, 0.0, 0.0, 0.0};
         double[] noise = new double[]{0.0, 0.0, 0.0, 0.0};
         double tx0 = this.tx[0];
         double tx1 = this.tx[1];
         double ty0 = this.ty[0] - (double)w * tx0;
         double ty1 = this.ty[1] - (double)w * tx1;
         double[] p = new double[]{(double)minX, (double)minY};
         this.txf.transform(p, 0, p, 0, 1);
         double point_0 = p[0];
         double point_1 = p[1];
         int i;
         int end;
         StitchInfo si;
         if (this.isFractalNoise) {
            if (this.stitchInfo == null) {
               if (this.channels.length == 4) {
                  for(i = 0; i < h; ++i) {
                     for(end = dp + w; dp < end; ++dp) {
                        destPixels[dp] = this.turbulenceFractal_4(point_0, point_1, fSum);
                        point_0 += tx0;
                        point_1 += tx1;
                     }

                     point_0 += ty0;
                     point_1 += ty1;
                     dp += dstAdjust;
                  }
               } else {
                  for(i = 0; i < h; ++i) {
                     for(end = dp + w; dp < end; ++dp) {
                        this.turbulenceFractal(rgb, point_0, point_1, fSum, noise);
                        destPixels[dp] = rgb[3] << 24 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
                        point_0 += tx0;
                        point_1 += tx1;
                     }

                     point_0 += ty0;
                     point_1 += ty1;
                     dp += dstAdjust;
                  }
               }
            } else {
               si = new StitchInfo();

               for(i = 0; i < h; ++i) {
                  for(end = dp + w; dp < end; ++dp) {
                     si.assign(this.stitchInfo);
                     this.turbulenceFractalStitch(rgb, point_0, point_1, fSum, noise, si);
                     destPixels[dp] = rgb[3] << 24 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
                     point_0 += tx0;
                     point_1 += tx1;
                  }

                  point_0 += ty0;
                  point_1 += ty1;
                  dp += dstAdjust;
               }
            }
         } else if (this.stitchInfo == null) {
            if (this.channels.length == 4) {
               for(i = 0; i < h; ++i) {
                  for(end = dp + w; dp < end; ++dp) {
                     destPixels[dp] = this.turbulence_4(point_0, point_1, fSum);
                     point_0 += tx0;
                     point_1 += tx1;
                  }

                  point_0 += ty0;
                  point_1 += ty1;
                  dp += dstAdjust;
               }
            } else {
               for(i = 0; i < h; ++i) {
                  for(end = dp + w; dp < end; ++dp) {
                     this.turbulence(rgb, point_0, point_1, fSum, noise);
                     destPixels[dp] = rgb[3] << 24 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
                     point_0 += tx0;
                     point_1 += tx1;
                  }

                  point_0 += ty0;
                  point_1 += ty1;
                  dp += dstAdjust;
               }
            }
         } else {
            si = new StitchInfo();

            for(i = 0; i < h; ++i) {
               for(end = dp + w; dp < end; ++dp) {
                  si.assign(this.stitchInfo);
                  this.turbulenceStitch(rgb, point_0, point_1, fSum, noise, si);
                  destPixels[dp] = rgb[3] << 24 | rgb[0] << 16 | rgb[1] << 8 | rgb[2];
                  point_0 += tx0;
                  point_1 += tx1;
               }

               point_0 += ty0;
               point_1 += ty1;
               dp += dstAdjust;
            }
         }

         return dest;
      }
   }

   public TurbulencePatternRed(double baseFrequencyX, double baseFrequencyY, int numOctaves, int seed, boolean isFractalNoise, Rectangle2D tile, AffineTransform txf, Rectangle devRect, ColorSpace cs, boolean alpha) {
      this.baseFrequencyX = baseFrequencyX;
      this.baseFrequencyY = baseFrequencyY;
      this.seed = seed;
      this.isFractalNoise = isFractalNoise;
      this.tile = tile;
      this.txf = txf;
      if (this.txf == null) {
         this.txf = IDENTITY;
      }

      int nChannels = cs.getNumComponents();
      if (alpha) {
         ++nChannels;
      }

      this.channels = new int[nChannels];

      for(int i = 0; i < this.channels.length; this.channels[i] = i++) {
      }

      txf.deltaTransform(this.tx, 0, this.tx, 0, 1);
      txf.deltaTransform(this.ty, 0, this.ty, 0, 1);
      double[] vecX = new double[]{0.5, 0.0};
      double[] vecY = new double[]{0.0, 0.5};
      txf.deltaTransform(vecX, 0, vecX, 0, 1);
      txf.deltaTransform(vecY, 0, vecY, 0, 1);
      double dx = Math.max(Math.abs(vecX[0]), Math.abs(vecY[0]));
      int maxX = -((int)Math.round((Math.log(dx) + Math.log(baseFrequencyX)) / Math.log(2.0)));
      double dy = Math.max(Math.abs(vecX[1]), Math.abs(vecY[1]));
      int maxY = -((int)Math.round((Math.log(dy) + Math.log(baseFrequencyY)) / Math.log(2.0)));
      this.numOctaves = numOctaves > maxX ? maxX : numOctaves;
      this.numOctaves = this.numOctaves > maxY ? maxY : this.numOctaves;
      if (this.numOctaves < 1 && numOctaves > 1) {
         this.numOctaves = 1;
      }

      if (this.numOctaves > 8) {
         this.numOctaves = 8;
      }

      if (tile != null) {
         double lowFreq = Math.floor(tile.getWidth() * baseFrequencyX) / tile.getWidth();
         double highFreq = Math.ceil(tile.getWidth() * baseFrequencyX) / tile.getWidth();
         if (baseFrequencyX / lowFreq < highFreq / baseFrequencyX) {
            this.baseFrequencyX = lowFreq;
         } else {
            this.baseFrequencyX = highFreq;
         }

         lowFreq = Math.floor(tile.getHeight() * baseFrequencyY) / tile.getHeight();
         highFreq = Math.ceil(tile.getHeight() * baseFrequencyY) / tile.getHeight();
         if (baseFrequencyY / lowFreq < highFreq / baseFrequencyY) {
            this.baseFrequencyY = lowFreq;
         } else {
            this.baseFrequencyY = highFreq;
         }

         this.stitchInfo = new StitchInfo();
         this.stitchInfo.width = (int)(tile.getWidth() * this.baseFrequencyX);
         this.stitchInfo.height = (int)(tile.getHeight() * this.baseFrequencyY);
         this.stitchInfo.wrapX = (int)(tile.getX() * this.baseFrequencyX + 4096.0 + (double)this.stitchInfo.width);
         this.stitchInfo.wrapY = (int)(tile.getY() * this.baseFrequencyY + 4096.0 + (double)this.stitchInfo.height);
         if (this.stitchInfo.width == 0) {
            this.stitchInfo.width = 1;
         }

         if (this.stitchInfo.height == 0) {
            this.stitchInfo.height = 1;
         }
      }

      this.initLattice(seed);
      DirectColorModel cm;
      if (alpha) {
         cm = new DirectColorModel(cs, 32, 16711680, 65280, 255, -16777216, false, 3);
      } else {
         cm = new DirectColorModel(cs, 24, 16711680, 65280, 255, 0, false, 3);
      }

      int tileSize = AbstractTiledRed.getDefaultTileSize();
      this.init((CachableRed)null, devRect, cm, cm.createCompatibleSampleModel(tileSize, tileSize), 0, 0, (Map)null);
   }

   static final class StitchInfo {
      int width;
      int height;
      int wrapX;
      int wrapY;

      StitchInfo() {
      }

      StitchInfo(StitchInfo stitchInfo) {
         this.width = stitchInfo.width;
         this.height = stitchInfo.height;
         this.wrapX = stitchInfo.wrapX;
         this.wrapY = stitchInfo.wrapY;
      }

      final void assign(StitchInfo stitchInfo) {
         this.width = stitchInfo.width;
         this.height = stitchInfo.height;
         this.wrapX = stitchInfo.wrapX;
         this.wrapY = stitchInfo.wrapY;
      }

      final void doubleFrequency() {
         this.width *= 2;
         this.height *= 2;
         this.wrapX *= 2;
         this.wrapY *= 2;
         this.wrapX = (int)((double)this.wrapX - 4096.0);
         this.wrapY = (int)((double)this.wrapY - 4096.0);
      }
   }
}
