package org.apache.fop.fo;

import java.util.NoSuchElementException;

public class NullCharIterator extends CharIterator {
   private static CharIterator instance = new NullCharIterator();

   public static CharIterator getInstance() {
      return instance;
   }

   public boolean hasNext() {
      return false;
   }

   public char nextChar() throws NoSuchElementException {
      throw new NoSuchElementException();
   }
}
