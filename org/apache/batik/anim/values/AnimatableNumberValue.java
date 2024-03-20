package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableNumberValue extends AnimatableValue {
   protected float value;

   protected AnimatableNumberValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableNumberValue(AnimationTarget target, float v) {
      super(target);
      this.value = v;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableNumberValue res;
      if (result == null) {
         res = new AnimatableNumberValue(this.target);
      } else {
         res = (AnimatableNumberValue)result;
      }

      float v = this.value;
      AnimatableNumberValue accNumber;
      if (to != null) {
         accNumber = (AnimatableNumberValue)to;
         v += interpolation * (accNumber.value - this.value);
      }

      if (accumulation != null) {
         accNumber = (AnimatableNumberValue)accumulation;
         v += (float)multiplier * accNumber.value;
      }

      if (res.value != v) {
         res.value = v;
         res.hasChanged = true;
      }

      return res;
   }

   public float getValue() {
      return this.value;
   }

   public boolean canPace() {
      return true;
   }

   public float distanceTo(AnimatableValue other) {
      AnimatableNumberValue o = (AnimatableNumberValue)other;
      return Math.abs(this.value - o.value);
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableNumberValue(this.target, 0.0F);
   }

   public String getCssText() {
      return formatNumber(this.value);
   }
}
