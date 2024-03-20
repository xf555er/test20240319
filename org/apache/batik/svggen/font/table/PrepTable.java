package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class PrepTable extends Program implements Table {
   public PrepTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.readInstructions(raf, de.getLength());
   }

   public int getType() {
      return 1886545264;
   }
}
