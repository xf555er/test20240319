package net.jsign.commons.io.output;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.jsign.commons.io.IOUtils;

public abstract class AbstractByteArrayOutputStream extends OutputStream {
   private final List buffers = new ArrayList();
   private int currentBufferIndex;
   private int filledBufferSum;
   private byte[] currentBuffer;
   protected int count;
   private boolean reuseBuffers = true;

   protected void needNewBuffer(int newcount) {
      if (this.currentBufferIndex < this.buffers.size() - 1) {
         this.filledBufferSum += this.currentBuffer.length;
         ++this.currentBufferIndex;
         this.currentBuffer = (byte[])this.buffers.get(this.currentBufferIndex);
      } else {
         int newBufferSize;
         if (this.currentBuffer == null) {
            newBufferSize = newcount;
            this.filledBufferSum = 0;
         } else {
            newBufferSize = Math.max(this.currentBuffer.length << 1, newcount - this.filledBufferSum);
            this.filledBufferSum += this.currentBuffer.length;
         }

         ++this.currentBufferIndex;
         this.currentBuffer = IOUtils.byteArray(newBufferSize);
         this.buffers.add(this.currentBuffer);
      }

   }

   protected void writeImpl(byte[] b, int off, int len) {
      int newcount = this.count + len;
      int remaining = len;
      int inBufferPos = this.count - this.filledBufferSum;

      while(remaining > 0) {
         int part = Math.min(remaining, this.currentBuffer.length - inBufferPos);
         System.arraycopy(b, off + len - remaining, this.currentBuffer, inBufferPos, part);
         remaining -= part;
         if (remaining > 0) {
            this.needNewBuffer(newcount);
            inBufferPos = 0;
         }
      }

      this.count = newcount;
   }

   protected void writeImpl(int b) {
      int inBufferPos = this.count - this.filledBufferSum;
      if (inBufferPos == this.currentBuffer.length) {
         this.needNewBuffer(this.count + 1);
         inBufferPos = 0;
      }

      this.currentBuffer[inBufferPos] = (byte)b;
      ++this.count;
   }

   public void close() throws IOException {
   }

   public abstract byte[] toByteArray();

   protected byte[] toByteArrayImpl() {
      int remaining = this.count;
      if (remaining == 0) {
         return IOUtils.EMPTY_BYTE_ARRAY;
      } else {
         byte[] newbuf = IOUtils.byteArray(remaining);
         int pos = 0;
         Iterator var4 = this.buffers.iterator();

         while(var4.hasNext()) {
            byte[] buf = (byte[])var4.next();
            int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
               break;
            }
         }

         return newbuf;
      }
   }

   /** @deprecated */
   @Deprecated
   public String toString() {
      return new String(this.toByteArray(), Charset.defaultCharset());
   }
}
