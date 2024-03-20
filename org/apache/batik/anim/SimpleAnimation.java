package org.apache.batik.anim;

import java.awt.geom.Point2D;
import org.apache.batik.anim.dom.AnimatableElement;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.ext.awt.geom.Cubic;

public class SimpleAnimation extends InterpolatingAnimation {
   protected AnimatableValue[] values;
   protected AnimatableValue from;
   protected AnimatableValue to;
   protected AnimatableValue by;

   public SimpleAnimation(TimedElement timedElement, AnimatableElement animatableElement, int calcMode, float[] keyTimes, float[] keySplines, boolean additive, boolean cumulative, AnimatableValue[] values, AnimatableValue from, AnimatableValue to, AnimatableValue by) {
      super(timedElement, animatableElement, calcMode, keyTimes, keySplines, additive, cumulative);
      this.from = from;
      this.to = to;
      this.by = by;
      if (values == null) {
         if (from != null) {
            values = new AnimatableValue[]{from, null};
            if (to != null) {
               values[1] = to;
            } else {
               if (by == null) {
                  throw timedElement.createException("values.to.by.missing", new Object[]{null});
               }

               values[1] = from.interpolate((AnimatableValue)null, (AnimatableValue)null, 0.0F, by, 1);
            }
         } else if (to != null) {
            values = new AnimatableValue[]{animatableElement.getUnderlyingValue(), to};
            this.cumulative = false;
            this.toAnimation = true;
         } else {
            if (by == null) {
               throw timedElement.createException("values.to.by.missing", new Object[]{null});
            }

            this.additive = true;
            values = new AnimatableValue[]{by.getZeroValue(), by};
         }
      }

      this.values = values;
      if (this.keyTimes != null && calcMode != 2) {
         if (this.keyTimes.length != values.length) {
            throw timedElement.createException("attribute.malformed", new Object[]{null, "keyTimes"});
         }
      } else {
         int count;
         int i;
         if (calcMode != 1 && calcMode != 3 && (calcMode != 2 || values[0].canPace())) {
            if (calcMode == 0) {
               count = values.length;
               this.keyTimes = new float[count];

               for(i = 0; i < count; ++i) {
                  this.keyTimes[i] = (float)i / (float)count;
               }
            } else {
               count = values.length;
               float[] cumulativeDistances = new float[count];
               cumulativeDistances[0] = 0.0F;

               for(int i = 1; i < count; ++i) {
                  cumulativeDistances[i] = cumulativeDistances[i - 1] + values[i - 1].distanceTo(values[i]);
               }

               float totalLength = cumulativeDistances[count - 1];
               this.keyTimes = new float[count];
               this.keyTimes[0] = 0.0F;

               for(int i = 1; i < count - 1; ++i) {
                  this.keyTimes[i] = cumulativeDistances[i] / totalLength;
               }

               this.keyTimes[count - 1] = 1.0F;
            }
         } else {
            count = values.length == 1 ? 2 : values.length;
            this.keyTimes = new float[count];

            for(i = 0; i < count; ++i) {
               this.keyTimes[i] = (float)i / (float)(count - 1);
            }
         }
      }

      if (calcMode == 3 && keySplines.length != (this.keyTimes.length - 1) * 4) {
         throw timedElement.createException("attribute.malformed", new Object[]{null, "keySplines"});
      }
   }

   protected void sampledAtUnitTime(float unitTime, int repeatIteration) {
      float interpolation = 0.0F;
      AnimatableValue value;
      AnimatableValue nextValue;
      if (unitTime != 1.0F) {
         int keyTimeIndex;
         for(keyTimeIndex = 0; keyTimeIndex < this.keyTimes.length - 1 && unitTime >= this.keyTimes[keyTimeIndex + 1]; ++keyTimeIndex) {
         }

         value = this.values[keyTimeIndex];
         if (this.calcMode != 1 && this.calcMode != 2 && this.calcMode != 3) {
            nextValue = null;
         } else {
            nextValue = this.values[keyTimeIndex + 1];
            interpolation = (unitTime - this.keyTimes[keyTimeIndex]) / (this.keyTimes[keyTimeIndex + 1] - this.keyTimes[keyTimeIndex]);
            if (this.calcMode == 3 && unitTime != 0.0F) {
               Cubic c = this.keySplineCubics[keyTimeIndex];
               float tolerance = 0.001F;
               float min = 0.0F;
               float max = 1.0F;

               while(true) {
                  float t = (min + max) / 2.0F;
                  Point2D.Double p = c.eval((double)t);
                  double x = p.getX();
                  if (Math.abs(x - (double)interpolation) < (double)tolerance) {
                     interpolation = (float)p.getY();
                     break;
                  }

                  if (x < (double)interpolation) {
                     min = t;
                  } else {
                     max = t;
                  }
               }
            }
         }
      } else {
         value = this.values[this.values.length - 1];
         nextValue = null;
      }

      AnimatableValue accumulation;
      if (this.cumulative) {
         accumulation = this.values[this.values.length - 1];
      } else {
         accumulation = null;
      }

      this.value = value.interpolate(this.value, nextValue, interpolation, accumulation, repeatIteration);
      if (this.value.hasChanged()) {
         this.markDirty();
      }

   }
}
