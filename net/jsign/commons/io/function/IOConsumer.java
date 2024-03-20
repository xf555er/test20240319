package net.jsign.commons.io.function;

import java.io.IOException;

@FunctionalInterface
public interface IOConsumer {
   IOConsumer NOOP_IO_CONSUMER = (t) -> {
   };

   static IOConsumer noop() {
      return NOOP_IO_CONSUMER;
   }

   void accept(Object var1) throws IOException;
}
