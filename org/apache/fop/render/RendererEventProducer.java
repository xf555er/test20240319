package org.apache.fop.render;

import java.io.IOException;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;

public interface RendererEventProducer extends EventProducer {
   void ioError(Object var1, IOException var2);

   void endPage(Object var1, int var2);

   public static final class Provider {
      private Provider() {
      }

      public static RendererEventProducer get(EventBroadcaster broadcaster) {
         return (RendererEventProducer)broadcaster.getEventProducerFor(RendererEventProducer.class);
      }
   }
}
