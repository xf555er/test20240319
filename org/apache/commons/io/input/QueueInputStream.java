package org.apache.commons.io.input;

import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.io.output.QueueOutputStream;

public class QueueInputStream extends InputStream {
   private final BlockingQueue blockingQueue;

   public QueueInputStream() {
      this(new LinkedBlockingQueue());
   }

   public QueueInputStream(BlockingQueue blockingQueue) {
      this.blockingQueue = (BlockingQueue)Objects.requireNonNull(blockingQueue, "blockingQueue");
   }

   public QueueOutputStream newQueueOutputStream() {
      return new QueueOutputStream(this.blockingQueue);
   }

   public int read() {
      Integer value = (Integer)this.blockingQueue.poll();
      return value == null ? -1 : 255 & value;
   }
}
