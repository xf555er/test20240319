package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableBooleanValue extends AnimatableValue {
   protected boolean value;

   protected AnimatableBooleanValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableBooleanValue(AnimationTarget target, boolean b) {
      super(target);
      this.value = b;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableBooleanValue res;
      if (result == null) {
         res = new AnimatableBooleanValue(this.target);
      } else {
         res = (AnimatableBooleanValue)result;
      }

      boolean newValue;
      if (to != null && (double)interpolation >= 0.5) {
         AnimatableBooleanValue toValue = (AnimatableBooleanValue)to;
         newValue = toValue.value;
      } else {
         newValue = this.value;
      }

      if (res.value != newValue) {
         res.value = newValue;
         res.hasChanged = true;
      }

      return res;
   }

   public boolean getValue() {
      return this.value;
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableBooleanValue(this.target, false);
   }

   public String getCssText() {
      return this.value ? "true" : "false";
   }
}
