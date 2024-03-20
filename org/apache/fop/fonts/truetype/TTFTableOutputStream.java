package org.apache.fop.fonts.truetype;

import java.io.IOException;

public interface TTFTableOutputStream {
   void streamTable(byte[] var1, int var2, int var3) throws IOException;
}
