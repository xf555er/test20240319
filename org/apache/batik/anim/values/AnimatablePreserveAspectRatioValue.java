package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatablePreserveAspectRatioValue extends AnimatableValue {
   protected static final String[] ALIGN_VALUES = new String[]{null, "none", "xMinYMin", "xMidYMin", "xMaxYMin", "xMinYMid", "xMidYMid", "xMaxYMid", "xMinYMax", "xMidYMax", "xMaxYMax"};
   protected static final String[] MEET_OR_SLICE_VALUES = new String[]{null, "meet", "slice"};
   protected short align;
   protected short meetOrSlice;

   protected AnimatablePreserveAspectRatioValue(AnimationTarget target) {
      super(target);
   }

   public AnimatablePreserveAspectRatioValue(AnimationTarget target, short align, short meetOrSlice) {
      super(target);
      this.align = align;
      this.meetOrSlice = meetOrSlice;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatablePreserveAspectRatioValue res;
      if (result == null) {
         res = new AnimatablePreserveAspectRatioValue(this.target);
      } else {
         res = (AnimatablePreserveAspectRatioValue)result;
      }

      short newAlign;
      short newMeetOrSlice;
      if (to != null && (double)interpolation >= 0.5) {
         AnimatablePreserveAspectRatioValue toValue = (AnimatablePreserveAspectRatioValue)to;
         newAlign = toValue.align;
         newMeetOrSlice = toValue.meetOrSlice;
      } else {
         newAlign = this.align;
         newMeetOrSlice = this.meetOrSlice;
      }

      if (res.align != newAlign || res.meetOrSlice != newMeetOrSlice) {
         res.align = this.align;
         res.meetOrSlice = this.meetOrSlice;
         res.hasChanged = true;
      }

      return res;
   }

   public short getAlign() {
      return this.align;
   }

   public short getMeetOrSlice() {
      return this.meetOrSlice;
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      return new AnimatablePreserveAspectRatioValue(this.target, (short)1, (short)1);
   }

   public String toStringRep() {
      if (this.align >= 1 && this.align <= 10) {
         String value = ALIGN_VALUES[this.align];
         if (this.align == 1) {
            return value;
         } else {
            return this.meetOrSlice >= 1 && this.meetOrSlice <= 2 ? value + ' ' + MEET_OR_SLICE_VALUES[this.meetOrSlice] : null;
         }
      } else {
         return null;
      }
   }
}
