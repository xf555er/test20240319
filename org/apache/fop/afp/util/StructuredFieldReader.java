package org.apache.fop.afp.util;

import java.io.IOException;
import java.io.InputStream;

public class StructuredFieldReader {
   private InputStream inputStream;

   public StructuredFieldReader(InputStream inputStream) {
      this.inputStream = inputStream;
   }

   public byte[] getNext(byte[] identifier) throws IOException {
      byte[] bytes = AFPResourceUtil.getNext(identifier, this.inputStream);
      if (bytes != null) {
         int srcPos = 2 + identifier.length;
         byte[] tmp = new byte[bytes.length - srcPos];
         System.arraycopy(bytes, srcPos, tmp, 0, tmp.length);
         bytes = tmp;
      }

      return bytes;
   }
}
