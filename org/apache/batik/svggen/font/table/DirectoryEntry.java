package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DirectoryEntry {
   private int tag;
   private int checksum;
   private int offset;
   private int length;
   private Table table = null;

   protected DirectoryEntry(RandomAccessFile raf) throws IOException {
      this.tag = raf.readInt();
      this.checksum = raf.readInt();
      this.offset = raf.readInt();
      this.length = raf.readInt();
   }

   public int getChecksum() {
      return this.checksum;
   }

   public int getLength() {
      return this.length;
   }

   public int getOffset() {
      return this.offset;
   }

   public int getTag() {
      return this.tag;
   }

   public String toString() {
      return "" + (char)(this.tag >> 24 & 255) + (char)(this.tag >> 16 & 255) + (char)(this.tag >> 8 & 255) + (char)(this.tag & 255) + ", offset: " + this.offset + ", length: " + this.length + ", checksum: 0x" + Integer.toHexString(this.checksum);
   }
}
