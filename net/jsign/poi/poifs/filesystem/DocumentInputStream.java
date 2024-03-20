package net.jsign.poi.poifs.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import net.jsign.poi.poifs.property.DocumentProperty;
import net.jsign.poi.util.IOUtils;

public final class DocumentInputStream extends InputStream {
   private int _current_offset;
   private int _current_block_count;
   private int _marked_offset;
   private int _marked_offset_count;
   private final int _document_size;
   private boolean _closed;
   private final POIFSDocument _document;
   private Iterator _data;
   private ByteBuffer _buffer;

   public DocumentInputStream(DocumentEntry document) throws IOException {
      if (!(document instanceof DocumentNode)) {
         throw new IOException("Cannot open internal document storage, " + document + " not a Document Node");
      } else {
         this._current_offset = 0;
         this._current_block_count = 0;
         this._marked_offset = 0;
         this._marked_offset_count = 0;
         this._document_size = document.getSize();
         this._closed = false;
         DocumentNode doc = (DocumentNode)document;
         DocumentProperty property = (DocumentProperty)doc.getProperty();
         this._document = new POIFSDocument(property, ((DirectoryNode)doc.getParent()).getFileSystem());
         this._data = this._document.getBlockIterator();
      }
   }

   public int available() {
      return this.remainingBytes();
   }

   private int remainingBytes() {
      if (this._closed) {
         throw new IllegalStateException("cannot perform requested operation on a closed stream");
      } else {
         return this._document_size - this._current_offset;
      }
   }

   public void close() {
      this._closed = true;
   }

   public boolean markSupported() {
      return true;
   }

   public synchronized void mark(int ignoredReadlimit) {
      this._marked_offset = this._current_offset;
      this._marked_offset_count = Math.max(0, this._current_block_count - 1);
   }

   public int read() throws IOException {
      this.dieIfClosed();
      if (this.atEOD()) {
         return -1;
      } else {
         byte[] b = new byte[1];
         int result = this.read(b, 0, 1);
         return result == -1 ? -1 : b[0] & 255;
      }
   }

   public int read(byte[] b) throws IOException {
      return this.read(b, 0, b.length);
   }

   public int read(byte[] b, int off, int len) throws IOException {
      this.dieIfClosed();
      if (b == null) {
         throw new IllegalArgumentException("buffer must not be null");
      } else if (off >= 0 && len >= 0 && b.length >= off + len) {
         if (len == 0) {
            return 0;
         } else if (this.atEOD()) {
            return -1;
         } else {
            int limit = Math.min(this.remainingBytes(), len);
            this.readFully(b, off, limit);
            return limit;
         }
      } else {
         throw new IndexOutOfBoundsException("can't read past buffer boundaries");
      }
   }

   public synchronized void reset() {
      if (this._marked_offset == 0 && this._marked_offset_count == 0) {
         this._current_block_count = this._marked_offset_count;
         this._current_offset = this._marked_offset;
         this._data = this._document.getBlockIterator();
         this._buffer = null;
      } else {
         this._data = this._document.getBlockIterator();
         this._current_offset = 0;

         int skipBy;
         for(skipBy = 0; skipBy < this._marked_offset_count; ++skipBy) {
            this._buffer = (ByteBuffer)this._data.next();
            this._current_offset += this._buffer.remaining();
         }

         this._current_block_count = this._marked_offset_count;
         if (this._current_offset != this._marked_offset) {
            this._buffer = (ByteBuffer)this._data.next();
            ++this._current_block_count;
            skipBy = this._marked_offset - this._current_offset;
            this._buffer.position(this._buffer.position() + skipBy);
         }

         this._current_offset = this._marked_offset;
      }
   }

   public long skip(long n) throws IOException {
      this.dieIfClosed();
      if (n < 0L) {
         return 0L;
      } else {
         long new_offset = (long)this._current_offset + n;
         if (new_offset < (long)this._current_offset) {
            new_offset = (long)this._document_size;
         } else if (new_offset > (long)this._document_size) {
            new_offset = (long)this._document_size;
         }

         long rval = new_offset - (long)this._current_offset;
         byte[] skip = IOUtils.safelyAllocate(rval, Integer.MAX_VALUE);
         this.readFully(skip);
         return rval;
      }
   }

   private void dieIfClosed() throws IOException {
      if (this._closed) {
         throw new IOException("cannot perform requested operation on a closed stream");
      }
   }

   private boolean atEOD() {
      return this._current_offset == this._document_size;
   }

   private void checkAvaliable(int requestedSize) {
      if (this._closed) {
         throw new IllegalStateException("cannot perform requested operation on a closed stream");
      } else if (requestedSize > this._document_size - this._current_offset) {
         throw new RuntimeException("Buffer underrun - requested " + requestedSize + " bytes but " + (this._document_size - this._current_offset) + " was available");
      }
   }

   public void readFully(byte[] buf) {
      this.readFully(buf, 0, buf.length);
   }

   public void readFully(byte[] buf, int off, int len) {
      if (len < 0) {
         throw new RuntimeException("Can't read negative number of bytes");
      } else {
         this.checkAvaliable(len);

         int limit;
         for(int read = 0; read < len; read += limit) {
            if (this._buffer == null || this._buffer.remaining() == 0) {
               ++this._current_block_count;
               this._buffer = (ByteBuffer)this._data.next();
            }

            limit = Math.min(len - read, this._buffer.remaining());
            this._buffer.get(buf, off + read, limit);
            this._current_offset += limit;
         }

      }
   }
}
