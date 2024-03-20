package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;

public class ClosedReader extends Reader {
   public static final ClosedReader CLOSED_READER = new ClosedReader();

   public int read(char[] cbuf, int off, int len) {
      return -1;
   }

   public void close() throws IOException {
   }
}
