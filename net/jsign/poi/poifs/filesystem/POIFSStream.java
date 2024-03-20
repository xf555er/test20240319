package net.jsign.poi.poifs.filesystem;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class POIFSStream implements Iterable {
   private final BlockStore blockStore;
   private int startBlock;
   private OutputStream outStream;

   public POIFSStream(BlockStore blockStore, int startBlock) {
      this.blockStore = blockStore;
      this.startBlock = startBlock;
   }

   public POIFSStream(BlockStore blockStore) {
      this.blockStore = blockStore;
      this.startBlock = -2;
   }

   public int getStartBlock() {
      return this.startBlock;
   }

   public Iterator iterator() {
      return this.getBlockIterator();
   }

   Iterator getBlockIterator() {
      if (this.startBlock == -2) {
         throw new IllegalStateException("Can't read from a new stream before it has been written to");
      } else {
         return new StreamBlockByteBufferIterator(this.startBlock);
      }
   }

   public OutputStream getOutputStream() throws IOException {
      if (this.outStream == null) {
         this.outStream = new StreamBlockByteBuffer();
      }

      return this.outStream;
   }

   public void free() throws IOException {
      BlockStore.ChainLoopDetector loopDetector = this.blockStore.getChainLoopDetector();
      this.free(loopDetector);
   }

   private void free(BlockStore.ChainLoopDetector loopDetector) {
      int nextBlock = this.startBlock;

      while(nextBlock != -2) {
         int thisBlock = nextBlock;
         loopDetector.claim(nextBlock);
         nextBlock = this.blockStore.getNextBlock(nextBlock);
         this.blockStore.setNextBlock(thisBlock, -1);
      }

      this.startBlock = -2;
   }

   protected class StreamBlockByteBuffer extends OutputStream {
      byte[] oneByte = new byte[1];
      ByteBuffer buffer;
      BlockStore.ChainLoopDetector loopDetector;
      int prevBlock;
      int nextBlock;

      StreamBlockByteBuffer() throws IOException {
         this.loopDetector = POIFSStream.this.blockStore.getChainLoopDetector();
         this.prevBlock = -2;
         this.nextBlock = POIFSStream.this.startBlock;
      }

      void createBlockIfNeeded() throws IOException {
         if (this.buffer == null || !this.buffer.hasRemaining()) {
            int thisBlock = this.nextBlock;
            if (thisBlock == -2) {
               thisBlock = POIFSStream.this.blockStore.getFreeBlock();
               this.loopDetector.claim(thisBlock);
               this.nextBlock = -2;
               if (this.prevBlock != -2) {
                  POIFSStream.this.blockStore.setNextBlock(this.prevBlock, thisBlock);
               }

               POIFSStream.this.blockStore.setNextBlock(thisBlock, -2);
               if (POIFSStream.this.startBlock == -2) {
                  POIFSStream.this.startBlock = thisBlock;
               }
            } else {
               this.loopDetector.claim(thisBlock);
               this.nextBlock = POIFSStream.this.blockStore.getNextBlock(thisBlock);
            }

            if (this.buffer != null) {
               POIFSStream.this.blockStore.releaseBuffer(this.buffer);
            }

            this.buffer = POIFSStream.this.blockStore.createBlockIfNeeded(thisBlock);
            this.prevBlock = thisBlock;
         }
      }

      public void write(int b) throws IOException {
         this.oneByte[0] = (byte)(b & 255);
         this.write(this.oneByte);
      }

      public void write(byte[] b, int off, int len) throws IOException {
         if (off >= 0 && off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
            if (len != 0) {
               do {
                  this.createBlockIfNeeded();
                  int writeBytes = Math.min(this.buffer.remaining(), len);
                  this.buffer.put(b, off, writeBytes);
                  off += writeBytes;
                  len -= writeBytes;
               } while(len > 0);

            }
         } else {
            throw new IndexOutOfBoundsException();
         }
      }

      public void close() throws IOException {
         POIFSStream toFree = new POIFSStream(POIFSStream.this.blockStore, this.nextBlock);
         toFree.free(this.loopDetector);
         if (this.prevBlock != -2) {
            POIFSStream.this.blockStore.setNextBlock(this.prevBlock, -2);
         }

      }
   }

   private class StreamBlockByteBufferIterator implements Iterator {
      private final BlockStore.ChainLoopDetector loopDetector;
      private int nextBlock;

      StreamBlockByteBufferIterator(int firstBlock) {
         this.nextBlock = firstBlock;

         try {
            this.loopDetector = POIFSStream.this.blockStore.getChainLoopDetector();
         } catch (IOException var4) {
            throw new RuntimeException(var4);
         }
      }

      public boolean hasNext() {
         return this.nextBlock != -2;
      }

      public ByteBuffer next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException("Can't read past the end of the stream");
         } else {
            try {
               this.loopDetector.claim(this.nextBlock);
               ByteBuffer data = POIFSStream.this.blockStore.getBlockAt(this.nextBlock);
               this.nextBlock = POIFSStream.this.blockStore.getNextBlock(this.nextBlock);
               return data;
            } catch (IOException var2) {
               throw new RuntimeException(var2);
            }
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }
}
