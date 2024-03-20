package org.apache.fop.render.ps.fonts;

import java.io.IOException;
import org.apache.fop.fonts.truetype.TTFGlyphOutputStream;
import org.apache.fop.fonts.truetype.TTFOutputStream;
import org.apache.fop.fonts.truetype.TTFTableOutputStream;
import org.apache.xmlgraphics.ps.PSGenerator;

public class PSTTFOutputStream implements TTFOutputStream {
   private final PSTTFGenerator ttfGen;

   public PSTTFOutputStream(PSGenerator gen) {
      this.ttfGen = new PSTTFGenerator(gen);
   }

   public void startFontStream() throws IOException {
      this.ttfGen.write("/sfnts[");
   }

   public TTFTableOutputStream getTableOutputStream() {
      return new PSTTFTableOutputStream(this.ttfGen);
   }

   public TTFGlyphOutputStream getGlyphOutputStream() {
      return new PSTTFGlyphOutputStream(this.ttfGen);
   }

   public void endFontStream() throws IOException {
      this.ttfGen.writeln("] def");
   }
}
