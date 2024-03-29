package org.apache.commons.io.input;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;

public class DemuxInputStream extends InputStream {
   private final InheritableThreadLocal inputStreamLocal = new InheritableThreadLocal();

   public InputStream bindStream(InputStream input) {
      InputStream oldValue = (InputStream)this.inputStreamLocal.get();
      this.inputStreamLocal.set(input);
      return oldValue;
   }

   public void close() throws IOException {
      IOUtils.close((Closeable)this.inputStreamLocal.get());
   }

   public int read() throws IOException {
      InputStream inputStream = (InputStream)this.inputStreamLocal.get();
      return null != inputStream ? inputStream.read() : -1;
   }
}
