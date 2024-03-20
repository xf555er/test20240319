package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class SingleSubstFormat1 extends SingleSubst {
   private int coverageOffset;
   private short deltaGlyphID;
   private Coverage coverage;

   protected SingleSubstFormat1(RandomAccessFile raf, int offset) throws IOException {
      this.coverageOffset = raf.readUnsignedShort();
      this.deltaGlyphID = raf.readShort();
      raf.seek((long)(offset + this.coverageOffset));
      this.coverage = Coverage.read(raf);
   }

   public int getFormat() {
      return 1;
   }

   public int substitute(int glyphId) {
      int i = this.coverage.findGlyph(glyphId);
      return i > -1 ? glyphId + this.deltaGlyphID : glyphId;
   }
}
