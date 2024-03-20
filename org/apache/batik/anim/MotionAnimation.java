package org.apache.batik.anim;

import java.awt.geom.Point2D;
import org.apache.batik.anim.dom.AnimatableElement;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableAngleValue;
import org.apache.batik.anim.values.AnimatableMotionPointValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.ext.awt.geom.Cubic;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.ext.awt.geom.PathLength;

public class MotionAnimation extends InterpolatingAnimation {
   protected ExtendedGeneralPath path;
   protected PathLength pathLength;
   protected float[] keyPoints;
   protected boolean rotateAuto;
   protected boolean rotateAutoReverse;
   protected float rotateAngle;

   public MotionAnimation(TimedElement timedElement, AnimatableElement animatableElement, int calcMode, float[] keyTimes, float[] keySplines, boolean additive, boolean cumulative, AnimatableValue[] values, AnimatableValue from, AnimatableValue to, AnimatableValue by, ExtendedGeneralPath path, float[] keyPoints, boolean rotateAuto, boolean rotateAutoReverse, float rotateAngle, short rotateAngleUnit) {
      super(timedElement, animatableElement, calcMode, keyTimes, keySplines, additive, cumulative);
      this.rotateAuto = rotateAuto;
      this.rotateAutoReverse = rotateAutoReverse;
      this.rotateAngle = AnimatableAngleValue.rad(rotateAngle, rotateAngleUnit);
      if (path == null) {
         path = new ExtendedGeneralPath();
         AnimatableMotionPointValue byPt;
         if (values != null && values.length != 0) {
            byPt = (AnimatableMotionPointValue)values[0];
            path.moveTo(byPt.getX(), byPt.getY());

            for(int i = 1; i < values.length; ++i) {
               byPt = (AnimatableMotionPointValue)values[i];
               path.lineTo(byPt.getX(), byPt.getY());
            }
         } else if (from != null) {
            byPt = (AnimatableMotionPointValue)from;
            float x = byPt.getX();
            float y = byPt.getY();
            path.moveTo(x, y);
            AnimatableMotionPointValue byPt;
            if (to != null) {
               byPt = (AnimatableMotionPointValue)to;
               path.lineTo(byPt.getX(), byPt.getY());
            } else {
               if (by == null) {
                  throw timedElement.createException("values.to.by.path.missing", new Object[]{null});
               }

               byPt = (AnimatableMotionPointValue)by;
               path.lineTo(x + byPt.getX(), y + byPt.getY());
            }
         } else if (to != null) {
            byPt = (AnimatableMotionPointValue)animatableElement.getUnderlyingValue();
            AnimatableMotionPointValue toPt = (AnimatableMotionPointValue)to;
            path.moveTo(byPt.getX(), byPt.getY());
            path.lineTo(toPt.getX(), toPt.getY());
            this.cumulative = false;
         } else {
            if (by == null) {
               throw timedElement.createException("values.to.by.path.missing", new Object[]{null});
            }

            byPt = (AnimatableMotionPointValue)by;
            path.moveTo(0.0F, 0.0F);
            path.lineTo(byPt.getX(), byPt.getY());
            this.additive = true;
         }
      }

      this.path = path;
      this.pathLength = new PathLength(path);
      int segments = 0;

      ExtendedPathIterator epi;
      int count;
      for(epi = path.getExtendedPathIterator(); !epi.isDone(); epi.next()) {
         count = epi.currentSegment();
         if (count != 0) {
            ++segments;
         }
      }

      count = keyPoints == null ? segments + 1 : keyPoints.length;
      float totalLength = this.pathLength.lengthOfPath();
      int j;
      int i;
      if (this.keyTimes != null && calcMode != 2) {
         if (this.keyTimes.length != count) {
            throw timedElement.createException("attribute.malformed", new Object[]{null, "keyTimes"});
         }
      } else if (calcMode != 1 && calcMode != 3) {
         if (calcMode == 0) {
            this.keyTimes = new float[count];

            for(j = 0; j < count; ++j) {
               this.keyTimes[j] = (float)j / (float)count;
            }
         } else {
            epi = path.getExtendedPathIterator();
            this.keyTimes = new float[count];
            j = 0;

            for(i = 0; i < count - 1; ++i) {
               while(epi.currentSegment() == 0) {
                  ++j;
                  epi.next();
               }

               this.keyTimes[i] = this.pathLength.getLengthAtSegment(j) / totalLength;
               ++j;
               epi.next();
            }

            this.keyTimes[count - 1] = 1.0F;
         }
      } else {
         this.keyTimes = new float[count];

         for(j = 0; j < count; ++j) {
            this.keyTimes[j] = (float)j / (float)(count - 1);
         }
      }

      if (keyPoints != null) {
         if (keyPoints.length != this.keyTimes.length) {
            throw timedElement.createException("attribute.malformed", new Object[]{null, "keyPoints"});
         }
      } else {
         epi = path.getExtendedPathIterator();
         keyPoints = new float[count];
         j = 0;

         for(i = 0; i < count - 1; ++i) {
            while(epi.currentSegment() == 0) {
               ++j;
               epi.next();
            }

            keyPoints[i] = this.pathLength.getLengthAtSegment(j) / totalLength;
            ++j;
            epi.next();
         }

         keyPoints[count - 1] = 1.0F;
      }

      this.keyPoints = keyPoints;
   }

   protected void sampledAtUnitTime(float unitTime, int repeatIteration) {
      float interpolation = 0.0F;
      AnimatableMotionPointValue value;
      Point2D p;
      float ang;
      if (unitTime != 1.0F) {
         int keyTimeIndex;
         for(keyTimeIndex = 0; keyTimeIndex < this.keyTimes.length - 1 && unitTime >= this.keyTimes[keyTimeIndex + 1]; ++keyTimeIndex) {
         }

         float min;
         if (keyTimeIndex == this.keyTimes.length - 1 && this.calcMode == 0) {
            keyTimeIndex = this.keyTimes.length - 2;
            interpolation = 1.0F;
         } else if (this.calcMode == 1 || this.calcMode == 2 || this.calcMode == 3) {
            if (unitTime == 0.0F) {
               interpolation = 0.0F;
            } else {
               interpolation = (unitTime - this.keyTimes[keyTimeIndex]) / (this.keyTimes[keyTimeIndex + 1] - this.keyTimes[keyTimeIndex]);
            }

            if (this.calcMode == 3 && unitTime != 0.0F) {
               Cubic c = this.keySplineCubics[keyTimeIndex];
               float tolerance = 0.001F;
               min = 0.0F;
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

         ang = this.keyPoints[keyTimeIndex];
         if (interpolation != 0.0F) {
            ang += interpolation * (this.keyPoints[keyTimeIndex + 1] - this.keyPoints[keyTimeIndex]);
         }

         ang *= this.pathLength.lengthOfPath();
         Point2D p = this.pathLength.pointAtLength(ang);
         if (this.rotateAuto) {
            min = this.pathLength.angleAtLength(ang);
            if (this.rotateAutoReverse) {
               min = (float)((double)min + Math.PI);
            }
         } else {
            min = this.rotateAngle;
         }

         value = new AnimatableMotionPointValue((AnimationTarget)null, (float)p.getX(), (float)p.getY(), min);
      } else {
         p = this.pathLength.pointAtLength(this.pathLength.lengthOfPath());
         if (this.rotateAuto) {
            ang = this.pathLength.angleAtLength(this.pathLength.lengthOfPath());
            if (this.rotateAutoReverse) {
               ang = (float)((double)ang + Math.PI);
            }
         } else {
            ang = this.rotateAngle;
         }

         value = new AnimatableMotionPointValue((AnimationTarget)null, (float)p.getX(), (float)p.getY(), ang);
      }

      AnimatableMotionPointValue accumulation;
      if (this.cumulative) {
         p = this.pathLength.pointAtLength(this.pathLength.lengthOfPath());
         if (this.rotateAuto) {
            ang = this.pathLength.angleAtLength(this.pathLength.lengthOfPath());
            if (this.rotateAutoReverse) {
               ang = (float)((double)ang + Math.PI);
            }
         } else {
            ang = this.rotateAngle;
         }

         accumulation = new AnimatableMotionPointValue((AnimationTarget)null, (float)p.getX(), (float)p.getY(), ang);
      } else {
         accumulation = null;
      }

      this.value = value.interpolate(this.value, (AnimatableValue)null, interpolation, accumulation, repeatIteration);
      if (this.value.hasChanged()) {
         this.markDirty();
      }

   }
}
