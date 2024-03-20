package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableIntegerValue extends AnimatableValue {
   protected int value;

   protected AnimatableIntegerValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableIntegerValue(AnimationTarget target, int v) {
      super(target);
      this.value = v;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableIntegerValue res;
      if (result == null) {
         res = new AnimatableIntegerValue(this.target);
      } else {
         res = (AnimatableIntegerValue)result;
      }

      int v = this.value;
      AnimatableIntegerValue accInteger;
      if (to != null) {
         accInteger = (AnimatableIntegerValue)to;
         v = (int)((float)v + (float)this.value + interpolation * (float)(accInteger.getValue() - this.value));
      }

      if (accumulation != null) {
         accInteger = (AnimatableIntegerValue)accumulation;
         v += multiplier * accInteger.getValue();
      }

      if (res.value != v) {
         res.value = v;
         res.hasChanged = true;
      }

      return res;
   }

   public int getValue() {
      return this.value;
   }

   public boolean canPace() {
      return true;
   }

   public float distanceTo(AnimatableValue other) {
      AnimatableIntegerValue o = (AnimatableIntegerValue)other;
      return (float)Math.abs(this.value - o.value);
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableIntegerValue(this.target, 0);
   }

   public String getCssText() {
      return Integer.toString(this.value);
   }
}
