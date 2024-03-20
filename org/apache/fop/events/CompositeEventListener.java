package org.apache.fop.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompositeEventListener implements EventListener {
   private List listeners = new ArrayList();

   public synchronized void addEventListener(EventListener listener) {
      this.listeners.add(listener);
   }

   public synchronized void removeEventListener(EventListener listener) {
      this.listeners.remove(listener);
   }

   public synchronized boolean hasEventListeners() {
      return !this.listeners.isEmpty();
   }

   public synchronized void processEvent(Event event) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         EventListener listener = (EventListener)var2.next();
         listener.processEvent(event);
      }

   }
}
