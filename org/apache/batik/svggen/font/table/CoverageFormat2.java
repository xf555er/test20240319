package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CoverageFormat2 extends Coverage {
   private int rangeCount;
   private RangeRecord[] rangeRecords;

   protected CoverageFormat2(RandomAccessFile raf) throws IOException {
      this.rangeCount = raf.readUnsignedShort();
      this.rangeRecords = new RangeRecord[this.rangeCount];

      for(int i = 0; i < this.rangeCount; ++i) {
         this.rangeRecords[i] = new RangeRecord(raf);
      }

   }

   public int getFormat() {
      return 2;
   }

   public int findGlyph(int glyphId) {
      for(int i = 0; i < this.rangeCount; ++i) {
         int n = this.rangeRecords[i].getCoverageIndex(glyphId);
         if (n > -1) {
            return n;
         }
      }

      return -1;
   }
}
