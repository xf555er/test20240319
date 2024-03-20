package org.apache.batik.anim;

import org.apache.batik.anim.dom.AnimatableElement;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableValue;

public class SetAnimation extends AbstractAnimation {
   protected AnimatableValue to;

   public SetAnimation(TimedElement timedElement, AnimatableElement animatableElement, AnimatableValue to) {
      super(timedElement, animatableElement);
      this.to = to;
   }

   protected void sampledAt(float simpleTime, float simpleDur, int repeatIteration) {
      if (this.value == null) {
         this.value = this.to;
         this.markDirty();
      }

   }

   protected void sampledLastValue(int repeatIteration) {
      if (this.value == null) {
         this.value = this.to;
         this.markDirty();
      }

   }
}
