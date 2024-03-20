package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;

public class BrokenReader extends Reader {
   private final IOException exception;

   public BrokenReader(IOException exception) {
      this.exception = exception;
   }

   public BrokenReader() {
      this(new IOException("Broken reader"));
   }

   public int read(char[] cbuf, int off, int len) throws IOException {
      throw this.exception;
   }

   public long skip(long n) throws IOException {
      throw this.exception;
   }

   public boolean ready() throws IOException {
      throw this.exception;
   }

   public void mark(int readAheadLimit) throws IOException {
      throw this.exception;
   }

   public synchronized void reset() throws IOException {
      throw this.exception;
   }

   public void close() throws IOException {
      throw this.exception;
   }
}
