package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class LigatureSet {
   private int ligatureCount;
   private int[] ligatureOffsets;
   private Ligature[] ligatures;

   public LigatureSet(RandomAccessFile raf, int offset) throws IOException {
      raf.seek((long)offset);
      this.ligatureCount = raf.readUnsignedShort();
      this.ligatureOffsets = new int[this.ligatureCount];
      this.ligatures = new Ligature[this.ligatureCount];

      int i;
      for(i = 0; i < this.ligatureCount; ++i) {
         this.ligatureOffsets[i] = raf.readUnsignedShort();
      }

      for(i = 0; i < this.ligatureCount; ++i) {
         raf.seek((long)(offset + this.ligatureOffsets[i]));
         this.ligatures[i] = new Ligature(raf);
      }

   }
}
