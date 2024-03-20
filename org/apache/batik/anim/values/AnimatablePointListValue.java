package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatablePointListValue extends AnimatableNumberListValue {
   protected AnimatablePointListValue(AnimationTarget target) {
      super(target);
   }

   public AnimatablePointListValue(AnimationTarget target, float[] numbers) {
      super(target, numbers);
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      if (result == null) {
         result = new AnimatablePointListValue(this.target);
      }

      return super.interpolate((AnimatableValue)result, to, interpolation, accumulation, multiplier);
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      float[] ns = new float[this.numbers.length];
      return new AnimatablePointListValue(this.target, ns);
   }
}
