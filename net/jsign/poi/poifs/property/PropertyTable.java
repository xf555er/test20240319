package net.jsign.poi.poifs.property;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import net.jsign.poi.poifs.common.POIFSBigBlockSize;
import net.jsign.poi.poifs.filesystem.POIFSFileSystem;
import net.jsign.poi.poifs.filesystem.POIFSStream;
import net.jsign.poi.poifs.storage.HeaderBlock;
import net.jsign.poi.util.IOUtils;
import net.jsign.poi.util.POILogFactory;
import net.jsign.poi.util.POILogger;

public final class PropertyTable {
   private static final POILogger _logger = POILogFactory.getLogger(PropertyTable.class);
   private final HeaderBlock _header_block;
   private final List _properties;
   private final POIFSBigBlockSize _bigBigBlockSize;

   public PropertyTable(HeaderBlock headerBlock) {
      this._properties = new ArrayList();
      this._header_block = headerBlock;
      this._bigBigBlockSize = headerBlock.getBigBlockSize();
      this.addProperty(new RootProperty());
   }

   public PropertyTable(HeaderBlock headerBlock, POIFSFileSystem filesystem) throws IOException {
      this(headerBlock, (Iterable)(new POIFSStream(filesystem, headerBlock.getPropertyStart())));
   }

   PropertyTable(HeaderBlock headerBlock, Iterable dataSource) throws IOException {
      this._properties = new ArrayList();
      this._header_block = headerBlock;
      this._bigBigBlockSize = headerBlock.getBigBlockSize();

      byte[] data;
      for(Iterator var3 = dataSource.iterator(); var3.hasNext(); PropertyFactory.convertToProperties(data, this._properties)) {
         ByteBuffer bb = (ByteBuffer)var3.next();
         if (bb.hasArray() && bb.arrayOffset() == 0 && bb.array().length == this._bigBigBlockSize.getBigBlockSize()) {
            data = bb.array();
         } else {
            data = IOUtils.safelyAllocate((long)this._bigBigBlockSize.getBigBlockSize(), 100000);
            int toRead = data.length;
            if (bb.remaining() < this._bigBigBlockSize.getBigBlockSize()) {
               _logger.log(5, "Short Property Block, ", bb.remaining(), " bytes instead of the expected " + this._bigBigBlockSize.getBigBlockSize());
               toRead = bb.remaining();
            }

            bb.get(data, 0, toRead);
         }
      }

      this.populatePropertyTree((DirectoryProperty)this._properties.get(0));
   }

   public void addProperty(Property property) {
      this._properties.add(property);
   }

   public RootProperty getRoot() {
      return (RootProperty)this._properties.get(0);
   }

   public int getStartBlock() {
      return this._header_block.getPropertyStart();
   }

   public void setStartBlock(int index) {
      this._header_block.setPropertyStart(index);
   }

   public void preWrite() {
      List pList = new ArrayList();
      int i = 0;
      Iterator var3 = this._properties.iterator();

      Property p;
      while(var3.hasNext()) {
         p = (Property)var3.next();
         if (p != null) {
            p.setIndex(i++);
            pList.add(p);
         }
      }

      var3 = pList.iterator();

      while(var3.hasNext()) {
         p = (Property)var3.next();
         p.preWrite();
      }

   }

   public void write(POIFSStream stream) throws IOException {
      OutputStream os = stream.getOutputStream();
      Iterator var3 = this._properties.iterator();

      while(var3.hasNext()) {
         Property property = (Property)var3.next();
         if (property != null) {
            property.writeData(os);
         }
      }

      os.close();
      if (this.getStartBlock() != stream.getStartBlock()) {
         this.setStartBlock(stream.getStartBlock());
      }

   }

   private void populatePropertyTree(DirectoryProperty root) throws IOException {
      int index = root.getChildIndex();
      if (Property.isValidIndex(index)) {
         Stack children = new Stack();
         children.push(this._properties.get(index));

         while(!children.empty()) {
            Property property = (Property)children.pop();
            if (property != null) {
               root.addChild(property);
               if (property.isDirectory()) {
                  this.populatePropertyTree((DirectoryProperty)property);
               }

               index = property.getPreviousChildIndex();
               if (this.isValidIndex(index)) {
                  children.push(this._properties.get(index));
               }

               index = property.getNextChildIndex();
               if (this.isValidIndex(index)) {
                  children.push(this._properties.get(index));
               }
            }
         }

      }
   }

   private boolean isValidIndex(int index) {
      if (!Property.isValidIndex(index)) {
         return false;
      } else if (index >= 0 && index < this._properties.size()) {
         return true;
      } else {
         _logger.log(5, "Property index " + index + "outside the valid range 0.." + this._properties.size());
         return false;
      }
   }
}
