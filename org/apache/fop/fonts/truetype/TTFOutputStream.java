package org.apache.fop.fonts.truetype;

import java.io.IOException;

public interface TTFOutputStream {
   void startFontStream() throws IOException;

   TTFTableOutputStream getTableOutputStream();

   TTFGlyphOutputStream getGlyphOutputStream();

   void endFontStream() throws IOException;
}
