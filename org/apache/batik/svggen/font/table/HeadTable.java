package org.apache.batik.svggen.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

public class HeadTable implements Table {
   private int versionNumber;
   private int fontRevision;
   private int checkSumAdjustment;
   private int magicNumber;
   private short flags;
   private short unitsPerEm;
   private long created;
   private long modified;
   private short xMin;
   private short yMin;
   private short xMax;
   private short yMax;
   private short macStyle;
   private short lowestRecPPEM;
   private short fontDirectionHint;
   private short indexToLocFormat;
   private short glyphDataFormat;

   protected HeadTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
      raf.seek((long)de.getOffset());
      this.versionNumber = raf.readInt();
      this.fontRevision = raf.readInt();
      this.checkSumAdjustment = raf.readInt();
      this.magicNumber = raf.readInt();
      this.flags = raf.readShort();
      this.unitsPerEm = raf.readShort();
      this.created = raf.readLong();
      this.modified = raf.readLong();
      this.xMin = raf.readShort();
      this.yMin = raf.readShort();
      this.xMax = raf.readShort();
      this.yMax = raf.readShort();
      this.macStyle = raf.readShort();
      this.lowestRecPPEM = raf.readShort();
      this.fontDirectionHint = raf.readShort();
      this.indexToLocFormat = raf.readShort();
      this.glyphDataFormat = raf.readShort();
   }

   public int getCheckSumAdjustment() {
      return this.checkSumAdjustment;
   }

   public long getCreated() {
      return this.created;
   }

   public short getFlags() {
      return this.flags;
   }

   public short getFontDirectionHint() {
      return this.fontDirectionHint;
   }

   public int getFontRevision() {
      return this.fontRevision;
   }

   public short getGlyphDataFormat() {
      return this.glyphDataFormat;
   }

   public short getIndexToLocFormat() {
      return this.indexToLocFormat;
   }

   public short getLowestRecPPEM() {
      return this.lowestRecPPEM;
   }

   public short getMacStyle() {
      return this.macStyle;
   }

   public long getModified() {
      return this.modified;
   }

   public int getType() {
      return 1751474532;
   }

   public short getUnitsPerEm() {
      return this.unitsPerEm;
   }

   public int getVersionNumber() {
      return this.versionNumber;
   }

   public short getXMax() {
      return this.xMax;
   }

   public short getXMin() {
      return this.xMin;
   }

   public short getYMax() {
      return this.yMax;
   }

   public short getYMin() {
      return this.yMin;
   }

   public String toString() {
      return "head\n\tversionNumber: " + this.versionNumber + "\n\tfontRevision: " + this.fontRevision + "\n\tcheckSumAdjustment: " + this.checkSumAdjustment + "\n\tmagicNumber: " + this.magicNumber + "\n\tflags: " + this.flags + "\n\tunitsPerEm: " + this.unitsPerEm + "\n\tcreated: " + this.created + "\n\tmodified: " + this.modified + "\n\txMin: " + this.xMin + ", yMin: " + this.yMin + "\n\txMax: " + this.xMax + ", yMax: " + this.yMax + "\n\tmacStyle: " + this.macStyle + "\n\tlowestRecPPEM: " + this.lowestRecPPEM + "\n\tfontDirectionHint: " + this.fontDirectionHint + "\n\tindexToLocFormat: " + this.indexToLocFormat + "\n\tglyphDataFormat: " + this.glyphDataFormat;
   }
}
