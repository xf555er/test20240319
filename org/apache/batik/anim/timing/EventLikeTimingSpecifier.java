package org.apache.batik.anim.timing;

import org.w3c.dom.events.Event;

public abstract class EventLikeTimingSpecifier extends OffsetTimingSpecifier {
   public EventLikeTimingSpecifier(TimedElement owner, boolean isBegin, float offset) {
      super(owner, isBegin, offset);
   }

   public boolean isEventCondition() {
      return true;
   }

   public abstract void resolve(Event var1);
}
