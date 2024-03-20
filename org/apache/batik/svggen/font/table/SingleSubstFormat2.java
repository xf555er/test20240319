package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class SingleSubstFormat2 extends SingleSubst {
   private int coverageOffset;
   private int glyphCount;
   private int[] substitutes;
   private Coverage coverage;

   protected SingleSubstFormat2(RandomAccessFile raf, int offset) throws IOException {
      this.coverageOffset = raf.readUnsignedShort();
      this.glyphCount = raf.readUnsignedShort();
      this.substitutes = new int[this.glyphCount];

      for(int i = 0; i < this.glyphCount; ++i) {
         this.substitutes[i] = raf.readUnsignedShort();
      }

      raf.seek((long)(offset + this.coverageOffset));
      this.coverage = Coverage.read(raf);
   }

   public int getFormat() {
      return 2;
   }

   public int substitute(int glyphId) {
      int i = this.coverage.findGlyph(glyphId);
      return i > -1 ? this.substitutes[i] : glyphId;
   }
}
