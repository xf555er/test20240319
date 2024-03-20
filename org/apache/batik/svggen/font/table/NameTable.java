package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NameTable implements Table {
   private short formatSelector;
   private short numberOfNameRecords;
   private short stringStorageOffset;
   private NameRecord[] records;

   protected NameTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.formatSelector = raf.readShort();
      this.numberOfNameRecords = raf.readShort();
      this.stringStorageOffset = raf.readShort();
      this.records = new NameRecord[this.numberOfNameRecords];

      int i;
      for(i = 0; i < this.numberOfNameRecords; ++i) {
         this.records[i] = new NameRecord(raf);
      }

      for(i = 0; i < this.numberOfNameRecords; ++i) {
         this.records[i].loadString(raf, de.getOffset() + this.stringStorageOffset);
      }

   }

   public String getRecord(short nameId) {
      for(int i = 0; i < this.numberOfNameRecords; ++i) {
         if (this.records[i].getNameId() == nameId) {
            return this.records[i].getRecordString();
         }
      }

      return "";
   }

   public int getType() {
      return 1851878757;
   }
}
