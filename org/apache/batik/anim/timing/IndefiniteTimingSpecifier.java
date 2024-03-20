package org.apache.batik.anim.timing;

public class IndefiniteTimingSpecifier extends TimingSpecifier {
   public IndefiniteTimingSpecifier(TimedElement owner, boolean isBegin) {
      super(owner, isBegin);
   }

   public String toString() {
      return "indefinite";
   }

   public void initialize() {
      if (!this.isBegin) {
         InstanceTime instance = new InstanceTime(this, Float.POSITIVE_INFINITY, false);
         this.owner.addInstanceTime(instance, this.isBegin);
      }

   }

   public boolean isEventCondition() {
      return false;
   }
}
