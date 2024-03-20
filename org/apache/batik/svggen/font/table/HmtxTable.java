package org.apache.batik.svggen.font.table;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HmtxTable implements Table {
   private byte[] buf = null;
   private int[] hMetrics = null;
   private short[] leftSideBearing = null;

   protected HmtxTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.buf = new byte[de.getLength()];
      raf.read(this.buf);
   }

   public void init(int numberOfHMetrics, int lsbCount) {
      if (this.buf != null) {
         this.hMetrics = new int[numberOfHMetrics];
         ByteArrayInputStream bais = new ByteArrayInputStream(this.buf);

         int i;
         for(i = 0; i < numberOfHMetrics; ++i) {
            this.hMetrics[i] = bais.read() << 24 | bais.read() << 16 | bais.read() << 8 | bais.read();
         }

         if (lsbCount > 0) {
            this.leftSideBearing = new short[lsbCount];

            for(i = 0; i < lsbCount; ++i) {
               this.leftSideBearing[i] = (short)(bais.read() << 8 | bais.read());
            }
         }

         this.buf = null;
      }
   }

   public int getAdvanceWidth(int i) {
      if (this.hMetrics == null) {
         return 0;
      } else {
         return i < this.hMetrics.length ? this.hMetrics[i] >> 16 : this.hMetrics[this.hMetrics.length - 1] >> 16;
      }
   }

   public short getLeftSideBearing(int i) {
      if (this.hMetrics == null) {
         return 0;
      } else {
         return i < this.hMetrics.length ? (short)(this.hMetrics[i] & '\uffff') : this.leftSideBearing[i - this.hMetrics.length];
      }
   }

   public int getType() {
      return 1752003704;
   }
}
