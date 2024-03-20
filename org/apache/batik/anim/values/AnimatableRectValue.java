package org.apache.batik.anim.values;

import org.apache.batik.anim.dom.AnimationTarget;

public class AnimatableRectValue extends AnimatableValue {
   protected float x;
   protected float y;
   protected float width;
   protected float height;

   protected AnimatableRectValue(AnimationTarget target) {
      super(target);
   }

   public AnimatableRectValue(AnimationTarget target, float x, float y, float w, float h) {
      super(target);
      this.x = x;
      this.y = y;
      this.width = w;
      this.height = h;
   }

   public AnimatableValue interpolate(AnimatableValue result, AnimatableValue to, float interpolation, AnimatableValue accumulation, int multiplier) {
      AnimatableRectValue res;
      if (result == null) {
         res = new AnimatableRectValue(this.target);
      } else {
         res = (AnimatableRectValue)result;
      }

      float newX = this.x;
      float newY = this.y;
      float newWidth = this.width;
      float newHeight = this.height;
      AnimatableRectValue accValue;
      if (to != null) {
         accValue = (AnimatableRectValue)to;
         newX += interpolation * (accValue.x - this.x);
         newY += interpolation * (accValue.y - this.y);
         newWidth += interpolation * (accValue.width - this.width);
         newHeight += interpolation * (accValue.height - this.height);
      }

      if (accumulation != null && multiplier != 0) {
         accValue = (AnimatableRectValue)accumulation;
         newX += (float)multiplier * accValue.x;
         newY += (float)multiplier * accValue.y;
         newWidth += (float)multiplier * accValue.width;
         newHeight += (float)multiplier * accValue.height;
      }

      if (res.x != newX || res.y != newY || res.width != newWidth || res.height != newHeight) {
         res.x = newX;
         res.y = newY;
         res.width = newWidth;
         res.height = newHeight;
         res.hasChanged = true;
      }

      return res;
   }

   public float getX() {
      return this.x;
   }

   public float getY() {
      return this.y;
   }

   public float getWidth() {
      return this.width;
   }

   public float getHeight() {
      return this.height;
   }

   public boolean canPace() {
      return false;
   }

   public float distanceTo(AnimatableValue other) {
      return 0.0F;
   }

   public AnimatableValue getZeroValue() {
      return new AnimatableRectValue(this.target, 0.0F, 0.0F, 0.0F, 0.0F);
   }

   public String toStringRep() {
      StringBuffer sb = new StringBuffer();
      sb.append(this.x);
      sb.append(',');
      sb.append(this.y);
      sb.append(',');
      sb.append(this.width);
      sb.append(',');
      sb.append(this.height);
      return sb.toString();
   }
}
