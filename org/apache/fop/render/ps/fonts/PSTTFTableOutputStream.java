package org.apache.fop.render.ps.fonts;

import java.io.IOException;
import org.apache.fop.fonts.truetype.TTFTableOutputStream;

public class PSTTFTableOutputStream implements TTFTableOutputStream {
   private PSTTFGenerator ttfGen;

   public PSTTFTableOutputStream(PSTTFGenerator ttfGen) {
      this.ttfGen = ttfGen;
   }

   public void streamTable(byte[] ttfData, int offset, int size) throws IOException {
      int offsetPosition = offset;

      for(int i = 0; i < size / 32764; ++i) {
         this.streamString(ttfData, offsetPosition, 32764);
         offsetPosition += 32764;
      }

      if (size % 32764 > 0) {
         this.streamString(ttfData, offsetPosition, size % 32764);
      }

   }

   private void streamString(byte[] byteArray, int offset, int length) throws IOException {
      this.ttfGen.startString();
      this.ttfGen.streamBytes(byteArray, offset, length);
      this.ttfGen.endString();
   }
}
