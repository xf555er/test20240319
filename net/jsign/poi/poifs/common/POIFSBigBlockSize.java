package net.jsign.poi.poifs.common;

public final class POIFSBigBlockSize {
   private int bigBlockSize;
   private short headerValue;

   protected POIFSBigBlockSize(int bigBlockSize, short headerValue) {
      this.bigBlockSize = bigBlockSize;
      this.headerValue = headerValue;
   }

   public int getBigBlockSize() {
      return this.bigBlockSize;
   }

   public short getHeaderValue() {
      return this.headerValue;
   }

   public int getBATEntriesPerBlock() {
      return this.bigBlockSize / 4;
   }

   public int getXBATEntriesPerBlock() {
      return this.getBATEntriesPerBlock() - 1;
   }
}
