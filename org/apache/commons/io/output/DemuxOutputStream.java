package org.apache.commons.io.output;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;

public class DemuxOutputStream extends OutputStream {
   private final InheritableThreadLocal outputStreamThreadLocal = new InheritableThreadLocal();

   public OutputStream bindStream(OutputStream output) {
      OutputStream stream = (OutputStream)this.outputStreamThreadLocal.get();
      this.outputStreamThreadLocal.set(output);
      return stream;
   }

   public void close() throws IOException {
      IOUtils.close((Closeable)this.outputStreamThreadLocal.get());
   }

   public void flush() throws IOException {
      OutputStream output = (OutputStream)this.outputStreamThreadLocal.get();
      if (null != output) {
         output.flush();
      }

   }

   public void write(int ch) throws IOException {
      OutputStream output = (OutputStream)this.outputStreamThreadLocal.get();
      if (null != output) {
         output.write(ch);
      }

   }
}
