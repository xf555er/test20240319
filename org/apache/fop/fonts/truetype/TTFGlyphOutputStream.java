package org.apache.fop.fonts.truetype;

import java.io.IOException;

public interface TTFGlyphOutputStream {
   void startGlyphStream() throws IOException;

   void streamGlyph(byte[] var1, int var2, int var3) throws IOException;

   void endGlyphStream() throws IOException;
}
