package org.apache.batik.util.io;

import java.io.IOException;
import java.io.InputStream;

public class UTF8Decoder extends AbstractCharDecoder {
   protected static final byte[] UTF8_BYTES = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0};
   protected int nextChar = -1;

   public UTF8Decoder(InputStream is) {
      super(is);
   }

   public int readChar() throws IOException {
      int b1;
      if (this.nextChar != -1) {
         b1 = this.nextChar;
         this.nextChar = -1;
         return b1;
      } else {
         if (this.position == this.count) {
            this.fillBuffer();
         }

         if (this.count == -1) {
            return -1;
         } else {
            b1 = this.buffer[this.position++] & 255;
            byte b2;
            byte b3;
            switch (UTF8_BYTES[b1]) {
               case 2:
                  if (this.position == this.count) {
                     this.fillBuffer();
                  }

                  if (this.count == -1) {
                     this.endOfStreamError("UTF-8");
                  }

                  return (b1 & 31) << 6 | this.buffer[this.position++] & 63;
               case 3:
                  if (this.position == this.count) {
                     this.fillBuffer();
                  }

                  if (this.count == -1) {
                     this.endOfStreamError("UTF-8");
                  }

                  b2 = this.buffer[this.position++];
                  if (this.position == this.count) {
                     this.fillBuffer();
                  }

                  if (this.count == -1) {
                     this.endOfStreamError("UTF-8");
                  }

                  b3 = this.buffer[this.position++];
                  if ((b2 & 192) != 128 || (b3 & 192) != 128) {
                     this.charError("UTF-8");
                  }

                  return (b1 & 31) << 12 | (b2 & 63) << 6 | b3 & 31;
               case 4:
                  if (this.position == this.count) {
                     this.fillBuffer();
                  }

                  if (this.count == -1) {
                     this.endOfStreamError("UTF-8");
                  }

                  b2 = this.buffer[this.position++];
                  if (this.position == this.count) {
                     this.fillBuffer();
                  }

                  if (this.count == -1) {
                     this.endOfStreamError("UTF-8");
                  }

                  b3 = this.buffer[this.position++];
                  if (this.position == this.count) {
                     this.fillBuffer();
                  }

                  if (this.count == -1) {
                     this.endOfStreamError("UTF-8");
                  }

                  int b4 = this.buffer[this.position++];
                  if ((b2 & 192) != 128 || (b3 & 192) != 128 || (b4 & 192) != 128) {
                     this.charError("UTF-8");
                  }

                  int c = (b1 & 31) << 18 | (b2 & 63) << 12 | (b3 & 31) << 6 | b4 & 31;
                  this.nextChar = (c - 65536) % 1024 + '\udc00';
                  return (c - 65536) / 1024 + '\ud800';
               default:
                  this.charError("UTF-8");
               case 1:
                  return b1;
            }
         }
      }
   }
}
