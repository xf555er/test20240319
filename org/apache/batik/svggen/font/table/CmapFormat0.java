package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CmapFormat0 extends CmapFormat {
   private int[] glyphIdArray = new int[256];
   private int first;
   private int last;

   protected CmapFormat0(RandomAccessFile raf) throws IOException {
      super(raf);
      this.format = 0;
      this.first = -1;

      for(int i = 0; i < 256; ++i) {
         this.glyphIdArray[i] = raf.readUnsignedByte();
         if (this.glyphIdArray[i] > 0) {
            if (this.first == -1) {
               this.first = i;
            }

            this.last = i;
         }
      }

   }

   public int getFirst() {
      return this.first;
   }

   public int getLast() {
      return this.last;
   }

   public int mapCharCode(int charCode) {
      return 0 <= charCode && charCode < 256 ? this.glyphIdArray[charCode] : 0;
   }
}
