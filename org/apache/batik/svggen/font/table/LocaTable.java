package org.apache.batik.svggen.font.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LocaTable implements Table {
   private byte[] buf = null;
   private int[] offsets = null;
   private short factor = 0;

   protected LocaTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.buf = new byte[de.getLength()];
      raf.read(this.buf);
   }

   public void init(int numGlyphs, boolean shortEntries) {
      if (this.buf != null) {
         this.offsets = new int[numGlyphs + 1];
         ByteArrayInputStream bais = new ByteArrayInputStream(this.buf);
         int i;
         if (shortEntries) {
            this.factor = 2;

            for(i = 0; i <= numGlyphs; ++i) {
               this.offsets[i] = bais.read() << 8 | bais.read();
            }
         } else {
            this.factor = 1;

            for(i = 0; i <= numGlyphs; ++i) {
               this.offsets[i] = bais.read() << 24 | bais.read() << 16 | bais.read() << 8 | bais.read();
            }
         }

         this.buf = null;
      }
   }

   public int getOffset(int i) {
      return this.offsets == null ? 0 : this.offsets[i] * this.factor;
   }

   public int getType() {
      return 1819239265;
   }
}
