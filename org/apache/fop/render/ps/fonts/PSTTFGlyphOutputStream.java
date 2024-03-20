package org.apache.fop.render.ps.fonts;

import java.io.IOException;
import org.apache.fop.fonts.truetype.TTFGlyphOutputStream;

public class PSTTFGlyphOutputStream implements TTFGlyphOutputStream {
   private int byteCounter;
   private int lastStringBoundary;
   private PSTTFGenerator ttfGen;

   public PSTTFGlyphOutputStream(PSTTFGenerator ttfGen) {
      this.ttfGen = ttfGen;
   }

   public void startGlyphStream() throws IOException {
      this.ttfGen.startString();
   }

   public void streamGlyph(byte[] glyphData, int offset, int size) throws IOException {
      if (size > 32764) {
         throw new UnsupportedOperationException("The glyph is " + size + " bytes. There may be an error in the font file.");
      } else {
         if (size + (this.byteCounter - this.lastStringBoundary) < 32764) {
            this.ttfGen.streamBytes(glyphData, offset, size);
         } else {
            this.ttfGen.endString();
            this.lastStringBoundary = this.byteCounter;
            this.ttfGen.startString();
            this.ttfGen.streamBytes(glyphData, offset, size);
         }

         this.byteCounter += size;
      }
   }

   public void endGlyphStream() throws IOException {
      this.ttfGen.endString();
   }
}
