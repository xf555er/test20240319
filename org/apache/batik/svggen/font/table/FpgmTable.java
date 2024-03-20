package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FpgmTable extends Program implements Table {
   protected FpgmTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.readInstructions(raf, de.getLength());
   }

   public int getType() {
      return 1718642541;
   }
}
