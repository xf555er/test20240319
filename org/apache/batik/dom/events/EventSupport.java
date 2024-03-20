package org.apache.batik.dom.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.AbstractNode;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;

public class EventSupport {
   protected HashMap capturingListeners;
   protected HashMap bubblingListeners;
   protected AbstractNode node;

   public EventSupport(AbstractNode n) {
      this.node = n;
   }

   public void addEventListener(String type, EventListener listener, boolean useCapture) {
      this.addEventListenerNS((String)null, type, listener, useCapture, (Object)null);
   }

   public void addEventListenerNS(String namespaceURI, String type, EventListener listener, boolean useCapture, Object group) {
      HashMap listeners;
      if (useCapture) {
         if (this.capturingListeners == null) {
            this.capturingListeners = new HashMap();
         }

         listeners = this.capturingListeners;
      } else {
         if (this.bubblingListeners == null) {
            this.bubblingListeners = new HashMap();
         }

         listeners = this.bubblingListeners;
      }

      EventListenerList list = (EventListenerList)listeners.get(type);
      if (list == null) {
         list = new EventListenerList();
         listeners.put(type, list);
      }

      list.addListener(namespaceURI, group, listener);
   }

   public void removeEventListener(String type, EventListener listener, boolean useCapture) {
      this.removeEventListenerNS((String)null, type, listener, useCapture);
   }

   public void removeEventListenerNS(String namespaceURI, String type, EventListener listener, boolean useCapture) {
      HashMap listeners;
      if (useCapture) {
         listeners = this.capturingListeners;
      } else {
         listeners = this.bubblingListeners;
      }

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
      other.capturingListeners = this.capturingListeners;
      other.bubblingListeners = this.bubblingListeners;
      this.capturingListeners = null;
      this.bubblingListeners = null;
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
            e.setTarget(target);
            e.stopPropagation(false);
            e.stopImmediatePropagation(false);
            e.preventDefault(false);
            NodeEventTarget[] ancestors = this.getAncestors(target);
            e.setEventPhase((short)1);
            HashSet stoppedGroups = new HashSet();
            HashSet toBeStoppedGroups = new HashSet();
            NodeEventTarget[] var8 = ancestors;
            int var9 = ancestors.length;

            for(int var10 = 0; var10 < var9; ++var10) {
               NodeEventTarget node = var8[var10];
               e.setCurrentTarget(node);
               this.fireEventListeners(node, e, true, stoppedGroups, toBeStoppedGroups);
               stoppedGroups.addAll(toBeStoppedGroups);
               toBeStoppedGroups.clear();
            }

            e.setEventPhase((short)2);
            e.setCurrentTarget(target);
            this.fireEventListeners(target, e, false, stoppedGroups, toBeStoppedGroups);
            stoppedGroups.addAll(toBeStoppedGroups);
            toBeStoppedGroups.clear();
            if (e.getBubbles()) {
               e.setEventPhase((short)3);

               for(int i = ancestors.length - 1; i >= 0; --i) {
                  NodeEventTarget node = ancestors[i];
                  e.setCurrentTarget(node);
                  this.fireEventListeners(node, e, false, stoppedGroups, toBeStoppedGroups);
                  stoppedGroups.addAll(toBeStoppedGroups);
                  toBeStoppedGroups.clear();
               }
            }

            if (!e.getDefaultPrevented()) {
               this.runDefaultActions(e);
            }

            return e.getDefaultPrevented();
         } else {
            throw this.createEventException((short)0, "unspecified.event", new Object[0]);
         }
      }
   }

   protected void runDefaultActions(AbstractEvent e) {
      List runables = e.getDefaultActions();
      if (runables != null) {
         Iterator var3 = runables.iterator();

         while(var3.hasNext()) {
            Object runable = var3.next();
            Runnable r = (Runnable)runable;
            r.run();
         }
      }

   }

   protected void fireEventListeners(NodeEventTarget node, AbstractEvent e, EventListenerList.Entry[] listeners, HashSet stoppedGroups, HashSet toBeStoppedGroups) {
      if (listeners != null) {
         String eventNS = e.getNamespaceURI();
         EventListenerList.Entry[] var7 = listeners;
         int var8 = listeners.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            EventListenerList.Entry listener = var7[var9];

            try {
               String listenerNS = listener.getNamespaceURI();
               if (listenerNS == null || eventNS == null || listenerNS.equals(eventNS)) {
                  Object group = listener.getGroup();
                  if (stoppedGroups == null || !stoppedGroups.contains(group)) {
                     listener.getListener().handleEvent(e);
                     if (e.getStopImmediatePropagation()) {
                        if (stoppedGroups != null) {
                           stoppedGroups.add(group);
                        }

                        e.stopImmediatePropagation(false);
                     } else if (e.getStopPropagation()) {
                        if (toBeStoppedGroups != null) {
                           toBeStoppedGroups.add(group);
                        }

                        e.stopPropagation(false);
                     }
                  }
               }
            } catch (ThreadDeath var13) {
               throw var13;
            } catch (Throwable var14) {
               var14.printStackTrace();
            }
         }

      }
   }

   protected void fireEventListeners(NodeEventTarget node, AbstractEvent e, boolean useCapture, HashSet stoppedGroups, HashSet toBeStoppedGroups) {
      String type = e.getType();
      EventSupport support = node.getEventSupport();
      if (support != null) {
         EventListenerList list = support.getEventListeners(type, useCapture);
         if (list != null) {
            EventListenerList.Entry[] listeners = list.getEventListeners();
            this.fireEventListeners(node, e, listeners, stoppedGroups, toBeStoppedGroups);
         }
      }
   }

   protected NodeEventTarget[] getAncestors(NodeEventTarget node) {
      node = node.getParentNodeEventTarget();
      int nancestors = 0;

      for(NodeEventTarget n = node; n != null; ++nancestors) {
         n = n.getParentNodeEventTarget();
      }

      NodeEventTarget[] ancestors = new NodeEventTarget[nancestors];

      for(int i = nancestors - 1; i >= 0; node = node.getParentNodeEventTarget()) {
         ancestors[i] = node;
         --i;
      }

      return ancestors;
   }

   public boolean hasEventListenerNS(String namespaceURI, String type) {
      EventListenerList ell;
      if (this.capturingListeners != null) {
         ell = (EventListenerList)this.capturingListeners.get(type);
         if (ell != null && ell.hasEventListener(namespaceURI)) {
            return true;
         }
      }

      if (this.bubblingListeners != null) {
         ell = (EventListenerList)this.capturingListeners.get(type);
         if (ell != null) {
            return ell.hasEventListener(namespaceURI);
         }
      }

      return false;
   }

   public EventListenerList getEventListeners(String type, boolean useCapture) {
      HashMap listeners = useCapture ? this.capturingListeners : this.bubblingListeners;
      return listeners == null ? null : (EventListenerList)listeners.get(type);
   }

   protected EventException createEventException(short code, String key, Object[] args) {
      try {
         AbstractDocument doc = (AbstractDocument)this.node.getOwnerDocument();
         return new EventException(code, doc.formatMessage(key, args));
      } catch (Exception var5) {
         return new EventException(code, key);
      }
   }

   protected void setTarget(AbstractEvent e, NodeEventTarget target) {
      e.setTarget(target);
   }

   protected void stopPropagation(AbstractEvent e, boolean b) {
      e.stopPropagation(b);
   }

   protected void stopImmediatePropagation(AbstractEvent e, boolean b) {
      e.stopImmediatePropagation(b);
   }

   protected void preventDefault(AbstractEvent e, boolean b) {
      e.preventDefault(b);
   }

   protected void setCurrentTarget(AbstractEvent e, NodeEventTarget target) {
      e.setCurrentTarget(target);
   }

   protected void setEventPhase(AbstractEvent e, short phase) {
      e.setEventPhase(phase);
   }

   public static Event getUltimateOriginalEvent(Event evt) {
      AbstractEvent e = (AbstractEvent)evt;

      while(true) {
         AbstractEvent origEvt = (AbstractEvent)e.getOriginalEvent();
         if (origEvt == null) {
            return e;
         }

         e = origEvt;
      }
   }
}
