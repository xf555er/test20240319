package org.apache.commons.io.input.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class PeekableInputStream extends CircularBufferInputStream {
   public PeekableInputStream(InputStream inputStream, int bufferSize) {
      super(inputStream, bufferSize);
   }

   public PeekableInputStream(InputStream inputStream) {
      super(inputStream);
   }

   public boolean peek(byte[] sourceBuffer) throws IOException {
      Objects.requireNonNull(sourceBuffer, "sourceBuffer");
      return this.peek(sourceBuffer, 0, sourceBuffer.length);
   }

   public boolean peek(byte[] sourceBuffer, int offset, int length) throws IOException {
      Objects.requireNonNull(sourceBuffer, "sourceBuffer");
      if (sourceBuffer.length > this.bufferSize) {
         throw new IllegalArgumentException("Peek request size of " + sourceBuffer.length + " bytes exceeds buffer size of " + this.bufferSize + " bytes");
      } else {
         if (this.buffer.getCurrentNumberOfBytes() < sourceBuffer.length) {
            this.fillBuffer();
         }

         return this.buffer.peek(sourceBuffer, offset, length);
      }
   }
}
