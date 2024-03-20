package org.apache.fop.accessibility.fo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.fo.FOEventHandler;

class Event {
   private List children = new ArrayList();
   protected FOEventHandler eventHandler;
   protected Event parent;
   protected boolean hasContent;

   public Event(FO2StructureTreeConverter structureTreeConverter) {
      this.eventHandler = structureTreeConverter.converter;
   }

   public Event(Event parent) {
      this.parent = parent;
   }

   public void run() {
      if (this.hasContent()) {
         Iterator var1 = this.children.iterator();

         while(var1.hasNext()) {
            Event e = (Event)var1.next();
            e.run();
         }
      }

      this.children.clear();
   }

   private boolean hasContent() {
      Iterator var1 = this.children.iterator();

      Event e;
      do {
         if (!var1.hasNext()) {
            return this.hasContent;
         }

         e = (Event)var1.next();
      } while(!e.hasContent());

      return true;
   }

   public void add(Event child) {
      this.children.add(child);
   }
}
