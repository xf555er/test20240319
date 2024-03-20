package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableNumberOrPercentageValue extends AnimatableNumberValue {
   protected boolean isPercentage;

   protected AnimatableNumberOrPercentageValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableNumberOrPercentageValue(AnimationTarget target, float n) {
      super(target, n);
   }

   public AnimatableNumberOrPercentageValue(AnimationTarget target, float n, boolean isPercentage) {
      super(target, n);
      this.isPercentage = isPercentage;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableNumberOrPercentageValue res;
      if (result == null) {
         res = new AnimatableNumberOrPercentageValue(this.target);
      } else {
         res = (AnimatableNumberOrPercentageValue)result;
      }

      AnimatableNumberOrPercentageValue toValue = (AnimatableNumberOrPercentageValue)to;
      AnimatableNumberOrPercentageValue accValue = (AnimatableNumberOrPercentageValue)accumulation;
      float newValue;
      boolean newIsPercentage;
      if (to != null) {
         if (toValue.isPercentage == this.isPercentage) {
            newValue = this.value + interpolation * (toValue.value - this.value);
            newIsPercentage = this.isPercentage;
         } else if ((double)interpolation >= 0.5) {
            newValue = toValue.value;
            newIsPercentage = toValue.isPercentage;
         } else {
            newValue = this.value;
            newIsPercentage = this.isPercentage;
         }
      } else {
         newValue = this.value;
         newIsPercentage = this.isPercentage;
      }

      if (accumulation != null && accValue.isPercentage == newIsPercentage) {
         newValue += (float)multiplier * accValue.value;
      }

      if (res.value != newValue || res.isPercentage != newIsPercentage) {
         res.value = newValue;
         res.isPercentage = newIsPercentage;
         res.hasChanged = true;
      }

      return res;
   }

   public boolean isPercentage() {
      return this.isPercentage;
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableNumberOrPercentageValue(this.target, 0.0F, this.isPercentage);
   }

   public String getCssText() {
      StringBuffer sb = new StringBuffer();
      sb.append(formatNumber(this.value));
      if (this.isPercentage) {
         sb.append('%');
      }

      return sb.toString();
   }
}
