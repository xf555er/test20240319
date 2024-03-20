package org.apache.batik.bridge;

import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.anim.SetAnimation;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.values.AnimatableValue;

public class SVGSetElementBridge extends SVGAnimationElementBridge {
   public String getLocalName() {
      return "set";
   }

   public Bridge getInstance() {
      return new SVGSetElementBridge();
   }

   protected AbstractAnimation createAnimation(AnimationTarget target) {
      AnimatableValue to = this.parseAnimatableValue("to");
      return new SetAnimation(this.timedElement, this, to);
   }

   protected boolean canAnimateType(int type) {
      return true;
   }

   protected boolean isConstantAnimation() {
      return true;
   }
}
