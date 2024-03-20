package net.jsign.poi.poifs.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.jsign.commons.math3.util.ArithmeticUtils;
import net.jsign.poi.EmptyFileException;
import net.jsign.poi.poifs.common.POIFSBigBlockSize;
import net.jsign.poi.poifs.common.POIFSConstants;
import net.jsign.poi.poifs.nio.ByteArrayBackedDataSource;
import net.jsign.poi.poifs.nio.DataSource;
import net.jsign.poi.poifs.nio.FileBackedDataSource;
import net.jsign.poi.poifs.property.PropertyTable;
import net.jsign.poi.poifs.storage.BATBlock;
import net.jsign.poi.poifs.storage.HeaderBlock;
import net.jsign.poi.util.IOUtils;
import net.jsign.poi.util.POILogFactory;
import net.jsign.poi.util.POILogger;

public class POIFSFileSystem extends BlockStore implements Closeable {
   private static final POILogger LOG = POILogFactory.getLogger(POIFSFileSystem.class);
   private POIFSMiniStore _mini_store;
   private PropertyTable _property_table;
   private final List _xbat_blocks;
   private final List _bat_blocks;
   private HeaderBlock _header;
   private DirectoryNode _root;
   protected DataSource _data;
   private POIFSBigBlockSize bigBlockSize;

   private POIFSFileSystem(boolean newFS) {
      this.bigBlockSize = POIFSConstants.SMALLER_BIG_BLOCK_SIZE_DETAILS;
      this._header = new HeaderBlock(this.bigBlockSize);
      this._property_table = new PropertyTable(this._header);
      this._mini_store = new POIFSMiniStore(this, this._property_table.getRoot(), new ArrayList(), this._header);
      this._xbat_blocks = new ArrayList();
      this._bat_blocks = new ArrayList();
      this._root = null;
      if (newFS) {
         this.createNewDataSource();
      }

   }

   protected void createNewDataSource() {
      long blockSize = ArithmeticUtils.mulAndCheck((long)this.bigBlockSize.getBigBlockSize(), 3L);
      this._data = new ByteArrayBackedDataSource(IOUtils.safelyAllocate(blockSize, 100000));
   }

   public POIFSFileSystem() {
      this(true);
      this._header.setBATCount(1);
      this._header.setBATArray(new int[]{1});
      BATBlock bb = BATBlock.createEmptyBATBlock(this.bigBlockSize, false);
      bb.setOurBlockIndex(1);
      this._bat_blocks.add(bb);
      this.setNextBlock(0, -2);
      this.setNextBlock(1, -3);
      this._property_table.setStartBlock(0);
   }

   public POIFSFileSystem(File file, boolean readOnly) throws IOException {
      this((FileChannel)null, file, readOnly, true);
   }

   private POIFSFileSystem(FileChannel channel, File srcFile, boolean readOnly, boolean closeChannelOnError) throws IOException {
      this(false);

      try {
         if (srcFile != null) {
            if (srcFile.length() == 0L) {
               throw new EmptyFileException(srcFile);
            }

            FileBackedDataSource d = new FileBackedDataSource(srcFile, readOnly);
            channel = d.getChannel();
            this._data = d;
         } else {
            this._data = new FileBackedDataSource(channel, readOnly);
         }

         ByteBuffer headerBuffer = ByteBuffer.allocate(512);
         IOUtils.readFully(channel, headerBuffer);
         this._header = new HeaderBlock(headerBuffer);
         this.readCoreContents();
      } catch (RuntimeException | IOException var6) {
         if (closeChannelOnError && channel != null) {
            channel.close();
         }

         throw var6;
      }
   }

   public POIFSFileSystem(InputStream stream) throws IOException {
      this(false);
      boolean success = false;

      try {
         ReadableByteChannel channel = Channels.newChannel(stream);
         Throwable var4 = null;

         try {
            ByteBuffer headerBuffer = ByteBuffer.allocate(512);
            IOUtils.readFully(channel, headerBuffer);
            this._header = new HeaderBlock(headerBuffer);
            sanityCheckBlockCount(this._header.getBATCount());
            long maxSize = BATBlock.calculateMaximumSize(this._header);
            if (maxSize > 2147483647L) {
               throw new IllegalArgumentException("Unable read a >2gb file via an InputStream");
            }

            ByteBuffer data = ByteBuffer.allocate((int)maxSize);
            headerBuffer.position(0);
            data.put(headerBuffer);
            data.position(headerBuffer.capacity());
            IOUtils.readFully(channel, data);
            success = true;
            this._data = new ByteArrayBackedDataSource(data.array(), data.position());
         } catch (Throwable var23) {
            var4 = var23;
            throw var23;
         } finally {
            if (channel != null) {
               if (var4 != null) {
                  try {
                     channel.close();
                  } catch (Throwable var22) {
                     var4.addSuppressed(var22);
                  }
               } else {
                  channel.close();
               }
            }

         }
      } finally {
         this.closeInputStream(stream, success);
      }

      this.readCoreContents();
   }

   private void closeInputStream(InputStream stream, boolean success) {
      try {
         stream.close();
      } catch (IOException var4) {
         if (success) {
            throw new RuntimeException(var4);
         }

         LOG.log(7, "can't close input stream", var4);
      }

   }

   private void readCoreContents() throws IOException {
      this.bigBlockSize = this._header.getBigBlockSize();
      BlockStore.ChainLoopDetector loopDetector = this.getChainLoopDetector();
      int[] var2 = this._header.getBATArray();
      int var3 = var2.length;

      int nextAt;
      int i;
      for(nextAt = 0; nextAt < var3; ++nextAt) {
         i = var2[nextAt];
         this.readBAT(i, loopDetector);
      }

      int remainingFATs = this._header.getBATCount() - this._header.getBATArray().length;
      nextAt = this._header.getXBATIndex();

      int xbatFATs;
      for(i = 0; i < this._header.getXBATCount(); ++i) {
         loopDetector.claim(nextAt);
         ByteBuffer fatData = this.getBlockAt(nextAt);
         BATBlock xfat = BATBlock.createBATBlock(this.bigBlockSize, fatData);
         xfat.setOurBlockIndex(nextAt);
         nextAt = xfat.getValueAt(this.bigBlockSize.getXBATEntriesPerBlock());
         this._xbat_blocks.add(xfat);
         xbatFATs = Math.min(remainingFATs, this.bigBlockSize.getXBATEntriesPerBlock());

         for(int j = 0; j < xbatFATs; ++j) {
            int fatAt = xfat.getValueAt(j);
            if (fatAt == -1 || fatAt == -2) {
               break;
            }

            this.readBAT(fatAt, loopDetector);
         }

         remainingFATs -= xbatFATs;
      }

      this._property_table = new PropertyTable(this._header, this);
      List sbats = new ArrayList();
      this._mini_store = new POIFSMiniStore(this, this._property_table.getRoot(), sbats, this._header);
      nextAt = this._header.getSBATStart();

      for(xbatFATs = 0; xbatFATs < this._header.getSBATCount() && nextAt != -2; ++xbatFATs) {
         loopDetector.claim(nextAt);
         ByteBuffer fatData = this.getBlockAt(nextAt);
         BATBlock sfat = BATBlock.createBATBlock(this.bigBlockSize, fatData);
         sfat.setOurBlockIndex(nextAt);
         sbats.add(sfat);
         nextAt = this.getNextBlock(nextAt);
      }

   }

   private void readBAT(int batAt, BlockStore.ChainLoopDetector loopDetector) throws IOException {
      loopDetector.claim(batAt);
      ByteBuffer fatData = this.getBlockAt(batAt);
      BATBlock bat = BATBlock.createBATBlock(this.bigBlockSize, fatData);
      bat.setOurBlockIndex(batAt);
      this._bat_blocks.add(bat);
   }

   private BATBlock createBAT(int offset, boolean isBAT) throws IOException {
      BATBlock newBAT = BATBlock.createEmptyBATBlock(this.bigBlockSize, !isBAT);
      newBAT.setOurBlockIndex(offset);
      ByteBuffer buffer = ByteBuffer.allocate(this.bigBlockSize.getBigBlockSize());
      long writeTo = ArithmeticUtils.mulAndCheck(1L + (long)offset, (long)this.bigBlockSize.getBigBlockSize());
      this._data.write(buffer, writeTo);
      return newBAT;
   }

   protected ByteBuffer getBlockAt(int offset) throws IOException {
      long blockWanted = (long)offset + 1L;
      long startAt = blockWanted * (long)this.bigBlockSize.getBigBlockSize();

      try {
         return this._data.read(this.bigBlockSize.getBigBlockSize(), startAt);
      } catch (IndexOutOfBoundsException var8) {
         IndexOutOfBoundsException wrapped = new IndexOutOfBoundsException("Block " + offset + " not found");
         wrapped.initCause(var8);
         throw wrapped;
      }
   }

   protected ByteBuffer createBlockIfNeeded(int offset) throws IOException {
      try {
         return this.getBlockAt(offset);
      } catch (IndexOutOfBoundsException var6) {
         long startAt = ((long)offset + 1L) * (long)this.bigBlockSize.getBigBlockSize();
         ByteBuffer buffer = ByteBuffer.allocate(this.getBigBlockSize());
         this._data.write(buffer, startAt);
         return this.getBlockAt(offset);
      }
   }

   protected BATBlock.BATBlockAndIndex getBATBlockAndIndex(int offset) {
      return BATBlock.getBATBlockAndIndex(offset, this._header, this._bat_blocks);
   }

   protected int getNextBlock(int offset) {
      BATBlock.BATBlockAndIndex bai = this.getBATBlockAndIndex(offset);
      return bai.getBlock().getValueAt(bai.getIndex());
   }

   protected void setNextBlock(int offset, int nextBlock) {
      BATBlock.BATBlockAndIndex bai = this.getBATBlockAndIndex(offset);
      bai.getBlock().setValueAt(bai.getIndex(), nextBlock);
   }

   protected int getFreeBlock() throws IOException {
      int numSectors = this.bigBlockSize.getBATEntriesPerBlock();
      int offset = 0;

      BATBlock xbat;
      int i;
      for(Iterator var3 = this._bat_blocks.iterator(); var3.hasNext(); offset += numSectors) {
         xbat = (BATBlock)var3.next();
         if (xbat.hasFreeSectors()) {
            for(i = 0; i < numSectors; ++i) {
               int batValue = xbat.getValueAt(i);
               if (batValue == -1) {
                  return offset + i;
               }
            }
         }
      }

      BATBlock bat = this.createBAT(offset, true);
      bat.setValueAt(0, -3);
      this._bat_blocks.add(bat);
      if (this._header.getBATCount() >= 109) {
         xbat = null;
         Iterator var9 = this._xbat_blocks.iterator();

         while(var9.hasNext()) {
            BATBlock x = (BATBlock)var9.next();
            if (x.hasFreeSectors()) {
               xbat = x;
               break;
            }
         }

         if (xbat == null) {
            xbat = this.createBAT(offset + 1, false);
            xbat.setValueAt(0, offset);
            bat.setValueAt(1, -4);
            ++offset;
            if (this._xbat_blocks.size() == 0) {
               this._header.setXBATStart(offset);
            } else {
               ((BATBlock)this._xbat_blocks.get(this._xbat_blocks.size() - 1)).setValueAt(this.bigBlockSize.getXBATEntriesPerBlock(), offset);
            }

            this._xbat_blocks.add(xbat);
            this._header.setXBATCount(this._xbat_blocks.size());
         } else {
            for(i = 0; i < this.bigBlockSize.getXBATEntriesPerBlock(); ++i) {
               if (xbat.getValueAt(i) == -1) {
                  xbat.setValueAt(i, offset);
                  break;
               }
            }
         }
      } else {
         int[] newBATs = new int[this._header.getBATCount() + 1];
         System.arraycopy(this._header.getBATArray(), 0, newBATs, 0, newBATs.length - 1);
         newBATs[newBATs.length - 1] = offset;
         this._header.setBATArray(newBATs);
      }

      this._header.setBATCount(this._bat_blocks.size());
      return offset + 1;
   }

   protected BlockStore.ChainLoopDetector getChainLoopDetector() throws IOException {
      return new BlockStore.ChainLoopDetector(this._data.size());
   }

   PropertyTable _get_property_table() {
      return this._property_table;
   }

   POIFSMiniStore getMiniStore() {
      return this._mini_store;
   }

   void addDocument(POIFSDocument document) {
      this._property_table.addProperty(document.getDocumentProperty());
   }

   public void writeFilesystem() throws IOException {
      if (!(this._data instanceof FileBackedDataSource)) {
         throw new IllegalArgumentException("POIFS opened from an inputstream, so writeFilesystem() may not be called. Use writeFilesystem(OutputStream) instead");
      } else if (!((FileBackedDataSource)this._data).isWriteable()) {
         throw new IllegalArgumentException("POIFS opened in read only mode, so writeFilesystem() may not be called. Open the FileSystem in read-write mode first");
      } else {
         this.syncWithDataSource();
      }
   }

   public void writeFilesystem(OutputStream stream) throws IOException {
      this.syncWithDataSource();
      this._data.copyTo(stream);
   }

   private void syncWithDataSource() throws IOException {
      this._mini_store.syncWithDataSource();
      POIFSStream propStream = new POIFSStream(this, this._header.getPropertyStart());
      this._property_table.preWrite();
      this._property_table.write(propStream);
      ByteArrayOutputStream baos = new ByteArrayOutputStream(this._header.getBigBlockSize().getBigBlockSize());
      this._header.writeData(baos);
      this.getBlockAt(-1).put(baos.toByteArray());
      Iterator var3 = this._bat_blocks.iterator();

      BATBlock bat;
      ByteBuffer block;
      while(var3.hasNext()) {
         bat = (BATBlock)var3.next();
         block = this.getBlockAt(bat.getOurBlockIndex());
         bat.writeData(block);
      }

      var3 = this._xbat_blocks.iterator();

      while(var3.hasNext()) {
         bat = (BATBlock)var3.next();
         block = this.getBlockAt(bat.getOurBlockIndex());
         bat.writeData(block);
      }

   }

   public void close() throws IOException {
      this._data.close();
   }

   public DirectoryNode getRoot() {
      if (this._root == null) {
         this._root = new DirectoryNode(this._property_table.getRoot(), this, (DirectoryNode)null);
      }

      return this._root;
   }

   public int getBigBlockSize() {
      return this.bigBlockSize.getBigBlockSize();
   }

   public POIFSBigBlockSize getBigBlockSizeDetails() {
      return this.bigBlockSize;
   }

   protected int getBlockStoreBlockSize() {
      return this.getBigBlockSize();
   }

   public PropertyTable getPropertyTable() {
      return this._property_table;
   }

   protected void releaseBuffer(ByteBuffer buffer) {
      if (this._data instanceof FileBackedDataSource) {
         ((FileBackedDataSource)this._data).releaseBuffer(buffer);
      }

   }

   private static void sanityCheckBlockCount(int block_count) throws IOException {
      if (block_count <= 0) {
         throw new IOException("Illegal block count; minimum count is 1, got " + block_count + " instead");
      } else if (block_count > 65535) {
         throw new IOException("Block count " + block_count + " is too high. POI maximum is " + '\uffff' + ".");
      }
   }
}
