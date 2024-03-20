package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CmapTable implements Table {
   private int version;
   private int numTables;
   private CmapIndexEntry[] entries;
   private CmapFormat[] formats;

   protected CmapTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      long fp = raf.getFilePointer();
      this.version = raf.readUnsignedShort();
      this.numTables = raf.readUnsignedShort();
      this.entries = new CmapIndexEntry[this.numTables];
      this.formats = new CmapFormat[this.numTables];

      int i;
      for(i = 0; i < this.numTables; ++i) {
         this.entries[i] = new CmapIndexEntry(raf);
      }

      for(i = 0; i < this.numTables; ++i) {
         raf.seek(fp + (long)this.entries[i].getOffset());
         int format = raf.readUnsignedShort();
         this.formats[i] = CmapFormat.create(format, raf);
      }

   }

   public CmapFormat getCmapFormat(short platformId, short encodingId) {
      for(int i = 0; i < this.numTables; ++i) {
         if (this.entries[i].getPlatformId() == platformId && this.entries[i].getEncodingId() == encodingId) {
            return this.formats[i];
         }
      }

      return null;
   }

   public int getType() {
      return 1668112752;
   }

   public String toString() {
      StringBuffer sb = (new StringBuffer(this.numTables * 8)).append("cmap\n");

      int i;
      for(i = 0; i < this.numTables; ++i) {
         sb.append('\t').append(this.entries[i].toString()).append('\n');
      }

      for(i = 0; i < this.numTables; ++i) {
         sb.append('\t').append(this.formats[i].toString()).append('\n');
      }

      return sb.toString();
   }
}
