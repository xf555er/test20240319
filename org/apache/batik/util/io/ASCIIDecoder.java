package org.apache.batik.util.io;

import java.io.IOException;
import java.io.InputStream;

public class ASCIIDecoder extends AbstractCharDecoder {
   public ASCIIDecoder(InputStream is) {
      super(is);
   }

   public int readChar() throws IOException {
      if (this.position == this.count) {
         this.fillBuffer();
      }

      if (this.count == -1) {
         return -1;
      } else {
         int result = this.buffer[this.position++];
         if (result < 0) {
            this.charError("ASCII");
         }

         return result;
      }
   }
}
