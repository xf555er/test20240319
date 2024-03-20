package org.apache.batik.dom.events;

import org.apache.batik.w3c.dom.events.TextEvent;
import org.w3c.dom.views.AbstractView;

public class DOMTextEvent extends DOMUIEvent implements TextEvent {
   protected String data;

   public String getData() {
      return this.data;
   }

   public void initTextEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, String dataArg) {
      this.initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, 0);
      this.data = dataArg;
   }

   public void initTextEventNS(String namespaceURIArg, String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, String dataArg) {
      this.initUIEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg, viewArg, 0);
      this.data = dataArg;
   }
}
