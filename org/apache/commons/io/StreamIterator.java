package org.apache.commons.io;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

class StreamIterator implements Iterator, Closeable {
   private final Iterator iterator;
   private final Stream stream;

   public static Iterator iterator(Stream stream) {
      return (new StreamIterator(stream)).iterator;
   }

   private StreamIterator(Stream stream) {
      this.stream = (Stream)Objects.requireNonNull(stream, "stream");
      this.iterator = stream.iterator();
   }

   public boolean hasNext() {
      boolean hasNext = this.iterator.hasNext();
      if (!hasNext) {
         this.close();
      }

      return hasNext;
   }

   public Object next() {
      Object next = this.iterator.next();
      if (next == null) {
         this.close();
      }

      return next;
   }

   public void close() {
      this.stream.close();
   }
}
