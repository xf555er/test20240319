package org.apache.commons.io.output;

import java.io.IOException;
import java.io.Writer;

public class BrokenWriter extends Writer {
   private final IOException exception;

   public BrokenWriter(IOException exception) {
      this.exception = exception;
   }

   public BrokenWriter() {
      this(new IOException("Broken writer"));
   }

   public void write(char[] cbuf, int off, int len) throws IOException {
      throw this.exception;
   }

   public void flush() throws IOException {
      throw this.exception;
   }

   public void close() throws IOException {
      throw this.exception;
   }
}
