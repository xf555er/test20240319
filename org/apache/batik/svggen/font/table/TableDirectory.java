package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class TableDirectory {
   private int version = 0;
   private short numTables = 0;
   private short searchRange = 0;
   private short entrySelector = 0;
   private short rangeShift = 0;
   private DirectoryEntry[] entries;

   public TableDirectory(RandomAccessFile raf) throws IOException {
      this.version = raf.readInt();
      this.numTables = raf.readShort();
      this.searchRange = raf.readShort();
      this.entrySelector = raf.readShort();
      this.rangeShift = raf.readShort();
      this.entries = new DirectoryEntry[this.numTables];

      for(int i = 0; i < this.numTables; ++i) {
         this.entries[i] = new DirectoryEntry(raf);
      }

      boolean modified = true;

      while(modified) {
         modified = false;

         for(int i = 0; i < this.numTables - 1; ++i) {
            if (this.entries[i].getOffset() > this.entries[i + 1].getOffset()) {
               DirectoryEntry temp = this.entries[i];
               this.entries[i] = this.entries[i + 1];
               this.entries[i + 1] = temp;
               modified = true;
            }
         }
      }

   }

   public DirectoryEntry getEntry(int index) {
      return this.entries[index];
   }

   public DirectoryEntry getEntryByTag(int tag) {
      for(int i = 0; i < this.numTables; ++i) {
         if (this.entries[i].getTag() == tag) {
            return this.entries[i];
         }
      }

      return null;
   }

   public short getEntrySelector() {
      return this.entrySelector;
   }

   public short getNumTables() {
      return this.numTables;
   }

   public short getRangeShift() {
      return this.rangeShift;
   }

   public short getSearchRange() {
      return this.searchRange;
   }

   public int getVersion() {
      return this.version;
   }
}
