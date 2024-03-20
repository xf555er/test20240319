package org.apache.fop.layoutmgr.inline;

import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.events.EventProducer;
import org.xml.sax.Locator;

public interface InlineLevelEventProducer extends EventProducer {
   void leaderWithoutContent(Object var1, Locator var2);

   void lineOverflows(Object var1, String var2, int var3, int var4, Locator var5);

   void inlineContainerAutoIPDNotSupported(Object var1, float var2);

   public static final class Provider {
      private Provider() {
      }

      public static InlineLevelEventProducer get(EventBroadcaster broadcaster) {
         return (InlineLevelEventProducer)broadcaster.getEventProducerFor(InlineLevelEventProducer.class);
      }
   }
}
