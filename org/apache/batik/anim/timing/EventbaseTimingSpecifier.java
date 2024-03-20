package org.apache.batik.anim.timing;

import org.apache.batik.dom.events.NodeEventTarget;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public class EventbaseTimingSpecifier extends EventLikeTimingSpecifier implements EventListener {
   protected String eventbaseID;
   protected TimedElement eventbase;
   protected EventTarget eventTarget;
   protected String eventNamespaceURI;
   protected String eventType;
   protected String eventName;

   public EventbaseTimingSpecifier(TimedElement owner, boolean isBegin, float offset, String eventbaseID, String eventName) {
      super(owner, isBegin, offset);
      this.eventbaseID = eventbaseID;
      this.eventName = eventName;
      TimedDocumentRoot root = owner.getRoot();
      this.eventNamespaceURI = root.getEventNamespaceURI(eventName);
      this.eventType = root.getEventType(eventName);
      if (eventbaseID == null) {
         this.eventTarget = owner.getAnimationEventTarget();
      } else {
         this.eventTarget = owner.getEventTargetById(eventbaseID);
      }

   }

   public String toString() {
      return (this.eventbaseID == null ? "" : this.eventbaseID + ".") + this.eventName + (this.offset != 0.0F ? super.toString() : "");
   }

   public void initialize() {
      ((NodeEventTarget)this.eventTarget).addEventListenerNS(this.eventNamespaceURI, this.eventType, this, false, (Object)null);
   }

   public void deinitialize() {
      ((NodeEventTarget)this.eventTarget).removeEventListenerNS(this.eventNamespaceURI, this.eventType, this, false);
   }

   public void handleEvent(Event e) {
      this.owner.eventOccurred(this, e);
   }

   public void resolve(Event e) {
      float time = this.owner.getRoot().convertEpochTime(e.getTimeStamp());
      InstanceTime instance = new InstanceTime(this, time + this.offset, true);
      this.owner.addInstanceTime(instance, this.isBegin);
   }
}
