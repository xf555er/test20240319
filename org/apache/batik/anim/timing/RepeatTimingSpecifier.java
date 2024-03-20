package org.apache.batik.anim.timing;

import org.w3c.dom.events.Event;
import org.w3c.dom.smil.TimeEvent;

public class RepeatTimingSpecifier extends EventbaseTimingSpecifier {
   protected int repeatIteration;
   protected boolean repeatIterationSpecified;

   public RepeatTimingSpecifier(TimedElement owner, boolean isBegin, float offset, String syncbaseID) {
      super(owner, isBegin, offset, syncbaseID, owner.getRoot().getRepeatEventName());
   }

   public RepeatTimingSpecifier(TimedElement owner, boolean isBegin, float offset, String syncbaseID, int repeatIteration) {
      super(owner, isBegin, offset, syncbaseID, owner.getRoot().getRepeatEventName());
      this.repeatIteration = repeatIteration;
      this.repeatIterationSpecified = true;
   }

   public String toString() {
      return (this.eventbaseID == null ? "" : this.eventbaseID + ".") + "repeat" + (this.repeatIterationSpecified ? "(" + this.repeatIteration + ")" : "") + (this.offset != 0.0F ? super.toString() : "");
   }

   public void handleEvent(Event e) {
      TimeEvent evt = (TimeEvent)e;
      if (!this.repeatIterationSpecified || evt.getDetail() == this.repeatIteration) {
         super.handleEvent(e);
      }

   }
}
