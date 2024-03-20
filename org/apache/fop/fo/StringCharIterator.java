package org.apache.fop.fo;

import java.util.NoSuchElementException;

public class StringCharIterator extends CharIterator {
   private int index = -1;
   private String str;

   public StringCharIterator(String s) {
      this.str = s;
   }

   public boolean hasNext() {
      return this.index + 1 < this.str.length();
   }

   public char nextChar() throws NoSuchElementException {
      if (this.index + 1 < this.str.length()) {
         return this.str.charAt(++this.index);
      } else {
         throw new NoSuchElementException();
      }
   }
}
