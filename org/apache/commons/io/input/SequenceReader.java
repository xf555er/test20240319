package org.apache.commons.io.input;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class SequenceReader extends Reader {
   private Reader reader;
   private Iterator readers;

   public SequenceReader(Iterable readers) {
      this.readers = ((Iterable)Objects.requireNonNull(readers, "readers")).iterator();
      this.reader = this.nextReader();
   }

   public SequenceReader(Reader... readers) {
      this((Iterable)Arrays.asList(readers));
   }

   public void close() throws IOException {
      this.readers = null;
      this.reader = null;
   }

   private Reader nextReader() {
      return this.readers.hasNext() ? (Reader)this.readers.next() : null;
   }

   public int read() throws IOException {
      int c;
      for(c = -1; this.reader != null; this.reader = this.nextReader()) {
         c = this.reader.read();
         if (c != -1) {
            break;
         }
      }

      return c;
   }

   public int read(char[] cbuf, int off, int len) throws IOException {
      Objects.requireNonNull(cbuf, "cbuf");
      if (len >= 0 && off >= 0 && off + len <= cbuf.length) {
         int count = 0;

         while(this.reader != null) {
            int readLen = this.reader.read(cbuf, off, len);
            if (readLen == -1) {
               this.reader = this.nextReader();
            } else {
               count += readLen;
               off += readLen;
               len -= readLen;
               if (len <= 0) {
                  break;
               }
            }
         }

         return count > 0 ? count : -1;
      } else {
         throw new IndexOutOfBoundsException("Array Size=" + cbuf.length + ", offset=" + off + ", length=" + len);
      }
   }
}
