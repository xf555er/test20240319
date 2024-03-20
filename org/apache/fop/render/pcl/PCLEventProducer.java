package org.apache.fop.render.pcl;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

public interface PCLEventProducer extends EventProducer {
   void paperTypeUnavailable(Object var1, long var2, long var4, String var6);

   void fontTypeNotSupported(Object var1, String var2, String var3);

   public static final class Provider {
      private Provider() {
      }

      public static PCLEventProducer get(EventBroadcaster broadcaster) {
         return (PCLEventProducer)broadcaster.getEventProducerFor(PCLEventProducer.class);
      }
   }
}
