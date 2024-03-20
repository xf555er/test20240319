package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableAngleValue extends AnimatableNumberValue {
   protected static final String[] UNITS = new String[]{"", "", "deg", "rad", "grad"};
   protected short unit;

   public AnimatableAngleValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableAngleValue(AnimationTarget target, float v, short unit) {
      super(target, v);
      this.unit = unit;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableAngleValue res;
      if (result == null) {
         res = new AnimatableAngleValue(this.target);
      } else {
         res = (AnimatableAngleValue)result;
      }

      float v = this.value;
      short u = this.unit;
      AnimatableAngleValue accAngle;
      if (to != null) {
         accAngle = (AnimatableAngleValue)to;
         if (accAngle.unit != u) {
            v = rad(v, u);
            v += interpolation * (rad(accAngle.value, accAngle.unit) - v);
            u = 3;
         } else {
            v += interpolation * (accAngle.value - v);
         }
      }

      if (accumulation != null) {
         accAngle = (AnimatableAngleValue)accumulation;
         if (accAngle.unit != u) {
            v += (float)multiplier * rad(accAngle.value, accAngle.unit);
            u = 3;
         } else {
            v += (float)multiplier * accAngle.value;
         }
      }

      if (res.value != v || res.unit != u) {
         res.value = v;
         res.unit = u;
         res.hasChanged = true;
      }

      return res;
   }

   public short getUnit() {
      return this.unit;
   }

   public float distanceTo(AnimatableValue other) {
      AnimatableAngleValue o = (AnimatableAngleValue)other;
      return Math.abs(rad(this.value, this.unit) - rad(o.value, o.unit));
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableAngleValue(this.target, 0.0F, (short)1);
   }

   public String getCssText() {
      return super.getCssText() + UNITS[this.unit];
   }

   public static float rad(float v, short unit) {
      switch (unit) {
         case 3:
            return v;
         case 4:
            return 3.1415927F * v / 200.0F;
         default:
            return 3.1415927F * v / 180.0F;
      }
   }
}
