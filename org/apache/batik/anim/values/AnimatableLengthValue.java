package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableLengthValue extends AnimatableValue {
   protected static final String[] UNITS = new String[]{"", "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc"};
   protected short lengthType;
   protected float lengthValue;
   protected short percentageInterpretation;

   protected AnimatableLengthValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableLengthValue(AnimationTarget target, short type, float v, short pcInterp) {
      super(target);
      this.lengthType = type;
      this.lengthValue = v;
      this.percentageInterpretation = pcInterp;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableLengthValue res;
      if (result == null) {
         res = new AnimatableLengthValue(this.target);
      } else {
         res = (AnimatableLengthValue)result;
      }

      short oldLengthType = res.lengthType;
      float oldLengthValue = res.lengthValue;
      short oldPercentageInterpretation = res.percentageInterpretation;
      res.lengthType = this.lengthType;
      res.lengthValue = this.lengthValue;
      res.percentageInterpretation = this.percentageInterpretation;
      AnimatableLengthValue accLength;
      float accValue;
      if (to != null) {
         accLength = (AnimatableLengthValue)to;
         if (!compatibleTypes(res.lengthType, res.percentageInterpretation, accLength.lengthType, accLength.percentageInterpretation)) {
            res.lengthValue = this.target.svgToUserSpace(res.lengthValue, res.lengthType, res.percentageInterpretation);
            res.lengthType = 1;
            accValue = accLength.target.svgToUserSpace(accLength.lengthValue, accLength.lengthType, accLength.percentageInterpretation);
         } else {
            accValue = accLength.lengthValue;
         }

         res.lengthValue += interpolation * (accValue - res.lengthValue);
      }

      if (accumulation != null) {
         accLength = (AnimatableLengthValue)accumulation;
         if (!compatibleTypes(res.lengthType, res.percentageInterpretation, accLength.lengthType, accLength.percentageInterpretation)) {
            res.lengthValue = this.target.svgToUserSpace(res.lengthValue, res.lengthType, res.percentageInterpretation);
            res.lengthType = 1;
            accValue = accLength.target.svgToUserSpace(accLength.lengthValue, accLength.lengthType, accLength.percentageInterpretation);
         } else {
            accValue = accLength.lengthValue;
         }

         res.lengthValue += (float)multiplier * accValue;
      }

      if (oldPercentageInterpretation != res.percentageInterpretation || oldLengthType != res.lengthType || oldLengthValue != res.lengthValue) {
         res.hasChanged = true;
      }

      return res;
   }

   public static boolean compatibleTypes(short t1, short pi1, short t2, short pi2) {
      return t1 == t2 && (t1 != 2 || pi1 == pi2) || t1 == 1 && t2 == 5 || t1 == 5 && t2 == 1;
   }

   public int getLengthType() {
      return this.lengthType;
   }

   public float getLengthValue() {
      return this.lengthValue;
   }

   public boolean canPace() {
      return true;
   }

   public float distanceTo(AnimatableValue other) {
      AnimatableLengthValue o = (AnimatableLengthValue)other;
      float v1 = this.target.svgToUserSpace(this.lengthValue, this.lengthType, this.percentageInterpretation);
      float v2 = this.target.svgToUserSpace(o.lengthValue, o.lengthType, o.percentageInterpretation);
      return Math.abs(v1 - v2);
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableLengthValue(this.target, (short)1, 0.0F, this.percentageInterpretation);
   }

   public String getCssText() {
      return formatNumber(this.lengthValue) + UNITS[this.lengthType - 1];
   }
}
