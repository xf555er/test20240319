package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableStringValue extends AnimatableValue {
   protected String string;

   protected AnimatableStringValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableStringValue(AnimationTarget target, String s) {
      super(target);
      this.string = s;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableStringValue res;
      if (result == null) {
         res = new AnimatableStringValue(this.target);
      } else {
         res = (AnimatableStringValue)result;
      }

      String newString;
      if (to != null && (double)interpolation >= 0.5) {
         AnimatableStringValue toValue = (AnimatableStringValue)to;
         newString = toValue.string;
      } else {
         newString = this.string;
      }

      if (res.string == null || !res.string.equals(newString)) {
         res.string = newString;
         res.hasChanged = true;
      }

      return res;
   }

   public String getString() {
      return this.string;
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableStringValue(this.target, "");
   }

   public String getCssText() {
      return this.string;
   }
}
