package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableMotionPointValue extends AnimatableValue {
   protected float x;
   protected float y;
   protected float angle;

   protected AnimatableMotionPointValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableMotionPointValue(AnimationTarget target, float x, float y, float angle) {
      super(target);
      this.x = x;
      this.y = y;
      this.angle = angle;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableMotionPointValue res;
      if (result == null) {
         res = new AnimatableMotionPointValue(this.target);
      } else {
         res = (AnimatableMotionPointValue)result;
      }

      float newX = this.x;
      float newY = this.y;
      float newAngle = this.angle;
      int angleCount = 1;
      AnimatableMotionPointValue accValue;
      if (to != null) {
         accValue = (AnimatableMotionPointValue)to;
         newX += interpolation * (accValue.x - this.x);
         newY += interpolation * (accValue.y - this.y);
         newAngle += accValue.angle;
         ++angleCount;
      }

      if (accumulation != null && multiplier != 0) {
         accValue = (AnimatableMotionPointValue)accumulation;
         newX += (float)multiplier * accValue.x;
         newY += (float)multiplier * accValue.y;
         newAngle += accValue.angle;
         ++angleCount;
      }

      newAngle /= (float)angleCount;
      if (res.x != newX || res.y != newY || res.angle != newAngle) {
         res.x = newX;
         res.y = newY;
         res.angle = newAngle;
         res.hasChanged = true;
      }

      return res;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getAngle() {
      return this.angle;
   }

   public boolean canPace() {
      return true;
   }

   public float distanceTo(AnimatableValue other) {
      AnimatableMotionPointValue o = (AnimatableMotionPointValue)other;
      float dx = this.x - o.x;
      float dy = this.y - o.y;
      return (float)Math.sqrt((double)(dx * dx + dy * dy));
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableMotionPointValue(this.target, 0.0F, 0.0F, 0.0F);
   }

   public String toStringRep() {
      StringBuffer sb = new StringBuffer();
      sb.append(formatNumber(this.x));
      sb.append(',');
      sb.append(formatNumber(this.y));
      sb.append(',');
      sb.append(formatNumber(this.angle));
      sb.append("rad");
      return sb.toString();
   }
}
