package org.apache.batik.bridge;

import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.ColorAnimation;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.values.AnimatableColorValue;
import org.apache.batik.anim.values.AnimatablePaintValue;
import org.apache.batik.anim.values.AnimatableValue;

public class SVGAnimateColorElementBridge extends SVGAnimateElementBridge {
   public String getLocalName() {
      return "animateColor";
   }

   public Bridge getInstance() {
      return new SVGAnimateColorElementBridge();
   }

   protected AbstractAnimation createAnimation(AnimationTarget target) {
      AnimatableValue from = this.parseAnimatableValue("from");
      AnimatableValue to = this.parseAnimatableValue("to");
      AnimatableValue by = this.parseAnimatableValue("by");
      return new ColorAnimation(this.timedElement, this, this.parseCalcMode(), this.parseKeyTimes(), this.parseKeySplines(), this.parseAdditive(), this.parseAccumulate(), this.parseValues(), from, to, by);
   }

   protected boolean canAnimateType(int type) {
      return type == 6 || type == 7;
   }

   protected boolean checkValueType(AnimatableValue v) {
      if (v instanceof AnimatablePaintValue) {
         return ((AnimatablePaintValue)v).getPaintType() == 2;
      } else {
         return v instanceof AnimatableColorValue;
      }
   }
}
