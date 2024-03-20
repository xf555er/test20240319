package org.apache.batik.anim.dom;

import java.util.HashMap;
import java.util.HashSet;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.EventListenerList;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.ShadowTreeEvent;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;

public class XBLEventSupport extends EventSupport {
   protected HashMap capturingImplementationListeners;
   protected HashMap bubblingImplementationListeners;
   protected static HashMap eventTypeAliases = new HashMap();

   public XBLEventSupport(AbstractNode n) {
      super(n);
   }

   public void addEventListenerNS(String namespaceURI, String type, EventListener listener, boolean useCapture, Object group) {
      super.addEventListenerNS(namespaceURI, type, listener, useCapture, group);
      if (namespaceURI == null || namespaceURI.equals("http://www.w3.org/2001/xml-events")) {
         String alias = (String)eventTypeAliases.get(type);
         if (alias != null) {
            super.addEventListenerNS(namespaceURI, alias, listener, useCapture, group);
         }
      }

   }

   public void removeEventListenerNS(String namespaceURI, String type, EventListener listener, boolean useCapture) {
      super.removeEventListenerNS(namespaceURI, type, listener, useCapture);
      if (namespaceURI == null || namespaceURI.equals("http://www.w3.org/2001/xml-events")) {
         String alias = (String)eventTypeAliases.get(type);
         if (alias != null) {
            super.removeEventListenerNS(namespaceURI, alias, listener, useCapture);
         }
      }

   }

   public void addImplementationEventListenerNS(String namespaceURI, String type, EventListener listener, boolean useCapture) {
      HashMap listeners;
      if (useCapture) {
         if (this.capturingImplementationListeners == null) {
            this.capturingImplementationListeners = new HashMap();
         }

         listeners = this.capturingImplementationListeners;
      } else {
         if (this.bubblingImplementationListeners == null) {
            this.bubblingImplementationListeners = new HashMap();
         }

         listeners = this.bubblingImplementationListeners;
      }

      EventListenerList list = (EventListenerList)listeners.get(type);
      if (list == null) {
         list = new EventListenerList();
         listeners.put(type, list);
      }

      list.addListener(namespaceURI, (Object)null, listener);
   }

   public void removeImplementationEventListenerNS(String namespaceURI, String type, EventListener listener, boolean useCapture) {
      HashMap listeners = useCapture ? this.capturingImplementationListeners : this.bubblingImplementationListeners;
      if (listeners != null) {
         EventListenerList list = (EventListenerList)listeners.get(type);
         if (list != null) {
            list.removeListener(namespaceURI, listener);
            if (list.size() == 0) {
               listeners.remove(type);
            }

         }
      }
   }

   public void moveEventListeners(EventSupport other) {
      super.moveEventListeners(other);
      XBLEventSupport es = (XBLEventSupport)other;
      es.capturingImplementationListeners = this.capturingImplementationListeners;
      es.bubblingImplementationListeners = this.bubblingImplementationListeners;
      this.capturingImplementationListeners = null;
      this.bubblingImplementationListeners = null;
   }

   public boolean dispatchEvent(NodeEventTarget target, Event evt) throws EventException {
      if (evt == null) {
         return false;
      } else if (!(evt instanceof AbstractEvent)) {
         throw this.createEventException((short)9, "unsupported.event", new Object[0]);
      } else {
         AbstractEvent e = (AbstractEvent)evt;
         String type = e.getType();
         if (type != null && type.length() != 0) {
            this.setTarget(e, target);
            this.stopPropagation(e, false);
            this.stopImmediatePropagation(e, false);
            this.preventDefault(e, false);
            NodeEventTarget[] ancestors = this.getAncestors(target);
            int bubbleLimit = e.getBubbleLimit();
            int minAncestor = 0;
            if (this.isSingleScopeEvent(e)) {
               AbstractNode targetNode = (AbstractNode)target;
               Node boundElement = targetNode.getXblBoundElement();
               if (boundElement != null) {
                  for(minAncestor = ancestors.length; minAncestor > 0; --minAncestor) {
                     AbstractNode ancestorNode = (AbstractNode)ancestors[minAncestor - 1];
                     if (ancestorNode.getXblBoundElement() != boundElement) {
                        break;
                     }
                  }
               }
            } else if (bubbleLimit != 0) {
               minAncestor = ancestors.length - bubbleLimit + 1;
               if (minAncestor < 0) {
                  minAncestor = 0;
               }
            }

            AbstractEvent[] es = this.getRetargettedEvents(target, ancestors, e);
            boolean preventDefault = false;
            HashSet stoppedGroups = new HashSet();
            HashSet toBeStoppedGroups = new HashSet();

            int i;
            NodeEventTarget node;
            for(i = 0; i < minAncestor; ++i) {
               node = ancestors[i];
               this.setCurrentTarget(es[i], node);
               this.setEventPhase(es[i], (short)1);
               this.fireImplementationEventListeners(node, es[i], true);
            }

            for(i = minAncestor; i < ancestors.length; ++i) {
               node = ancestors[i];
               this.setCurrentTarget(es[i], node);
               this.setEventPhase(es[i], (short)1);
               this.fireImplementationEventListeners(node, es[i], true);
               this.fireEventListeners(node, es[i], true, stoppedGroups, toBeStoppedGroups);
               this.fireHandlerGroupEventListeners(node, es[i], true, stoppedGroups, toBeStoppedGroups);
               preventDefault = preventDefault || es[i].getDefaultPrevented();
               stoppedGroups.addAll(toBeStoppedGroups);
               toBeStoppedGroups.clear();
            }

            this.setEventPhase(e, (short)2);
            this.setCurrentTarget(e, target);
            this.fireImplementationEventListeners(target, e, false);
            this.fireEventListeners(target, e, false, stoppedGroups, toBeStoppedGroups);
            this.fireHandlerGroupEventListeners(this.node, e, false, stoppedGroups, toBeStoppedGroups);
            stoppedGroups.addAll(toBeStoppedGroups);
            toBeStoppedGroups.clear();
            preventDefault = preventDefault || e.getDefaultPrevented();
            if (e.getBubbles()) {
               for(i = ancestors.length - 1; i >= minAncestor; --i) {
                  node = ancestors[i];
                  this.setCurrentTarget(es[i], node);
                  this.setEventPhase(es[i], (short)3);
                  this.fireImplementationEventListeners(node, es[i], false);
                  this.fireEventListeners(node, es[i], false, stoppedGroups, toBeStoppedGroups);
                  this.fireHandlerGroupEventListeners(node, es[i], false, stoppedGroups, toBeStoppedGroups);
                  preventDefault = preventDefault || es[i].getDefaultPrevented();
                  stoppedGroups.addAll(toBeStoppedGroups);
                  toBeStoppedGroups.clear();
               }

               for(i = minAncestor - 1; i >= 0; --i) {
                  node = ancestors[i];
                  this.setCurrentTarget(es[i], node);
                  this.setEventPhase(es[i], (short)3);
                  this.fireImplementationEventListeners(node, es[i], false);
                  preventDefault = preventDefault || es[i].getDefaultPrevented();
               }
            }

            if (!preventDefault) {
               this.runDefaultActions(e);
            }

            return preventDefault;
         } else {
            throw this.createEventException((short)0, "unspecified.event", new Object[0]);
         }
      }
   }

   protected void fireHandlerGroupEventListeners(NodeEventTarget node, AbstractEvent e, boolean useCapture, HashSet stoppedGroups, HashSet toBeStoppedGroups) {
      NodeList defs = ((NodeXBL)node).getXblDefinitions();

      for(int j = 0; j < defs.getLength(); ++j) {
         Node n;
         for(n = defs.item(j).getFirstChild(); n != null && !(n instanceof XBLOMHandlerGroupElement); n = n.getNextSibling()) {
         }

         if (n != null) {
            node = (NodeEventTarget)n;
            String type = e.getType();
            EventSupport support = node.getEventSupport();
            if (support != null) {
               EventListenerList list = support.getEventListeners(type, useCapture);
               if (list == null) {
                  return;
               }

               EventListenerList.Entry[] listeners = list.getEventListeners();
               this.fireEventListeners(node, e, listeners, stoppedGroups, toBeStoppedGroups);
            }
         }
      }

   }

   protected boolean isSingleScopeEvent(Event evt) {
      return evt instanceof MutationEvent || evt instanceof ShadowTreeEvent;
   }

   protected AbstractEvent[] getRetargettedEvents(NodeEventTarget target, NodeEventTarget[] ancestors, AbstractEvent e) {
      boolean singleScope = this.isSingleScopeEvent(e);
      AbstractNode targetNode = (AbstractNode)target;
      AbstractEvent[] es = new AbstractEvent[ancestors.length];
      if (ancestors.length > 0) {
         int index = ancestors.length - 1;
         Node boundElement = targetNode.getXblBoundElement();
         AbstractNode ancestorNode = (AbstractNode)ancestors[index];
         if (!singleScope && ancestorNode.getXblBoundElement() != boundElement) {
            es[index] = this.retargetEvent(e, ancestors[index]);
         } else {
            es[index] = e;
         }

         while(true) {
            while(true) {
               --index;
               if (index < 0) {
                  return es;
               }

               ancestorNode = (AbstractNode)ancestors[index + 1];
               boundElement = ancestorNode.getXblBoundElement();
               AbstractNode nextAncestorNode = (AbstractNode)ancestors[index];
               Node nextBoundElement = nextAncestorNode.getXblBoundElement();
               if (!singleScope && nextBoundElement != boundElement) {
                  es[index] = this.retargetEvent(es[index + 1], ancestors[index]);
               } else {
                  es[index] = es[index + 1];
               }
            }
         }
      } else {
         return es;
      }
   }

   protected AbstractEvent retargetEvent(AbstractEvent e, NodeEventTarget target) {
      AbstractEvent clonedEvent = e.cloneEvent();
      this.setTarget(clonedEvent, target);
      return clonedEvent;
   }

   public EventListenerList getImplementationEventListeners(String type, boolean useCapture) {
      HashMap listeners = useCapture ? this.capturingImplementationListeners : this.bubblingImplementationListeners;
      return listeners != null ? (EventListenerList)listeners.get(type) : null;
   }

   protected void fireImplementationEventListeners(NodeEventTarget node, AbstractEvent e, boolean useCapture) {
      String type = e.getType();
      XBLEventSupport support = (XBLEventSupport)node.getEventSupport();
      if (support != null) {
         EventListenerList list = support.getImplementationEventListeners(type, useCapture);
         if (list != null) {
            EventListenerList.Entry[] listeners = list.getEventListeners();
            this.fireEventListeners(node, e, listeners, (HashSet)null, (HashSet)null);
         }
      }
   }

   static {
      eventTypeAliases.put("SVGLoad", "load");
      eventTypeAliases.put("SVGUnoad", "unload");
      eventTypeAliases.put("SVGAbort", "abort");
      eventTypeAliases.put("SVGError", "error");
      eventTypeAliases.put("SVGResize", "resize");
      eventTypeAliases.put("SVGScroll", "scroll");
      eventTypeAliases.put("SVGZoom", "zoom");
   }
}
