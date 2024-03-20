package org.apache.batik.svggen.font.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class GlyfTable implements Table {
   private byte[] buf = null;
   private GlyfDescript[] descript;

   protected GlyfTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.buf = new byte[de.getLength()];
      raf.read(this.buf);
   }

   public void init(int numGlyphs, LocaTable loca) {
      if (this.buf != null) {
         this.descript = new GlyfDescript[numGlyphs];
         ByteArrayInputStream bais = new ByteArrayInputStream(this.buf);

         int i;
         for(i = 0; i < numGlyphs; ++i) {
            int len = loca.getOffset(i + 1) - loca.getOffset(i);
            if (len > 0) {
               bais.reset();
               bais.skip((long)loca.getOffset(i));
               short numberOfContours = (short)(bais.read() << 8 | bais.read());
               if (numberOfContours >= 0) {
                  this.descript[i] = new GlyfSimpleDescript(this, numberOfContours, bais);
               } else {
                  this.descript[i] = new GlyfCompositeDescript(this, bais);
               }
            }
         }

         this.buf = null;

         for(i = 0; i < numGlyphs; ++i) {
            if (this.descript[i] != null) {
               this.descript[i].resolve();
            }
         }

      }
   }

   public GlyfDescript getDescription(int i) {
      return this.descript[i];
   }

   public int getType() {
      return 1735162214;
   }
}
