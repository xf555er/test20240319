package org.apache.batik.dom.events;

import org.w3c.dom.smil.TimeEvent;
import org.w3c.dom.views.AbstractView;

public class DOMTimeEvent extends AbstractEvent implements TimeEvent {
   protected AbstractView view;
   protected int detail;

   public AbstractView getView() {
      return this.view;
   }

   public int getDetail() {
      return this.detail;
   }

   public void initTimeEvent(String typeArg, AbstractView viewArg, int detailArg) {
      this.initEvent(typeArg, false, false);
      this.view = viewArg;
      this.detail = detailArg;
   }

   public void initTimeEventNS(String namespaceURIArg, String typeArg, AbstractView viewArg, int detailArg) {
      this.initEventNS(namespaceURIArg, typeArg, false, false);
      this.view = viewArg;
      this.detail = detailArg;
   }

   public void setTimestamp(long timeStamp) {
      this.timeStamp = timeStamp;
   }
}
