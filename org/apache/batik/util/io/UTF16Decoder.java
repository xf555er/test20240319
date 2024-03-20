package org.apache.batik.util.io;

import java.io.IOException;
import java.io.InputStream;

public class UTF16Decoder extends AbstractCharDecoder {
   protected boolean bigEndian;

   public UTF16Decoder(InputStream is) throws IOException {
      super(is);
      int b1 = is.read();
      if (b1 == -1) {
         this.endOfStreamError("UTF-16");
      }

      int b2 = is.read();
      if (b2 == -1) {
         this.endOfStreamError("UTF-16");
      }

      int m = (b1 & 255) << 8 | b2 & 255;
      switch (m) {
         case 65279:
            this.bigEndian = true;
         case 65534:
            break;
         default:
            this.charError("UTF-16");
      }

   }

   public UTF16Decoder(InputStream is, boolean be) {
      super(is);
      this.bigEndian = be;
   }

   public int readChar() throws IOException {
      if (this.position == this.count) {
         this.fillBuffer();
      }

      if (this.count == -1) {
         return -1;
      } else {
         byte b1 = this.buffer[this.position++];
         if (this.position == this.count) {
            this.fillBuffer();
         }

         if (this.count == -1) {
            this.endOfStreamError("UTF-16");
         }

         byte b2 = this.buffer[this.position++];
         int c = this.bigEndian ? (b1 & 255) << 8 | b2 & 255 : (b2 & 255) << 8 | b1 & 255;
         if (c == 65534) {
            this.charError("UTF-16");
         }

         return c;
      }
   }
}
