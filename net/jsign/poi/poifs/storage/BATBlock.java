package net.jsign.poi.poifs.storage;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import net.jsign.poi.poifs.common.POIFSBigBlockSize;
import net.jsign.poi.util.LittleEndian;

public final class BATBlock {
   private POIFSBigBlockSize bigBlockSize;
   private int[] _values;
   private boolean _has_free_sectors;
   private int ourBlockIndex;

   private BATBlock(POIFSBigBlockSize bigBlockSize) {
      this.bigBlockSize = bigBlockSize;
      int _entries_per_block = bigBlockSize.getBATEntriesPerBlock();
      this._values = new int[_entries_per_block];
      this._has_free_sectors = true;
      Arrays.fill(this._values, -1);
   }

   private void recomputeFree() {
      boolean hasFree = false;
      int[] var2 = this._values;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         int _value = var2[var4];
         if (_value == -1) {
            hasFree = true;
            break;
         }
      }

      this._has_free_sectors = hasFree;
   }

   public static BATBlock createBATBlock(POIFSBigBlockSize bigBlockSize, ByteBuffer data) {
      BATBlock block = new BATBlock(bigBlockSize);
      byte[] buffer = new byte[4];

      for(int i = 0; i < block._values.length; ++i) {
         data.get(buffer);
         block._values[i] = LittleEndian.getInt(buffer);
      }

      block.recomputeFree();
      return block;
   }

   public static BATBlock createEmptyBATBlock(POIFSBigBlockSize bigBlockSize, boolean isXBAT) {
      BATBlock block = new BATBlock(bigBlockSize);
      if (isXBAT) {
         int _entries_per_xbat_block = bigBlockSize.getXBATEntriesPerBlock();
         block._values[_entries_per_xbat_block] = -2;
      }

      return block;
   }

   public static long calculateMaximumSize(POIFSBigBlockSize bigBlockSize, int numBATs) {
      long size = 1L;
      size += (long)numBATs * (long)bigBlockSize.getBATEntriesPerBlock();
      return size * (long)bigBlockSize.getBigBlockSize();
   }

   public static long calculateMaximumSize(HeaderBlock header) {
      return calculateMaximumSize(header.getBigBlockSize(), header.getBATCount());
   }

   public static BATBlockAndIndex getBATBlockAndIndex(int offset, HeaderBlock header, List bats) {
      POIFSBigBlockSize bigBlockSize = header.getBigBlockSize();
      int entriesPerBlock = bigBlockSize.getBATEntriesPerBlock();
      int whichBAT = offset / entriesPerBlock;
      int index = offset % entriesPerBlock;
      return new BATBlockAndIndex(index, (BATBlock)bats.get(whichBAT));
   }

   public static BATBlockAndIndex getSBATBlockAndIndex(int offset, HeaderBlock header, List sbats) {
      return getBATBlockAndIndex(offset, header, sbats);
   }

   public boolean hasFreeSectors() {
      return this._has_free_sectors;
   }

   public int getOccupiedSize() {
      int usedSectors = this._values.length;

      for(int k = this._values.length - 1; k >= 0 && this._values[k] == -1; --k) {
         --usedSectors;
      }

      return usedSectors;
   }

   public int getValueAt(int relativeOffset) {
      if (relativeOffset >= this._values.length) {
         throw new ArrayIndexOutOfBoundsException("Unable to fetch offset " + relativeOffset + " as the BAT only contains " + this._values.length + " entries");
      } else {
         return this._values[relativeOffset];
      }
   }

   public void setValueAt(int relativeOffset, int value) {
      int oldValue = this._values[relativeOffset];
      this._values[relativeOffset] = value;
      if (value == -1) {
         this._has_free_sectors = true;
      } else {
         if (oldValue == -1) {
            this.recomputeFree();
         }

      }
   }

   public void setOurBlockIndex(int index) {
      this.ourBlockIndex = index;
   }

   public int getOurBlockIndex() {
      return this.ourBlockIndex;
   }

   public void writeData(ByteBuffer block) {
      block.put(this.serialize());
   }

   private byte[] serialize() {
      byte[] data = new byte[this.bigBlockSize.getBigBlockSize()];
      int offset = 0;
      int[] var3 = this._values;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         int _value = var3[var5];
         LittleEndian.putInt(data, offset, _value);
         offset += 4;
      }

      return data;
   }

   public static final class BATBlockAndIndex {
      private final int index;
      private final BATBlock block;

      private BATBlockAndIndex(int index, BATBlock block) {
         this.index = index;
         this.block = block;
      }

      public int getIndex() {
         return this.index;
      }

      public BATBlock getBlock() {
         return this.block;
      }

      // $FF: synthetic method
      BATBlockAndIndex(int x0, BATBlock x1, Object x2) {
         this(x0, x1);
      }
   }
}
