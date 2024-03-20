package net.jsign.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import net.jsign.commons.io.function.IOConsumer;
import net.jsign.commons.io.function.IOFunction;

public class ThresholdingOutputStream extends OutputStream {
   private static IOFunction NOOP_OS_GETTER = (os) -> {
      return NullOutputStream.NULL_OUTPUT_STREAM;
   };
   private final int threshold;
   private final IOConsumer thresholdConsumer;
   private final IOFunction outputStreamGetter;
   private long written;
   private boolean thresholdExceeded;

   public ThresholdingOutputStream(int threshold, IOConsumer thresholdConsumer, IOFunction outputStreamGetter) {
      this.threshold = threshold;
      this.thresholdConsumer = thresholdConsumer == null ? IOConsumer.noop() : thresholdConsumer;
      this.outputStreamGetter = outputStreamGetter == null ? NOOP_OS_GETTER : outputStreamGetter;
   }

   protected void checkThreshold(int count) throws IOException {
      if (!this.thresholdExceeded && this.written + (long)count > (long)this.threshold) {
         this.thresholdExceeded = true;
         this.thresholdReached();
      }

   }

   public void close() throws IOException {
      try {
         this.flush();
      } catch (IOException var2) {
      }

      this.getStream().close();
   }

   public void flush() throws IOException {
      this.getStream().flush();
   }

   protected OutputStream getStream() throws IOException {
      return (OutputStream)this.outputStreamGetter.apply(this);
   }

   protected void thresholdReached() throws IOException {
      this.thresholdConsumer.accept(this);
   }

   public void write(byte[] b) throws IOException {
      this.checkThreshold(b.length);
      this.getStream().write(b);
      this.written += (long)b.length;
   }

   public void write(byte[] b, int off, int len) throws IOException {
      this.checkThreshold(len);
      this.getStream().write(b, off, len);
      this.written += (long)len;
   }

   public void write(int b) throws IOException {
      this.checkThreshold(1);
      this.getStream().write(b);
      ++this.written;
   }
}
