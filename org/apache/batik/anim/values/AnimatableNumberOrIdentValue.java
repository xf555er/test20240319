package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableNumberOrIdentValue extends AnimatableNumberValue {
   protected boolean isIdent;
   protected String ident;
   protected boolean numericIdent;

   protected AnimatableNumberOrIdentValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableNumberOrIdentValue(AnimationTarget target, float v, boolean numericIdent) {
      super(target, v);
      this.numericIdent = numericIdent;
   }

   public AnimatableNumberOrIdentValue(AnimationTarget target, String ident) {
      super(target);
      this.ident = ident;
      this.isIdent = true;
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableNumberOrIdentValue(this.target, 0.0F, this.numericIdent);
   }

   public String getCssText() {
      if (this.isIdent) {
         return this.ident;
      } else {
         return this.numericIdent ? Integer.toString((int)this.value) : super.getCssText();
      }
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableNumberOrIdentValue res;
      if (result == null) {
         res = new AnimatableNumberOrIdentValue(this.target);
      } else {
         res = (AnimatableNumberOrIdentValue)result;
      }

      if (to == null) {
         if (this.isIdent) {
            res.hasChanged = !res.isIdent || !res.ident.equals(this.ident);
            res.ident = this.ident;
            res.isIdent = true;
         } else if (this.numericIdent) {
            res.hasChanged = res.value != this.value || res.isIdent;
            res.value = this.value;
            res.isIdent = false;
            res.hasChanged = true;
            res.numericIdent = true;
         } else {
            float oldValue = res.value;
            super.interpolate(res, to, interpolation, accumulation, multiplier);
            res.numericIdent = false;
            if (res.value != oldValue) {
               res.hasChanged = true;
            }
         }
      } else {
         AnimatableNumberOrIdentValue toValue = (AnimatableNumberOrIdentValue)to;
         if (!this.isIdent && !toValue.isIdent && !this.numericIdent) {
            super.interpolate(res, to, interpolation, accumulation, multiplier);
            res.numericIdent = false;
         } else if ((double)interpolation >= 0.5) {
            if (res.isIdent != toValue.isIdent || res.value != toValue.value || res.isIdent && toValue.isIdent && !toValue.ident.equals(this.ident)) {
               res.isIdent = toValue.isIdent;
               res.ident = toValue.ident;
               res.value = toValue.value;
               res.numericIdent = toValue.numericIdent;
               res.hasChanged = true;
            }
         } else if (res.isIdent != this.isIdent || res.value != this.value || res.isIdent && this.isIdent && !res.ident.equals(this.ident)) {
            res.isIdent = this.isIdent;
            res.ident = this.ident;
            res.value = this.value;
            res.numericIdent = this.numericIdent;
            res.hasChanged = true;
         }
      }

      return res;
   }
}
