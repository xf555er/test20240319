package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;
import org.apache.fop.fonts.truetype.FontFileReader;

public class PCLTTFTable {
   protected FontFileReader reader;

   public PCLTTFTable(FontFileReader reader) {
      this.reader = reader;
   }

   protected void skipShort(FontFileReader reader, int skips) throws IOException {
      reader.skip((long)skips * 2L);
   }

   protected void skipLong(FontFileReader reader, int skips) throws IOException {
      reader.skip((long)skips * 4L);
   }

   protected void skipByte(FontFileReader reader, int skips) throws IOException {
      reader.skip((long)skips);
   }
}
