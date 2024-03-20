package org.apache.commons.io.input;

import java.io.Reader;
import java.io.Serializable;
import java.util.Objects;

public class CharSequenceReader extends Reader implements Serializable {
   private static final long serialVersionUID = 3724187752191401220L;
   private final CharSequence charSequence;
   private int idx;
   private int mark;
   private final int start;
   private final Integer end;

   public CharSequenceReader(CharSequence charSequence) {
      this(charSequence, 0);
   }

   public CharSequenceReader(CharSequence charSequence, int start) {
      this(charSequence, start, Integer.MAX_VALUE);
   }

   public CharSequenceReader(CharSequence charSequence, int start, int end) {
      if (start < 0) {
         throw new IllegalArgumentException("Start index is less than zero: " + start);
      } else if (end < start) {
         throw new IllegalArgumentException("End index is less than start " + start + ": " + end);
      } else {
         this.charSequence = (CharSequence)(charSequence != null ? charSequence : "");
         this.start = start;
         this.end = end;
         this.idx = start;
         this.mark = start;
      }
   }

   private int start() {
      return Math.min(this.charSequence.length(), this.start);
   }

   private int end() {
      return Math.min(this.charSequence.length(), this.end == null ? Integer.MAX_VALUE : this.end);
   }

   public void close() {
      this.idx = this.start;
      this.mark = this.start;
   }

   public boolean ready() {
      return this.idx < this.end();
   }

   public void mark(int readAheadLimit) {
      this.mark = this.idx;
   }

   public boolean markSupported() {
      return true;
   }

   public int read() {
      return this.idx >= this.end() ? -1 : this.charSequence.charAt(this.idx++);
   }

   public int read(char[] array, int offset, int length) {
      if (this.idx >= this.end()) {
         return -1;
      } else {
         Objects.requireNonNull(array, "array");
         if (length >= 0 && offset >= 0 && offset + length <= array.length) {
            int count;
            if (this.charSequence instanceof String) {
               count = Math.min(length, this.end() - this.idx);
               ((String)this.charSequence).getChars(this.idx, this.idx + count, array, offset);
               this.idx += count;
               return count;
            } else if (this.charSequence instanceof StringBuilder) {
               count = Math.min(length, this.end() - this.idx);
               ((StringBuilder)this.charSequence).getChars(this.idx, this.idx + count, array, offset);
               this.idx += count;
               return count;
            } else if (this.charSequence instanceof StringBuffer) {
               count = Math.min(length, this.end() - this.idx);
               ((StringBuffer)this.charSequence).getChars(this.idx, this.idx + count, array, offset);
               this.idx += count;
               return count;
            } else {
               count = 0;

               for(int i = 0; i < length; ++i) {
                  int c = this.read();
                  if (c == -1) {
                     return count;
                  }

                  array[offset + i] = (char)c;
                  ++count;
               }

               return count;
            }
         } else {
            throw new IndexOutOfBoundsException("Array Size=" + array.length + ", offset=" + offset + ", length=" + length);
         }
      }
   }

   public void reset() {
      this.idx = this.mark;
   }

   public long skip(long n) {
      if (n < 0L) {
         throw new IllegalArgumentException("Number of characters to skip is less than zero: " + n);
      } else if (this.idx >= this.end()) {
         return 0L;
      } else {
         int dest = (int)Math.min((long)this.end(), (long)this.idx + n);
         int count = dest - this.idx;
         this.idx = dest;
         return (long)count;
      }
   }

   public String toString() {
      CharSequence subSequence = this.charSequence.subSequence(this.start(), this.end());
      return subSequence.toString();
   }
}
