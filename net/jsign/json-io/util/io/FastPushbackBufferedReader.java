package net.jsign.json-io.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class FastPushbackBufferedReader extends BufferedReader implements FastPushbackReader {
   private final int[] buf = new int[256];
   private int idx = 0;
   private int unread = Integer.MAX_VALUE;
   protected int line = 1;
   protected int col = 0;

   public FastPushbackBufferedReader(Reader reader) {
      super(reader);
   }

   public String getLastSnippet() {
      StringBuilder s = new StringBuilder();

      int i;
      for(i = this.idx; i < this.buf.length && !this.appendChar(s, i); ++i) {
      }

      for(i = 0; i < this.idx && !this.appendChar(s, i); ++i) {
      }

      return s.toString();
   }

   private boolean appendChar(StringBuilder s, int i) {
      try {
         int snip = this.buf[i];
         if (snip == 0) {
            return true;
         } else {
            s.appendCodePoint(snip);
            return false;
         }
      } catch (Exception var4) {
         return true;
      }
   }

   public int read() throws IOException {
      int ch;
      if (this.unread == Integer.MAX_VALUE) {
         ch = super.read();
      } else {
         ch = this.unread;
         this.unread = Integer.MAX_VALUE;
      }

      if ((this.buf[this.idx++] = ch) == 10) {
         ++this.line;
         this.col = 0;
      } else {
         ++this.col;
      }

      if (this.idx >= this.buf.length) {
         this.idx = 0;
      }

      return ch;
   }

   public void unread(int c) throws IOException {
      if ((this.unread = c) == 10) {
         --this.line;
      } else {
         --this.col;
      }

      if (this.idx < 1) {
         this.idx = this.buf.length - 1;
      } else {
         --this.idx;
      }

   }

   public int getCol() {
      return this.col;
   }

   public int getLine() {
      return this.line;
   }
}
