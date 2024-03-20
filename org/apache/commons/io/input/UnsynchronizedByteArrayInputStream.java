package org.apache.commons.io.input;

import java.io.InputStream;
import java.util.Objects;

public class UnsynchronizedByteArrayInputStream extends InputStream {
   public static final int END_OF_STREAM = -1;
   private final byte[] data;
   private final int eod;
   private int offset;
   private int markedOffset;

   public UnsynchronizedByteArrayInputStream(byte[] data) {
      this.data = (byte[])Objects.requireNonNull(data, "data");
      this.offset = 0;
      this.eod = data.length;
      this.markedOffset = this.offset;
   }

   public UnsynchronizedByteArrayInputStream(byte[] data, int offset) {
      Objects.requireNonNull(data, "data");
      if (offset < 0) {
         throw new IllegalArgumentException("offset cannot be negative");
      } else {
         this.data = data;
         this.offset = Math.min(offset, data.length > 0 ? data.length : offset);
         this.eod = data.length;
         this.markedOffset = this.offset;
      }
   }

   public UnsynchronizedByteArrayInputStream(byte[] data, int offset, int length) {
      if (offset < 0) {
         throw new IllegalArgumentException("offset cannot be negative");
      } else if (length < 0) {
         throw new IllegalArgumentException("length cannot be negative");
      } else {
         this.data = (byte[])Objects.requireNonNull(data, "data");
         this.offset = Math.min(offset, data.length > 0 ? data.length : offset);
         this.eod = Math.min(this.offset + length, data.length);
         this.markedOffset = this.offset;
      }
   }

   public int available() {
      return this.offset < this.eod ? this.eod - this.offset : 0;
   }

   public int read() {
      return this.offset < this.eod ? this.data[this.offset++] & 255 : -1;
   }

   public int read(byte[] dest) {
      Objects.requireNonNull(dest, "dest");
      return this.read(dest, 0, dest.length);
   }

   public int read(byte[] dest, int off, int len) {
      Objects.requireNonNull(dest, "dest");
      if (off >= 0 && len >= 0 && off + len <= dest.length) {
         if (this.offset >= this.eod) {
            return -1;
         } else {
            int actualLen = this.eod - this.offset;
            if (len < actualLen) {
               actualLen = len;
            }

            if (actualLen <= 0) {
               return 0;
            } else {
               System.arraycopy(this.data, this.offset, dest, off, actualLen);
               this.offset += actualLen;
               return actualLen;
            }
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public long skip(long n) {
      if (n < 0L) {
         throw new IllegalArgumentException("Skipping backward is not supported");
      } else {
         long actualSkip = (long)(this.eod - this.offset);
         if (n < actualSkip) {
            actualSkip = n;
         }

         this.offset = (int)((long)this.offset + actualSkip);
         return actualSkip;
      }
   }

   public boolean markSupported() {
      return true;
   }

   public void mark(int readlimit) {
      this.markedOffset = this.offset;
   }

   public void reset() {
      this.offset = this.markedOffset;
   }
}
