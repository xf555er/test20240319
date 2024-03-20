package org.apache.batik.bridge;

import java.util.Calendar;
import org.apache.batik.anim.dom.SVGOMAnimationElement;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.dom.events.DOMTimeEvent;
import org.apache.batik.dom.svg.IdContainer;
import org.apache.batik.dom.svg.SVGOMUseShadowRoot;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;

public abstract class AnimationSupport {
   public static void fireTimeEvent(EventTarget target, String eventType, Calendar time, int detail) {
      DocumentEvent de = (DocumentEvent)((Node)target).getOwnerDocument();
      DOMTimeEvent evt = (DOMTimeEvent)de.createEvent("TimeEvent");
      evt.initTimeEventNS("http://www.w3.org/2001/xml-events", eventType, (AbstractView)null, detail);
      evt.setTimestamp(time.getTime().getTime());
      target.dispatchEvent(evt);
   }

   public static TimedElement getTimedElementById(String id, Node n) {
      Element e = getElementById(id, n);
      if (e instanceof SVGOMAnimationElement) {
         SVGAnimationElementBridge b = (SVGAnimationElementBridge)((SVGOMAnimationElement)e).getSVGContext();
         return b.getTimedElement();
      } else {
         return null;
      }
   }

   public static EventTarget getEventTargetById(String id, Node n) {
      return (EventTarget)getElementById(id, n);
   }

   protected static Element getElementById(String id, Node n) {
      Node p = n.getParentNode();

      while(p != null) {
         n = p;
         if (p instanceof SVGOMUseShadowRoot) {
            p = ((SVGOMUseShadowRoot)p).getCSSParentNode();
         } else {
            p = p.getParentNode();
         }
      }

      if (n instanceof IdContainer) {
         return ((IdContainer)n).getElementById(id);
      } else {
         return null;
      }
   }
}
