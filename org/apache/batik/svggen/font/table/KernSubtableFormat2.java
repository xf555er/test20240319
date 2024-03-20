package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class KernSubtableFormat2 extends KernSubtable {
   private int rowWidth;
   private int leftClassTable;
   private int rightClassTable;
   private int array;

   protected KernSubtableFormat2(RandomAccessFile raf) throws IOException {
      this.rowWidth = raf.readUnsignedShort();
      this.leftClassTable = raf.readUnsignedShort();
      this.rightClassTable = raf.readUnsignedShort();
      this.array = raf.readUnsignedShort();
   }

   public int getKerningPairCount() {
      return 0;
   }

   public KerningPair getKerningPair(int i) {
      return null;
   }
}
