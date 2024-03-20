package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class KernTable implements Table {
   private int version;
   private int nTables;
   private KernSubtable[] tables;

   protected KernTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.version = raf.readUnsignedShort();
      this.nTables = raf.readUnsignedShort();
      this.tables = new KernSubtable[this.nTables];

      for(int i = 0; i < this.nTables; ++i) {
         this.tables[i] = KernSubtable.read(raf);
      }

   }

   public int getSubtableCount() {
      return this.nTables;
   }

   public KernSubtable getSubtable(int i) {
      return this.tables[i];
   }

   public int getType() {
      return 1801810542;
   }
}
