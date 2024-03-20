package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CmapFormat6 extends CmapFormat {
   private short format = 6;
   private short length;
   private short version;
   private short firstCode;
   private short entryCount;
   private short[] glyphIdArray;

   protected CmapFormat6(RandomAccessFile raf) throws IOException {
      super(raf);
   }

   public int getFirst() {
      return 0;
   }

   public int getLast() {
      return 0;
   }

   public int mapCharCode(int charCode) {
      return 0;
   }
}
