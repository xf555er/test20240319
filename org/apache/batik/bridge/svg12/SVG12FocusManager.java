package org.apache.batik.bridge.svg12;

import org.apache.batik.anim.dom.XBLEventSupport;
import org.apache.batik.bridge.FocusManager;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.DOMUIEvent;
import org.apache.batik.dom.events.EventSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;

public class SVG12FocusManager extends FocusManager {
   public SVG12FocusManager(Document doc) {
      super(doc);
   }

   protected void addEventListeners(Document doc) {
      AbstractNode n = (AbstractNode)doc;
      XBLEventSupport es = (XBLEventSupport)n.initializeEventSupport();
      this.mouseclickListener = new MouseClickTracker();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.mouseclickListener, true);
      this.mouseoverListener = new MouseOverTracker();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.mouseoverListener, true);
      this.mouseoutListener = new MouseOutTracker();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.mouseoutListener, true);
      this.domFocusInListener = new DOMFocusInTracker();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusIn", this.domFocusInListener, true);
      this.domFocusOutListener = new FocusManager.DOMFocusOutTracker();
      es.addImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusOut", this.domFocusOutListener, true);
   }

   protected void removeEventListeners(Document doc) {
      AbstractNode n = (AbstractNode)doc;
      XBLEventSupport es = (XBLEventSupport)n.getEventSupport();
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "click", this.mouseclickListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.mouseoverListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.mouseoutListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusIn", this.domFocusInListener, true);
      es.removeImplementationEventListenerNS("http://www.w3.org/2001/xml-events", "DOMFocusOut", this.domFocusOutListener, true);
   }

   protected void fireDOMFocusInEvent(EventTarget target, EventTarget relatedTarget) {
      DocumentEvent docEvt = (DocumentEvent)((Element)target).getOwnerDocument();
      DOMUIEvent uiEvt = (DOMUIEvent)docEvt.createEvent("UIEvents");
      uiEvt.initUIEventNS("http://www.w3.org/2001/xml-events", "DOMFocusIn", true, false, (AbstractView)null, 0);
      int limit = DefaultXBLManager.computeBubbleLimit((Node)relatedTarget, (Node)target);
      uiEvt.setBubbleLimit(limit);
      target.dispatchEvent(uiEvt);
   }

   protected void fireDOMFocusOutEvent(EventTarget target, EventTarget relatedTarget) {
      DocumentEvent docEvt = (DocumentEvent)((Element)target).getOwnerDocument();
      DOMUIEvent uiEvt = (DOMUIEvent)docEvt.createEvent("UIEvents");
      uiEvt.initUIEventNS("http://www.w3.org/2001/xml-events", "DOMFocusOut", true, false, (AbstractView)null, 0);
      int limit = DefaultXBLManager.computeBubbleLimit((Node)target, (Node)relatedTarget);
      uiEvt.setBubbleLimit(limit);
      target.dispatchEvent(uiEvt);
   }

   protected class MouseOutTracker extends FocusManager.MouseOutTracker {
      protected MouseOutTracker() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }

   protected class MouseOverTracker extends FocusManager.MouseOverTracker {
      protected MouseOverTracker() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }

   protected class DOMFocusInTracker extends FocusManager.DOMFocusInTracker {
      protected DOMFocusInTracker() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }

   protected class MouseClickTracker extends FocusManager.MouseClickTracker {
      protected MouseClickTracker() {
         super();
      }

      public void handleEvent(Event evt) {
         super.handleEvent(EventSupport.getUltimateOriginalEvent(evt));
      }
   }
}
