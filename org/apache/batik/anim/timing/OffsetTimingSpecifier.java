package org.apache.batik.anim.timing;

public class OffsetTimingSpecifier extends TimingSpecifier {
   protected float offset;

   public OffsetTimingSpecifier(TimedElement owner, boolean isBegin, float offset) {
      super(owner, isBegin);
      this.offset = offset;
   }

   public String toString() {
      return (this.offset >= 0.0F ? "+" : "") + this.offset;
   }

   public void initialize() {
      InstanceTime instance = new InstanceTime(this, this.offset, false);
      this.owner.addInstanceTime(instance, this.isBegin);
   }

   public boolean isEventCondition() {
      return false;
   }
}
