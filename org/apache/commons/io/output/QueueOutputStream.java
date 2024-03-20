package org.apache.commons.io.output;

import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.io.input.QueueInputStream;

public class QueueOutputStream extends OutputStream {
   private final BlockingQueue blockingQueue;

   public QueueOutputStream() {
      this(new LinkedBlockingQueue());
   }

   public QueueOutputStream(BlockingQueue blockingQueue) {
      this.blockingQueue = (BlockingQueue)Objects.requireNonNull(blockingQueue, "blockingQueue");
   }

   public QueueInputStream newQueueInputStream() {
      return new QueueInputStream(this.blockingQueue);
   }

   public void write(int b) throws InterruptedIOException {
      try {
         this.blockingQueue.put(255 & b);
      } catch (InterruptedException var4) {
         Thread.currentThread().interrupt();
         InterruptedIOException interruptedIoException = new InterruptedIOException();
         interruptedIoException.initCause(var4);
         throw interruptedIoException;
      }
   }
}
