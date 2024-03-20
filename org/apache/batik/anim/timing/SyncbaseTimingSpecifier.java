package org.apache.batik.anim.timing;

import java.util.HashMap;

public class SyncbaseTimingSpecifier extends OffsetTimingSpecifier {
   protected String syncbaseID;
   protected TimedElement syncbaseElement;
   protected boolean syncBegin;
   protected HashMap instances = new HashMap();

   public SyncbaseTimingSpecifier(TimedElement owner, boolean isBegin, float offset, String syncbaseID, boolean syncBegin) {
      super(owner, isBegin, offset);
      this.syncbaseID = syncbaseID;
      this.syncBegin = syncBegin;
      this.syncbaseElement = owner.getTimedElementById(syncbaseID);
      this.syncbaseElement.addDependent(this, syncBegin);
   }

   public String toString() {
      return this.syncbaseID + "." + (this.syncBegin ? "begin" : "end") + (this.offset != 0.0F ? super.toString() : "");
   }

   public void initialize() {
   }

   public boolean isEventCondition() {
      return false;
   }

   float newInterval(Interval interval) {
      if (this.owner.hasPropagated) {
         return Float.POSITIVE_INFINITY;
      } else {
         InstanceTime instance = new InstanceTime(this, (this.syncBegin ? interval.getBegin() : interval.getEnd()) + this.offset, true);
         this.instances.put(interval, instance);
         interval.addDependent(instance, this.syncBegin);
         return this.owner.addInstanceTime(instance, this.isBegin);
      }
   }

   float removeInterval(Interval interval) {
      if (this.owner.hasPropagated) {
         return Float.POSITIVE_INFINITY;
      } else {
         InstanceTime instance = (InstanceTime)this.instances.get(interval);
         interval.removeDependent(instance, this.syncBegin);
         return this.owner.removeInstanceTime(instance, this.isBegin);
      }
   }

   float handleTimebaseUpdate(InstanceTime instanceTime, float newTime) {
      return this.owner.hasPropagated ? Float.POSITIVE_INFINITY : this.owner.instanceTimeChanged(instanceTime, this.isBegin);
   }
}
