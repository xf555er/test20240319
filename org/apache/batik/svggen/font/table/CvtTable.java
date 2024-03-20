package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CvtTable implements Table {
   private short[] values;

   protected CvtTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      int len = de.getLength() / 2;
      this.values = new short[len];

      for(int i = 0; i < len; ++i) {
         this.values[i] = raf.readShort();
      }

   }

   public int getType() {
      return 1668707360;
   }

   public short[] getValues() {
      return this.values;
   }
}
