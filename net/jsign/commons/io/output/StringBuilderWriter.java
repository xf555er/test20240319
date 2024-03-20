package net.jsign.commons.io.output;

import java.io.Serializable;
import java.io.Writer;

public class StringBuilderWriter extends Writer implements Serializable {
   private final StringBuilder builder = new StringBuilder();

   public Writer append(char value) {
      this.builder.append(value);
      return this;
   }

   public Writer append(CharSequence value) {
      this.builder.append(value);
      return this;
   }

   public Writer append(CharSequence value, int start, int end) {
      this.builder.append(value, start, end);
      return this;
   }

   public void close() {
   }

   public void flush() {
   }

   public void write(String value) {
      if (value != null) {
         this.builder.append(value);
      }

   }

   public void write(char[] value, int offset, int length) {
      if (value != null) {
         this.builder.append(value, offset, length);
      }

   }

   public String toString() {
      return this.builder.toString();
   }
}
