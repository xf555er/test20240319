package org.apache.batik.dom.svg12;

import org.apache.batik.dom.events.DOMUIEvent;
import org.w3c.dom.views.AbstractView;

public class SVGOMWheelEvent extends DOMUIEvent {
   protected int wheelDelta;

   public int getWheelDelta() {
      return this.wheelDelta;
   }

   public void initWheelEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, int wheelDeltaArg) {
      this.initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, 0);
      this.wheelDelta = wheelDeltaArg;
   }

   public void initWheelEventNS(String namespaceURIArg, String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, int wheelDeltaArg) {
      this.initUIEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg, viewArg, 0);
      this.wheelDelta = wheelDeltaArg;
   }
}
