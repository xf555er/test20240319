package net.jsign.poi.poifs.filesystem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import net.jsign.poi.poifs.property.DocumentProperty;
import net.jsign.poi.util.IOUtils;

public final class POIFSDocument implements Iterable {
   private DocumentProperty _property;
   private POIFSFileSystem _filesystem;
   private POIFSStream _stream;
   private int _block_size;

   public POIFSDocument(DocumentNode document) {
      this((DocumentProperty)document.getProperty(), ((DirectoryNode)document.getParent()).getFileSystem());
   }

   public POIFSDocument(DocumentProperty property, POIFSFileSystem filesystem) {
      this._property = property;
      this._filesystem = filesystem;
      if (property.getSize() < 4096) {
         this._stream = new POIFSStream(this._filesystem.getMiniStore(), property.getStartBlock());
         this._block_size = this._filesystem.getMiniStore().getBlockStoreBlockSize();
      } else {
         this._stream = new POIFSStream(this._filesystem, property.getStartBlock());
         this._block_size = this._filesystem.getBlockStoreBlockSize();
      }

   }

   public POIFSDocument(String name, POIFSFileSystem filesystem, InputStream stream) throws IOException {
      this._filesystem = filesystem;
      int length = this.store(stream);
      this._property = new DocumentProperty(name, length);
      this._property.setStartBlock(this._stream.getStartBlock());
      this._property.setDocument(this);
   }

   private int store(InputStream stream) throws IOException {
      int bigBlockSize = true;
      BufferedInputStream bis = new BufferedInputStream(stream, 4097);
      bis.mark(4096);
      long streamBlockSize = IOUtils.skipFully(bis, 4096L);
      if (streamBlockSize < 4096L) {
         this._stream = new POIFSStream(this._filesystem.getMiniStore());
         this._block_size = this._filesystem.getMiniStore().getBlockStoreBlockSize();
      } else {
         this._stream = new POIFSStream(this._filesystem);
         this._block_size = this._filesystem.getBlockStoreBlockSize();
      }

      bis.reset();
      OutputStream os = this._stream.getOutputStream();
      Throwable var9 = null;

      long length;
      try {
         length = IOUtils.copy(bis, os);
         int usedInBlock = (int)(length % (long)this._block_size);
         if (usedInBlock != 0 && usedInBlock != this._block_size) {
            int toBlockEnd = this._block_size - usedInBlock;
            byte[] padding = IOUtils.safelyAllocate((long)toBlockEnd, 100000);
            Arrays.fill(padding, (byte)-1);
            os.write(padding);
         }
      } catch (Throwable var20) {
         var9 = var20;
         throw var20;
      } finally {
         if (os != null) {
            if (var9 != null) {
               try {
                  os.close();
               } catch (Throwable var19) {
                  var9.addSuppressed(var19);
               }
            } else {
               os.close();
            }
         }

      }

      return Math.toIntExact(length);
   }

   void free() throws IOException {
      this._stream.free();
      this._property.setStartBlock(-2);
   }

   public Iterator iterator() {
      return this.getBlockIterator();
   }

   Iterator getBlockIterator() {
      return ((Iterable)(this.getSize() > 0 ? this._stream : Collections.emptyList())).iterator();
   }

   public int getSize() {
      return this._property.getSize();
   }

   public void replaceContents(InputStream stream) throws IOException {
      this.free();
      int size = this.store(stream);
      this._property.setStartBlock(this._stream.getStartBlock());
      this._property.updateSize(size);
   }

   DocumentProperty getDocumentProperty() {
      return this._property;
   }
}
