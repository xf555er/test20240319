package org.apache.batik.util.io;

import java.io.IOException;
import java.io.Reader;

public abstract class NormalizingReader extends Reader {
   public int read(char[] cbuf, int off, int len) throws IOException {
      if (len == 0) {
         return 0;
      } else {
         int c = this.read();
         if (c == -1) {
            return -1;
         } else {
            int result = 0;

            do {
               cbuf[result + off] = (char)c;
               ++result;
               c = this.read();
            } while(c != -1 && result < len);

            return result;
         }
      }
   }

   public abstract int getLine();

   public abstract int getColumn();
}
