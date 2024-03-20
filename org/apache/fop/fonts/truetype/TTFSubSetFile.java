package org.apache.fop.fonts.truetype;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

public class TTFSubSetFile extends TTFFile {
   protected byte[] output;
   protected int realSize;
   protected int currentPos;
   protected Map offsets = new HashMap();
   private int checkSumAdjustmentOffset;
   protected int locaOffset;
   protected int[] glyphOffsets;
   protected Map newDirTabs = new HashMap();

   public TTFSubSetFile() {
   }

   public TTFSubSetFile(boolean useKerning, boolean useAdvanced) {
      super(useKerning, useAdvanced);
   }

   private int determineTableCount() {
      int numTables = 4;
      if (this.isCFF()) {
         throw new UnsupportedOperationException("OpenType fonts with CFF glyphs are not supported");
      } else {
         numTables += 5;
         if (this.hasCvt()) {
            ++numTables;
         }

         if (this.hasFpgm()) {
            ++numTables;
         }

         if (this.hasPrep()) {
            ++numTables;
         }

         if (!this.cid) {
            ++numTables;
         }

         return numTables;
      }
   }

   protected void createDirectory() {
      int numTables = this.determineTableCount();
      this.writeByte((byte)0);
      this.writeByte((byte)1);
      this.writeByte((byte)0);
      this.writeByte((byte)0);
      this.realSize += 4;
      this.writeUShort(numTables);
      this.realSize += 2;
      int maxPow = this.maxPow2(numTables);
      int searchRange = (int)Math.pow(2.0, (double)maxPow) * 16;
      this.writeUShort(searchRange);
      this.realSize += 2;
      this.writeUShort(maxPow);
      this.realSize += 2;
      this.writeUShort(numTables * 16 - searchRange);
      this.realSize += 2;
      this.writeTableName(OFTableName.OS2);
      if (!this.cid) {
         this.writeTableName(OFTableName.CMAP);
      }

      if (this.hasCvt()) {
         this.writeTableName(OFTableName.CVT);
      }

      if (this.hasFpgm()) {
         this.writeTableName(OFTableName.FPGM);
      }

      this.writeTableName(OFTableName.GLYF);
      this.writeTableName(OFTableName.HEAD);
      this.writeTableName(OFTableName.HHEA);
      this.writeTableName(OFTableName.HMTX);
      this.writeTableName(OFTableName.LOCA);
      this.writeTableName(OFTableName.MAXP);
      this.writeTableName(OFTableName.NAME);
      this.writeTableName(OFTableName.POST);
      if (this.hasPrep()) {
         this.writeTableName(OFTableName.PREP);
      }

      this.newDirTabs.put(OFTableName.TABLE_DIRECTORY, new OFDirTabEntry(0L, (long)this.currentPos));
   }

   private void writeTableName(OFTableName tableName) {
      this.writeString(tableName.getName());
      this.offsets.put(tableName, this.currentPos);
      this.currentPos += 12;
      this.realSize += 16;
   }

   private boolean hasCvt() {
      return this.dirTabs.containsKey(OFTableName.CVT);
   }

   private boolean hasFpgm() {
      return this.dirTabs.containsKey(OFTableName.FPGM);
   }

   private boolean hasPrep() {
      return this.dirTabs.containsKey(OFTableName.PREP);
   }

   protected void createLoca(int size) throws IOException {
      this.pad4();
      this.locaOffset = this.currentPos;
      int dirTableOffset = (Integer)this.offsets.get(OFTableName.LOCA);
      this.writeULong(dirTableOffset + 4, this.currentPos);
      this.writeULong(dirTableOffset + 8, size * 4 + 4);
      this.currentPos += size * 4 + 4;
      this.realSize += size * 4 + 4;
   }

   private boolean copyTable(FontFileReader in, OFTableName tableName) throws IOException {
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(tableName);
      if (entry != null) {
         this.pad4();
         this.seekTab(in, tableName, 0L);
         this.writeBytes(in.getBytes((int)entry.getOffset(), (int)entry.getLength()));
         this.updateCheckSum(this.currentPos, (int)entry.getLength(), tableName);
         this.currentPos += (int)entry.getLength();
         this.realSize += (int)entry.getLength();
         return true;
      } else {
         return false;
      }
   }

   protected boolean createCvt(FontFileReader in) throws IOException {
      return this.copyTable(in, OFTableName.CVT);
   }

   protected boolean createFpgm(FontFileReader in) throws IOException {
      return this.copyTable(in, OFTableName.FPGM);
   }

   protected boolean createName(FontFileReader in) throws IOException {
      return this.copyTable(in, OFTableName.NAME);
   }

   protected boolean createOS2(FontFileReader in) throws IOException {
      return this.copyTable(in, OFTableName.OS2);
   }

   protected void createMaxp(FontFileReader in, int size) throws IOException {
      OFTableName maxp = OFTableName.MAXP;
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(maxp);
      if (entry != null) {
         this.pad4();
         this.seekTab(in, maxp, 0L);
         this.writeBytes(in.getBytes((int)entry.getOffset(), (int)entry.getLength()));
         this.writeUShort(this.currentPos + 4, size);
         this.updateCheckSum(this.currentPos, (int)entry.getLength(), maxp);
         this.currentPos += (int)entry.getLength();
         this.realSize += (int)entry.getLength();
      } else {
         throw new IOException("Can't find maxp table");
      }
   }

   protected void createPost(FontFileReader in) throws IOException {
      OFTableName post = OFTableName.POST;
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(post);
      if (entry != null) {
         this.pad4();
         this.seekTab(in, post, 0L);
         int newTableSize = 32;
         byte[] newPostTable = new byte[newTableSize];
         System.arraycopy(in.getBytes((int)entry.getOffset(), newTableSize), 0, newPostTable, 0, newTableSize);
         newPostTable[1] = 3;
         this.writeBytes(newPostTable);
         this.updateCheckSum(this.currentPos, newTableSize, post);
         this.currentPos += newTableSize;
         this.realSize += newTableSize;
      }

   }

   protected boolean createPrep(FontFileReader in) throws IOException {
      return this.copyTable(in, OFTableName.PREP);
   }

   protected void createHhea(FontFileReader in, int size) throws IOException {
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(OFTableName.HHEA);
      if (entry != null) {
         this.pad4();
         this.seekTab(in, OFTableName.HHEA, 0L);
         this.writeBytes(in.getBytes((int)entry.getOffset(), (int)entry.getLength()));
         this.writeUShort((int)entry.getLength() + this.currentPos - 2, size);
         this.updateCheckSum(this.currentPos, (int)entry.getLength(), OFTableName.HHEA);
         this.currentPos += (int)entry.getLength();
         this.realSize += (int)entry.getLength();
      } else {
         throw new IOException("Can't find hhea table");
      }
   }

   protected void createHead(FontFileReader in) throws IOException {
      OFTableName head = OFTableName.HEAD;
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(head);
      if (entry != null) {
         this.pad4();
         this.seekTab(in, head, 0L);
         this.writeBytes(in.getBytes((int)entry.getOffset(), (int)entry.getLength()));
         this.checkSumAdjustmentOffset = this.currentPos + 8;
         this.output[this.currentPos + 8] = 0;
         this.output[this.currentPos + 9] = 0;
         this.output[this.currentPos + 10] = 0;
         this.output[this.currentPos + 11] = 0;
         this.output[this.currentPos + 50] = 0;
         if (this.cid) {
            this.output[this.currentPos + 51] = 1;
         }

         this.updateCheckSum(this.currentPos, (int)entry.getLength(), head);
         this.currentPos += (int)entry.getLength();
         this.realSize += (int)entry.getLength();
      } else {
         throw new IOException("Can't find head table");
      }
   }

   private void createGlyf(FontFileReader in, Map glyphs) throws IOException {
      OFTableName glyf = OFTableName.GLYF;
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(glyf);
      int size = false;
      int startPos = false;
      int endOffset = 0;
      if (entry != null) {
         this.pad4();
         int startPos = this.currentPos;
         int[] origIndexes = this.buildSubsetIndexToOrigIndexMap(glyphs);
         this.glyphOffsets = new int[origIndexes.length];

         int i;
         int origGlyphIndex;
         int nextOffset;
         for(i = 0; i < origIndexes.length; ++i) {
            int nextOffset = false;
            origGlyphIndex = origIndexes[i];
            if (origGlyphIndex >= this.mtxTab.length - 1) {
               nextOffset = (int)this.lastLoca;
            } else {
               nextOffset = (int)this.mtxTab[origGlyphIndex + 1].getOffset();
            }

            int glyphOffset = (int)this.mtxTab[origGlyphIndex].getOffset();
            int glyphLength = nextOffset - glyphOffset;
            byte[] glyphData = in.getBytes((int)entry.getOffset() + glyphOffset, glyphLength);
            int endOffset1 = endOffset;
            this.writeBytes(glyphData);
            this.writeULong(this.locaOffset + i * 4, this.currentPos - startPos);
            if (this.currentPos - startPos + glyphLength > endOffset) {
               endOffset1 = this.currentPos - startPos + glyphLength;
            }

            this.glyphOffsets[i] = this.currentPos;
            this.currentPos += glyphLength;
            this.realSize += glyphLength;
            endOffset = endOffset1;
         }

         int size = this.currentPos - startPos;
         this.currentPos += 12;
         this.realSize += 12;
         this.updateCheckSum(startPos, size + 12, glyf);
         this.writeULong(this.locaOffset + glyphs.size() * 4, endOffset);
         i = glyphs.size() * 4 + 4;
         nextOffset = getCheckSum(this.output, this.locaOffset, i);
         this.writeULong((Integer)this.offsets.get(OFTableName.LOCA), nextOffset);
         origGlyphIndex = (this.locaOffset + i) % 4;
         this.newDirTabs.put(OFTableName.LOCA, new OFDirTabEntry((long)this.locaOffset, (long)(i + origGlyphIndex)));
      } else {
         throw new IOException("Can't find glyf table");
      }
   }

   protected int[] buildSubsetIndexToOrigIndexMap(Map glyphs) {
      int[] origIndexes = new int[glyphs.size()];
      Iterator var3 = glyphs.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry glyph = (Map.Entry)var3.next();
         int origIndex = (Integer)glyph.getKey();
         int subsetIndex = (Integer)glyph.getValue();
         if (origIndexes.length > subsetIndex) {
            origIndexes[subsetIndex] = origIndex;
         }
      }

      return origIndexes;
   }

   protected void createHmtx(FontFileReader in, Map glyphs) throws IOException {
      OFTableName hmtx = OFTableName.HMTX;
      OFDirTabEntry entry = (OFDirTabEntry)this.dirTabs.get(hmtx);
      int longHorMetricSize = glyphs.size() * 2;
      int leftSideBearingSize = glyphs.size() * 2;
      int hmtxSize = longHorMetricSize + leftSideBearingSize;
      if (entry == null) {
         throw new IOException("Can't find hmtx table");
      } else {
         this.pad4();
         Iterator var8 = glyphs.entrySet().iterator();

         while(var8.hasNext()) {
            Map.Entry glyph = (Map.Entry)var8.next();
            Integer origIndex = (Integer)glyph.getKey();
            Integer subsetIndex = (Integer)glyph.getValue();
            this.writeUShort(this.currentPos + subsetIndex * 4, this.mtxTab[origIndex].getWx());
            this.writeUShort(this.currentPos + subsetIndex * 4 + 2, this.mtxTab[origIndex].getLsb());
         }

         this.updateCheckSum(this.currentPos, hmtxSize, hmtx);
         this.currentPos += hmtxSize;
         this.realSize += hmtxSize;
      }
   }

   public void readFont(FontFileReader in, String name, String header, Map glyphs) throws IOException {
      this.fontFile = in;
      if (!this.checkTTC(header, name)) {
         throw new IOException("Failed to read font");
      } else {
         Map subsetGlyphs = new HashMap(glyphs);
         this.output = new byte[in.getFileSize()];
         this.readDirTabs();
         this.readFontHeader();
         this.getNumGlyphs();
         this.readHorizontalHeader();
         this.readHorizontalMetrics();
         this.readIndexToLocation();
         this.scanGlyphs(in, subsetGlyphs);
         this.createDirectory();
         boolean optionalTableFound = this.createCvt(in);
         if (!optionalTableFound) {
            this.log.debug("TrueType: ctv table not present. Skipped.");
         }

         optionalTableFound = this.createFpgm(in);
         if (!optionalTableFound) {
            this.log.debug("TrueType: fpgm table not present. Skipped.");
         }

         this.createLoca(subsetGlyphs.size());
         this.createGlyf(in, subsetGlyphs);
         this.createOS2(in);
         this.createHead(in);
         this.createHhea(in, subsetGlyphs.size());
         this.createHmtx(in, subsetGlyphs);
         this.createMaxp(in, subsetGlyphs.size());
         this.createName(in);
         this.createPost(in);
         optionalTableFound = this.createPrep(in);
         if (!optionalTableFound) {
            this.log.debug("TrueType: prep table not present. Skipped.");
         }

         this.pad4();
         this.createCheckSumAdjustment();
      }
   }

   public byte[] getFontSubset() {
      byte[] ret = new byte[this.realSize];
      System.arraycopy(this.output, 0, ret, 0, this.realSize);
      return ret;
   }

   private void handleGlyphSubset(TTFGlyphOutputStream glyphOut) throws IOException {
      glyphOut.startGlyphStream();

      for(int i = 0; i < this.glyphOffsets.length - 1; ++i) {
         glyphOut.streamGlyph(this.output, this.glyphOffsets[i], this.glyphOffsets[i + 1] - this.glyphOffsets[i]);
      }

      OFDirTabEntry glyf = (OFDirTabEntry)this.newDirTabs.get(OFTableName.GLYF);
      long lastGlyphLength = glyf.getLength() - ((long)this.glyphOffsets[this.glyphOffsets.length - 1] - glyf.getOffset());
      glyphOut.streamGlyph(this.output, this.glyphOffsets[this.glyphOffsets.length - 1], (int)lastGlyphLength);
      glyphOut.endGlyphStream();
   }

   public void stream(TTFOutputStream ttfOut) throws IOException {
      SortedSet sortedDirTabs = this.sortDirTabMap(this.newDirTabs);
      TTFTableOutputStream tableOut = ttfOut.getTableOutputStream();
      TTFGlyphOutputStream glyphOut = ttfOut.getGlyphOutputStream();
      ttfOut.startFontStream();
      Iterator var5 = sortedDirTabs.iterator();

      while(var5.hasNext()) {
         Map.Entry entry = (Map.Entry)var5.next();
         if (((OFTableName)entry.getKey()).equals(OFTableName.GLYF)) {
            this.handleGlyphSubset(glyphOut);
         } else {
            tableOut.streamTable(this.output, (int)((OFDirTabEntry)entry.getValue()).getOffset(), (int)((OFDirTabEntry)entry.getValue()).getLength());
         }
      }

      ttfOut.endFontStream();
   }

   protected void scanGlyphs(FontFileReader in, Map subsetGlyphs) throws IOException {
      OFDirTabEntry glyfTableInfo = (OFDirTabEntry)this.dirTabs.get(OFTableName.GLYF);
      if (glyfTableInfo == null) {
         throw new IOException("Glyf table could not be found");
      } else {
         GlyfTable glyfTable = new GlyfTable(in, this.mtxTab, glyfTableInfo, subsetGlyphs);
         glyfTable.populateGlyphsWithComposites();
      }
   }

   private int writeString(String str) {
      int length = 0;

      try {
         byte[] buf = str.getBytes("ISO-8859-1");
         this.writeBytes(buf);
         length = buf.length;
         this.currentPos += length;
      } catch (UnsupportedEncodingException var4) {
      }

      return length;
   }

   private void writeByte(byte b) {
      this.output[this.currentPos++] = b;
   }

   protected void writeBytes(byte[] b) {
      if (b.length + this.currentPos > this.output.length) {
         byte[] newoutput = new byte[this.output.length * 2];
         System.arraycopy(this.output, 0, newoutput, 0, this.output.length);
         this.output = newoutput;
      }

      System.arraycopy(b, 0, this.output, this.currentPos, b.length);
   }

   protected void writeUShort(int s) {
      byte b1 = (byte)(s >> 8 & 255);
      byte b2 = (byte)(s & 255);
      this.writeByte(b1);
      this.writeByte(b2);
   }

   protected void writeUShort(int pos, int s) {
      byte b1 = (byte)(s >> 8 & 255);
      byte b2 = (byte)(s & 255);
      this.output[pos] = b1;
      this.output[pos + 1] = b2;
   }

   protected void writeULong(int pos, int s) {
      byte b1 = (byte)(s >> 24 & 255);
      byte b2 = (byte)(s >> 16 & 255);
      byte b3 = (byte)(s >> 8 & 255);
      byte b4 = (byte)(s & 255);
      this.output[pos] = b1;
      this.output[pos + 1] = b2;
      this.output[pos + 2] = b3;
      this.output[pos + 3] = b4;
   }

   protected void pad4() {
      int padSize = this.getPadSize(this.currentPos);
      if (padSize < 4) {
         for(int i = 0; i < padSize; ++i) {
            this.output[this.currentPos++] = 0;
            ++this.realSize;
         }
      }

   }

   private int maxPow2(int max) {
      int i;
      for(i = 0; Math.pow(2.0, (double)i) <= (double)max; ++i) {
      }

      return i - 1;
   }

   protected void updateCheckSum(int tableStart, int tableSize, OFTableName tableName) {
      int checksum = getCheckSum(this.output, tableStart, tableSize);
      int offset = (Integer)this.offsets.get(tableName);
      int padSize = this.getPadSize(tableStart + tableSize);
      this.newDirTabs.put(tableName, new OFDirTabEntry((long)tableStart, (long)(tableSize + padSize)));
      this.writeULong(offset, checksum);
      this.writeULong(offset + 4, tableStart);
      this.writeULong(offset + 8, tableSize);
   }

   protected static int getCheckSum(byte[] data, int start, int size) {
      int remainder = size % 4;
      if (remainder != 0) {
         size += remainder;
      }

      long sum = 0L;

      for(int i = 0; i < size; i += 4) {
         long l = 0L;

         for(int j = 0; j < 4; ++j) {
            l <<= 8;
            if (data.length > start + i + j) {
               l |= (long)(data[start + i + j] & 255);
            }
         }

         sum += l;
      }

      return (int)sum;
   }

   protected void createCheckSumAdjustment() {
      long sum = (long)getCheckSum(this.output, 0, this.realSize);
      int checksum = (int)(-1313820742L - sum);
      this.writeULong(this.checkSumAdjustmentOffset, checksum);
   }
}
