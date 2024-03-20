package org.apache.xmlgraphics.util.io;

import java.io.IOException;
import java.io.InputStream;

public class Base64DecodeStream extends InputStream {
   InputStream src;
   private static final byte[] PEM_ARRAY = new byte[256];
   byte[] decodeBuffer = new byte[4];
   byte[] outBuffer = new byte[3];
   int outOffset = 3;
   boolean eof;

   public Base64DecodeStream(InputStream src) {
      this.src = src;
   }

   public boolean markSupported() {
      return false;
   }

   public void close() throws IOException {
      this.eof = true;
   }

   public int available() throws IOException {
      return 3 - this.outOffset;
   }

   public int read() throws IOException {
      if (this.outOffset != 3 || !this.eof && !this.getNextAtom()) {
         return this.outBuffer[this.outOffset++] & 255;
      } else {
         this.eof = true;
         return -1;
      }
   }

   public int read(byte[] out, int offset, int len) throws IOException {
      int idx;
      for(idx = 0; idx < len; ++idx) {
         if (this.outOffset == 3 && (this.eof || this.getNextAtom())) {
            this.eof = true;
            return idx == 0 ? -1 : idx;
         }

         out[offset + idx] = this.outBuffer[this.outOffset++];
      }

      return idx;
   }

   final boolean getNextAtom() throws IOException {
      int out;
      for(int off = 0; off != 4; off = out) {
         int count = this.src.read(this.decodeBuffer, off, 4 - off);
         if (count == -1) {
            return true;
         }

         int in = off;

         for(out = off; in < off + count; ++in) {
            if (this.decodeBuffer[in] != 10 && this.decodeBuffer[in] != 13 && this.decodeBuffer[in] != 32) {
               this.decodeBuffer[out++] = this.decodeBuffer[in];
            }
         }
      }

      int a = PEM_ARRAY[this.decodeBuffer[0] & 255];
      int b = PEM_ARRAY[this.decodeBuffer[1] & 255];
      int c = PEM_ARRAY[this.decodeBuffer[2] & 255];
      int d = PEM_ARRAY[this.decodeBuffer[3] & 255];
      this.outBuffer[0] = (byte)(a << 2 | b >>> 4);
      this.outBuffer[1] = (byte)(b << 4 | c >>> 2);
      this.outBuffer[2] = (byte)(c << 6 | d);
      if (this.decodeBuffer[3] != 61) {
         this.outOffset = 0;
      } else if (this.decodeBuffer[2] == 61) {
         this.outBuffer[2] = this.outBuffer[0];
         this.outOffset = 2;
         this.eof = true;
      } else {
         this.outBuffer[2] = this.outBuffer[1];
         this.outBuffer[1] = this.outBuffer[0];
         this.outOffset = 1;
         this.eof = true;
      }

      return false;
   }

   static {
      int idx;
      for(idx = 0; idx < PEM_ARRAY.length; ++idx) {
         PEM_ARRAY[idx] = -1;
      }

      idx = 0;

      char c;
      for(c = 'A'; c <= 'Z'; ++c) {
         PEM_ARRAY[c] = (byte)(idx++);
      }

      for(c = 'a'; c <= 'z'; ++c) {
         PEM_ARRAY[c] = (byte)(idx++);
      }

      for(c = '0'; c <= '9'; ++c) {
         PEM_ARRAY[c] = (byte)(idx++);
      }

      PEM_ARRAY[43] = (byte)(idx++);
      PEM_ARRAY[47] = (byte)(idx++);
   }
}
