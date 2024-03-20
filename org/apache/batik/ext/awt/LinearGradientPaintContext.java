package org.apache.batik.ext.awt;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

final class LinearGradientPaintContext extends MultipleGradientPaintContext {
   private float dgdX;
   private float dgdY;
   private float gc;
   private float pixSz;
   private static final int DEFAULT_IMPL = 1;
   private static final int ANTI_ALIAS_IMPL = 3;
   private int fillMethod;

   public LinearGradientPaintContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform t, RenderingHints hints, Point2D dStart, Point2D dEnd, float[] fractions, Color[] colors, MultipleGradientPaint.CycleMethodEnum cycleMethod, MultipleGradientPaint.ColorSpaceEnum colorSpace) throws NoninvertibleTransformException {
      super(cm, deviceBounds, userBounds, t, hints, fractions, colors, cycleMethod, colorSpace);
      Point2D.Float start = new Point2D.Float((float)dStart.getX(), (float)dStart.getY());
      Point2D.Float end = new Point2D.Float((float)dEnd.getX(), (float)dEnd.getY());
      float dx = end.x - start.x;
      float dy = end.y - start.y;
      float dSq = dx * dx + dy * dy;
      float constX = dx / dSq;
      float constY = dy / dSq;
      this.dgdX = this.a00 * constX + this.a10 * constY;
      this.dgdY = this.a01 * constX + this.a11 * constY;
      float dgdXAbs = Math.abs(this.dgdX);
      float dgdYAbs = Math.abs(this.dgdY);
      if (dgdXAbs > dgdYAbs) {
         this.pixSz = dgdXAbs;
      } else {
         this.pixSz = dgdYAbs;
      }

      this.gc = (this.a02 - start.x) * constX + (this.a12 - start.y) * constY;
      Object colorRend = hints.get(RenderingHints.KEY_COLOR_RENDERING);
      Object rend = hints.get(RenderingHints.KEY_RENDERING);
      this.fillMethod = 1;
      if (cycleMethod == MultipleGradientPaint.REPEAT || this.hasDiscontinuity) {
         if (rend == RenderingHints.VALUE_RENDER_QUALITY) {
            this.fillMethod = 3;
         }

         if (colorRend == RenderingHints.VALUE_COLOR_RENDER_SPEED) {
            this.fillMethod = 1;
         } else if (colorRend == RenderingHints.VALUE_COLOR_RENDER_QUALITY) {
            this.fillMethod = 3;
         }
      }

   }

   protected void fillHardNoCycle(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
      float initConst = this.dgdX * (float)x + this.gc;

      for(int i = 0; i < h; ++i) {
         float g = initConst + this.dgdY * (float)(y + i);
         int rowLimit = off + w;
         int gradSteps;
         int preGradSteps;
         if (this.dgdX == 0.0F) {
            if (g <= 0.0F) {
               gradSteps = this.gradientUnderflow;
            } else if (g >= 1.0F) {
               gradSteps = this.gradientOverflow;
            } else {
               for(preGradSteps = 0; preGradSteps < this.gradientsLength - 1 && !(g < this.fractions[preGradSteps + 1]); ++preGradSteps) {
               }

               float delta = g - this.fractions[preGradSteps];
               float idx = delta * 255.0F / this.normalizedIntervals[preGradSteps] + 0.5F;
               gradSteps = this.gradients[preGradSteps][(int)idx];
            }

            while(off < rowLimit) {
               pixels[off++] = gradSteps;
            }
         } else {
            int preVal;
            int postVal;
            float gradStepsF;
            float preGradStepsF;
            if (this.dgdX >= 0.0F) {
               gradStepsF = (1.0F - g) / this.dgdX;
               preGradStepsF = (float)Math.ceil((double)((0.0F - g) / this.dgdX));
               preVal = this.gradientUnderflow;
               postVal = this.gradientOverflow;
            } else {
               gradStepsF = (0.0F - g) / this.dgdX;
               preGradStepsF = (float)Math.ceil((double)((1.0F - g) / this.dgdX));
               preVal = this.gradientOverflow;
               postVal = this.gradientUnderflow;
            }

            if (gradStepsF > (float)w) {
               gradSteps = w;
            } else {
               gradSteps = (int)gradStepsF;
            }

            if (preGradStepsF > (float)w) {
               preGradSteps = w;
            } else {
               preGradSteps = (int)preGradStepsF;
            }

            int gradLimit = off + gradSteps;
            int gradIdx;
            if (preGradSteps > 0) {
               for(gradIdx = off + preGradSteps; off < gradIdx; pixels[off++] = preVal) {
               }

               g += this.dgdX * (float)preGradSteps;
            }

            float delta;
            int[] grad;
            double stepsD;
            int steps;
            int subGradLimit;
            int idx;
            int step;
            if (this.dgdX > 0.0F) {
               for(gradIdx = 0; gradIdx < this.gradientsLength - 1 && !(g < this.fractions[gradIdx + 1]); ++gradIdx) {
               }

               while(off < gradLimit) {
                  delta = g - this.fractions[gradIdx];
                  grad = this.gradients[gradIdx];
                  stepsD = Math.ceil((double)((this.fractions[gradIdx + 1] - g) / this.dgdX));
                  if (stepsD > (double)w) {
                     steps = w;
                  } else {
                     steps = (int)stepsD;
                  }

                  subGradLimit = off + steps;
                  if (subGradLimit > gradLimit) {
                     subGradLimit = gradLimit;
                  }

                  idx = (int)(delta * 255.0F / this.normalizedIntervals[gradIdx] * 65536.0F) + '耀';

                  for(step = (int)(this.dgdX * 255.0F / this.normalizedIntervals[gradIdx] * 65536.0F); off < subGradLimit; idx += step) {
                     pixels[off++] = grad[idx >> 16];
                  }

                  g = (float)((double)g + (double)this.dgdX * stepsD);
                  ++gradIdx;
               }
            } else {
               for(gradIdx = this.gradientsLength - 1; gradIdx > 0 && !(g > this.fractions[gradIdx]); --gradIdx) {
               }

               while(off < gradLimit) {
                  delta = g - this.fractions[gradIdx];
                  grad = this.gradients[gradIdx];
                  stepsD = Math.ceil((double)(delta / -this.dgdX));
                  if (stepsD > (double)w) {
                     steps = w;
                  } else {
                     steps = (int)stepsD;
                  }

                  subGradLimit = off + steps;
                  if (subGradLimit > gradLimit) {
                     subGradLimit = gradLimit;
                  }

                  idx = (int)(delta * 255.0F / this.normalizedIntervals[gradIdx] * 65536.0F) + '耀';

                  for(step = (int)(this.dgdX * 255.0F / this.normalizedIntervals[gradIdx] * 65536.0F); off < subGradLimit; idx += step) {
                     pixels[off++] = grad[idx >> 16];
                  }

                  g = (float)((double)g + (double)this.dgdX * stepsD);
                  --gradIdx;
               }
            }

            while(off < rowLimit) {
               pixels[off++] = postVal;
            }
         }

         off += adjust;
      }

   }

   protected void fillSimpleNoCycle(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
      float initConst = this.dgdX * (float)x + this.gc;
      float step = this.dgdX * (float)this.fastGradientArraySize;
      int fpStep = (int)(step * 65536.0F);
      int[] grad = this.gradient;

      for(int i = 0; i < h; ++i) {
         float g = initConst + this.dgdY * (float)(y + i);
         g *= (float)this.fastGradientArraySize;
         g = (float)((double)g + 0.5);
         int rowLimit = off + w;
         float check = this.dgdX * (float)this.fastGradientArraySize * (float)w;
         if (check < 0.0F) {
            check = -check;
         }

         int gradSteps;
         if ((double)check < 0.3) {
            if (g <= 0.0F) {
               gradSteps = this.gradientUnderflow;
            } else if (g >= (float)this.fastGradientArraySize) {
               gradSteps = this.gradientOverflow;
            } else {
               gradSteps = grad[(int)g];
            }

            while(off < rowLimit) {
               pixels[off++] = gradSteps;
            }
         } else {
            int preGradSteps;
            int preVal;
            int postVal;
            if (this.dgdX > 0.0F) {
               gradSteps = (int)(((float)this.fastGradientArraySize - g) / step);
               preGradSteps = (int)Math.ceil((double)(0.0F - g / step));
               preVal = this.gradientUnderflow;
               postVal = this.gradientOverflow;
            } else {
               gradSteps = (int)((0.0F - g) / step);
               preGradSteps = (int)Math.ceil((double)(((float)this.fastGradientArraySize - g) / step));
               preVal = this.gradientOverflow;
               postVal = this.gradientUnderflow;
            }

            if (gradSteps > w) {
               gradSteps = w;
            }

            int gradLimit = off + gradSteps;
            int fpG;
            if (preGradSteps > 0) {
               if (preGradSteps > w) {
                  preGradSteps = w;
               }

               for(fpG = off + preGradSteps; off < fpG; pixels[off++] = preVal) {
               }

               g += step * (float)preGradSteps;
            }

            for(fpG = (int)(g * 65536.0F); off < gradLimit; fpG += fpStep) {
               pixels[off++] = grad[fpG >> 16];
            }

            while(off < rowLimit) {
               pixels[off++] = postVal;
            }
         }

         off += adjust;
      }

   }

   protected void fillSimpleRepeat(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
      float initConst = this.dgdX * (float)x + this.gc;
      float step = (this.dgdX - (float)((int)this.dgdX)) * (float)this.fastGradientArraySize;
      if (step < 0.0F) {
         step += (float)this.fastGradientArraySize;
      }

      int[] grad = this.gradient;

      for(int i = 0; i < h; ++i) {
         float g = initConst + this.dgdY * (float)(y + i);
         g -= (float)((int)g);
         if (g < 0.0F) {
            ++g;
         }

         g *= (float)this.fastGradientArraySize;
         g = (float)((double)g + 0.5);

         for(int rowLimit = off + w; off < rowLimit; g += step) {
            int idx = (int)g;
            if (idx >= this.fastGradientArraySize) {
               g -= (float)this.fastGradientArraySize;
               idx -= this.fastGradientArraySize;
            }

            pixels[off++] = grad[idx];
         }

         off += adjust;
      }

   }

   protected void fillSimpleReflect(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
      float initConst = this.dgdX * (float)x + this.gc;
      int[] grad = this.gradient;

      for(int i = 0; i < h; ++i) {
         float g = initConst + this.dgdY * (float)(y + i);
         g -= (float)(2 * (int)(g / 2.0F));
         float step = this.dgdX;
         if (g < 0.0F) {
            g = -g;
            step = -step;
         }

         step -= 2.0F * ((float)((int)step) / 2.0F);
         if (step < 0.0F) {
            step = (float)((double)step + 2.0);
         }

         int reflectMax = 2 * this.fastGradientArraySize;
         g *= (float)this.fastGradientArraySize;
         g = (float)((double)g + 0.5);
         step *= (float)this.fastGradientArraySize;

         for(int rowLimit = off + w; off < rowLimit; g += step) {
            int idx = (int)g;
            if (idx >= reflectMax) {
               g -= (float)reflectMax;
               idx -= reflectMax;
            }

            if (idx <= this.fastGradientArraySize) {
               pixels[off++] = grad[idx];
            } else {
               pixels[off++] = grad[reflectMax - idx];
            }
         }

         off += adjust;
      }

   }

   protected void fillRaster(int[] pixels, int off, int adjust, int x, int y, int w, int h) {
      float initConst = this.dgdX * (float)x + this.gc;
      int i;
      float g;
      int rowLimit;
      if (this.fillMethod == 3) {
         for(i = 0; i < h; ++i) {
            g = initConst + this.dgdY * (float)(y + i);

            for(rowLimit = off + w; off < rowLimit; g += this.dgdX) {
               pixels[off++] = this.indexGradientAntiAlias(g, this.pixSz);
            }

            off += adjust;
         }
      } else if (!this.isSimpleLookup) {
         if (this.cycleMethod == MultipleGradientPaint.NO_CYCLE) {
            this.fillHardNoCycle(pixels, off, adjust, x, y, w, h);
         } else {
            for(i = 0; i < h; ++i) {
               g = initConst + this.dgdY * (float)(y + i);

               for(rowLimit = off + w; off < rowLimit; g += this.dgdX) {
                  pixels[off++] = this.indexIntoGradientsArrays(g);
               }

               off += adjust;
            }
         }
      } else if (this.cycleMethod == MultipleGradientPaint.NO_CYCLE) {
         this.fillSimpleNoCycle(pixels, off, adjust, x, y, w, h);
      } else if (this.cycleMethod == MultipleGradientPaint.REPEAT) {
         this.fillSimpleRepeat(pixels, off, adjust, x, y, w, h);
      } else {
         this.fillSimpleReflect(pixels, off, adjust, x, y, w, h);
      }

   }
}
