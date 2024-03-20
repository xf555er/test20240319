package org.apache.commons.io.function;

import java.io.IOException;
import java.util.Objects;

@FunctionalInterface
public interface IOConsumer {
   IOConsumer NOOP_IO_CONSUMER = (t) -> {
   };

   static IOConsumer noop() {
      return NOOP_IO_CONSUMER;
   }

   void accept(Object var1) throws IOException;

   default IOConsumer andThen(IOConsumer after) {
      Objects.requireNonNull(after, "after");
      return (t) -> {
         this.accept(t);
         after.accept(t);
      };
   }
}
