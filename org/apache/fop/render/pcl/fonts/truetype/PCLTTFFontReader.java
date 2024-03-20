package org.apache.fop.render.pcl.fonts.truetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFDirTabEntry;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OFMtxEntry;
import org.apache.fop.fonts.truetype.OFTableName;
import org.apache.fop.fonts.truetype.OpenFont;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.PCLByteWriterUtil;
import org.apache.fop.render.pcl.fonts.PCLFontReader;
import org.apache.fop.render.pcl.fonts.PCLFontSegment;
import org.apache.fop.render.pcl.fonts.PCLSymbolSet;

public class PCLTTFFontReader extends PCLFontReader {
   protected TTFFile ttfFont;
   protected InputStream fontStream;
   protected FontFileReader reader;
   private PCLTTFPCLTFontTable pcltTable;
   private PCLTTFOS2FontTable os2Table;
   private PCLTTFPOSTFontTable postTable;
   private PCLTTFTableFactory ttfTableFactory;
   private Map charOffsets;
   private Map charMtxOffsets;
   private static final int HMTX_RESTRICT_SIZE = 50000;
   private static final Map FONT_WEIGHT = new HashMap() {
      private static final long serialVersionUID = 1L;

      {
         this.put(100, -6);
         this.put(200, -4);
         this.put(300, -3);
         this.put(400, 0);
         this.put(500, 0);
         this.put(600, 2);
         this.put(700, 3);
         this.put(800, 4);
         this.put(900, 5);
      }
   };
   private static final Map FONT_SERIF = new HashMap() {
      private static final long serialVersionUID = 1L;

      {
         this.put(0, 0);
         this.put(1, 64);
         this.put(2, 9);
         this.put(3, 12);
         this.put(4, 10);
         this.put(5, 0);
         this.put(6, 128);
         this.put(7, 2);
         this.put(8, 7);
         this.put(9, 11);
         this.put(10, 3);
         this.put(11, 0);
         this.put(12, 4);
         this.put(13, 6);
         this.put(14, 8);
         this.put(15, 1);
      }
   };
   private static final Map FONT_WIDTH = new HashMap() {
      private static final long serialVersionUID = 1L;

      {
         this.put(1, -5);
         this.put(2, -4);
         this.put(3, -3);
         this.put(4, -2);
         this.put(5, 0);
         this.put(6, 2);
         this.put(7, 3);
      }
   };
   private int scaleFactor = -1;
   private PCLSymbolSet symbolSet;

   public PCLTTFFontReader(Typeface font) throws IOException {
      super(font);
      this.symbolSet = PCLSymbolSet.Bound_Generic;
      this.loadFont();
   }

   protected void loadFont() throws IOException {
      if (this.typeface instanceof CustomFontMetricsMapper) {
         CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper)this.typeface;
         CustomFont font = (CustomFont)fontMetrics.getRealFont();
         this.setFont((CustomFont)fontMetrics.getRealFont());
         String fontName = font.getFullName();
         this.fontStream = font.getInputStream();
         this.reader = new FontFileReader(this.fontStream);
         this.ttfFont = new TTFFile();
         String header = OFFontLoader.readHeader(this.reader);
         this.ttfFont.readFont(this.reader, header, fontName);
         this.readFontTables();
      }

   }

   protected void readFontTables() throws IOException {
      PCLTTFTable fontTable = this.readFontTable(OFTableName.PCLT);
      if (fontTable instanceof PCLTTFPCLTFontTable) {
         this.pcltTable = (PCLTTFPCLTFontTable)fontTable;
      }

      fontTable = this.readFontTable(OFTableName.OS2);
      if (fontTable instanceof PCLTTFOS2FontTable) {
         this.os2Table = (PCLTTFOS2FontTable)fontTable;
      }

      fontTable = this.readFontTable(OFTableName.POST);
      if (fontTable instanceof PCLTTFPOSTFontTable) {
         this.postTable = (PCLTTFPOSTFontTable)fontTable;
      }

   }

   private PCLTTFTable readFontTable(OFTableName tableName) throws IOException {
      return this.ttfFont.seekTab(this.reader, tableName, 0L) ? this.getTTFTableFactory().newInstance(tableName) : null;
   }

   private PCLTTFTableFactory getTTFTableFactory() {
      if (this.ttfTableFactory == null) {
         this.ttfTableFactory = PCLTTFTableFactory.getInstance(this.reader);
      }

      return this.ttfTableFactory;
   }

   public int getDescriptorSize() {
      return 72;
   }

   public int getHeaderFormat() {
      return 15;
   }

   public int getFontType() {
      return this.symbolSet == PCLSymbolSet.Unbound ? 11 : 2;
   }

   public int getStyleMSB() {
      return this.pcltTable != null ? this.getMSB(this.pcltTable.getStyle()) : 3;
   }

   public int getBaselinePosition() {
      return 0;
   }

   public int getCellWidth() {
      int[] bbox = this.ttfFont.getBBoxRaw();
      return bbox[2] - bbox[0];
   }

   public int getCellHeight() {
      int[] bbox = this.ttfFont.getBBoxRaw();
      return bbox[3] - bbox[1];
   }

   public int getOrientation() {
      return 0;
   }

   public int getSpacing() {
      if (this.os2Table != null) {
         return this.os2Table.getPanose()[4] == 9 ? 0 : 1;
      } else {
         return this.postTable != null ? this.postTable.getIsFixedPitch() : 1;
      }
   }

   public int getSymbolSet() {
      return this.pcltTable != null ? this.pcltTable.getSymbolSet() : this.symbolSet.getKind1();
   }

   public int getPitch() {
      int pitch = this.ttfFont.getCharWidthRaw(32);
      return pitch < 0 ? 0 : pitch;
   }

   public int getHeight() {
      return 0;
   }

   public int getXHeight() {
      if (this.pcltTable != null) {
         return this.pcltTable.getXHeight();
      } else {
         return this.os2Table != null ? this.os2Table.getXHeight() : 0;
      }
   }

   public int getWidthType() {
      if (this.pcltTable != null) {
         return this.pcltTable.getWidthType();
      } else {
         return this.os2Table != null ? this.convertTTFWidthClass(this.os2Table.getWidthClass()) : 0;
      }
   }

   private int convertTTFWidthClass(int widthClass) {
      return FONT_WIDTH.containsKey(widthClass) ? (Integer)FONT_WIDTH.get(widthClass) : 0;
   }

   public int getStyleLSB() {
      return this.pcltTable != null ? this.getLSB(this.pcltTable.getStyle()) : 224;
   }

   public int getStrokeWeight() {
      if (this.pcltTable != null) {
         return this.pcltTable.getStrokeWeight();
      } else {
         return this.os2Table != null ? this.convertTTFWeightClass(this.os2Table.getWeightClass()) : 0;
      }
   }

   private int convertTTFWeightClass(int weightClass) {
      return FONT_WEIGHT.containsKey(weightClass) ? (Integer)FONT_WEIGHT.get(weightClass) : 0;
   }

   public int getTypefaceLSB() {
      return this.pcltTable != null ? this.getLSB(this.pcltTable.getTypeFamily()) : 254;
   }

   public int getTypefaceMSB() {
      return this.pcltTable != null ? this.getMSB(this.pcltTable.getTypeFamily()) : 0;
   }

   public int getSerifStyle() {
      return this.pcltTable != null ? this.pcltTable.getSerifStyle() : this.convertFromTTFSerifStyle();
   }

   private int convertFromTTFSerifStyle() {
      if (this.os2Table != null) {
         int serifStyle = this.os2Table.getPanose()[1];
         return (Integer)FONT_SERIF.get(serifStyle);
      } else {
         return 0;
      }
   }

   public int getQuality() {
      return 2;
   }

   public int getPlacement() {
      return 0;
   }

   public int getUnderlinePosition() {
      return 0;
   }

   public int getUnderlineThickness() {
      return 0;
   }

   public int getTextHeight() {
      return 2048;
   }

   public int getTextWidth() {
      return this.os2Table != null ? this.os2Table.getAvgCharWidth() : 0;
   }

   public int getFirstCode() {
      return 32;
   }

   public int getLastCode() {
      return 255;
   }

   public int getPitchExtended() {
      return 0;
   }

   public int getHeightExtended() {
      return 0;
   }

   public int getCapHeight() {
      if (this.pcltTable != null) {
         return this.pcltTable.getStrokeWeight();
      } else {
         return this.os2Table != null ? this.os2Table.getCapHeight() : 0;
      }
   }

   public int getFontNumber() {
      return this.pcltTable != null ? (int)this.pcltTable.getFontNumber() : 0;
   }

   public String getFontName() {
      return this.pcltTable != null ? this.pcltTable.getTypeface() : this.ttfFont.getFullName();
   }

   public int getScaleFactor() throws IOException {
      if (this.scaleFactor == -1) {
         OFTableName headTag = OFTableName.HEAD;
         if (this.ttfFont.seekTab(this.reader, headTag, 0L)) {
            this.reader.readTTFLong();
            this.reader.readTTFLong();
            this.reader.readTTFLong();
            this.reader.readTTFLong();
            this.reader.readTTFShort();
            this.scaleFactor = this.reader.readTTFUShort();
            return this.scaleFactor;
         } else {
            return 0;
         }
      } else {
         return this.scaleFactor;
      }
   }

   public int getMasterUnderlinePosition() throws IOException {
      return (int)Math.round((double)this.getScaleFactor() * 0.2);
   }

   public int getMasterUnderlineThickness() throws IOException {
      return (int)Math.round((double)this.getScaleFactor() * 0.05);
   }

   public int getFontScalingTechnology() {
      return 1;
   }

   public int getVariety() {
      return 0;
   }

   public List getFontSegments(Map mappedGlyphs) throws IOException {
      List fontSegments = new ArrayList();
      fontSegments.add(new PCLFontSegment(PCLFontSegment.SegmentID.CC, this.getCharacterComplement()));
      fontSegments.add(new PCLFontSegment(PCLFontSegment.SegmentID.PA, PCLByteWriterUtil.toByteArray(this.os2Table.getPanose())));
      fontSegments.add(new PCLFontSegment(PCLFontSegment.SegmentID.GT, this.getGlobalTrueTypeData(mappedGlyphs)));
      fontSegments.add(new PCLFontSegment(PCLFontSegment.SegmentID.CP, this.ttfFont.getCopyrightNotice().getBytes("US-ASCII")));
      fontSegments.add(new PCLFontSegment(PCLFontSegment.SegmentID.NULL, new byte[0]));
      return fontSegments;
   }

   private byte[] getCharacterComplement() {
      byte[] ccUnicode = new byte[8];
      ccUnicode[7] = 6;
      return ccUnicode;
   }

   private byte[] getGlobalTrueTypeData(Map mappedGlyphs) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      List tableOffsets = new ArrayList();
      baos.write(PCLByteWriterUtil.unsignedInt(1));
      baos.write(PCLByteWriterUtil.unsignedInt(0));
      int numTables = 5;
      OFDirTabEntry headTable = this.ttfFont.getDirectoryEntry(OFTableName.CVT);
      if (headTable != null) {
         ++numTables;
      }

      OFDirTabEntry fpgmTable = this.ttfFont.getDirectoryEntry(OFTableName.FPGM);
      if (fpgmTable != null) {
         ++numTables;
      }

      OFDirTabEntry prepTable = this.ttfFont.getDirectoryEntry(OFTableName.PREP);
      if (prepTable != null) {
         ++numTables;
      }

      baos.write(PCLByteWriterUtil.unsignedInt(numTables));
      int maxPowerNumTables = PCLByteWriterUtil.maxPower2(numTables);
      int searchRange = maxPowerNumTables * 16;
      baos.write(PCLByteWriterUtil.unsignedInt(searchRange));
      baos.write(PCLByteWriterUtil.unsignedInt(PCLByteWriterUtil.log(maxPowerNumTables, 2)));
      baos.write(PCLByteWriterUtil.unsignedInt(numTables * 16 - searchRange));
      this.writeTrueTypeTable(baos, OFTableName.HEAD, tableOffsets);
      this.writeTrueTypeTable(baos, OFTableName.HHEA, tableOffsets);
      byte[] hmtxTable = this.createHmtx(mappedGlyphs);
      this.writeSubsetHMTX(baos, OFTableName.HMTX, tableOffsets, hmtxTable);
      this.writeTrueTypeTable(baos, OFTableName.MAXP, tableOffsets);
      this.writeGDIR(baos);
      this.writeTrueTypeTable(baos, OFTableName.CVT, tableOffsets);
      this.writeTrueTypeTable(baos, OFTableName.FPGM, tableOffsets);
      this.writeTrueTypeTable(baos, OFTableName.PREP, tableOffsets);
      baos = this.copyTables(tableOffsets, baos, hmtxTable, mappedGlyphs.size());
      return baos.toByteArray();
   }

   private void writeTrueTypeTable(ByteArrayOutputStream baos, OFTableName table, List tableOffsets) throws IOException {
      OFDirTabEntry tabEntry = this.ttfFont.getDirectoryEntry(table);
      if (tabEntry != null) {
         baos.write(tabEntry.getTag());
         baos.write(PCLByteWriterUtil.unsignedLongInt(tabEntry.getChecksum()));
         TableOffset newTableOffset = new TableOffset(tabEntry.getOffset(), tabEntry.getLength(), baos.size());
         tableOffsets.add(newTableOffset);
         baos.write(PCLByteWriterUtil.unsignedLongInt(0));
         baos.write(PCLByteWriterUtil.unsignedLongInt(tabEntry.getLength()));
      }

   }

   private void writeGDIR(ByteArrayOutputStream baos) throws IOException {
      baos.write("gdir".getBytes("ISO-8859-1"));
      baos.write(PCLByteWriterUtil.unsignedLongInt(0));
      baos.write(PCLByteWriterUtil.unsignedLongInt(0));
      baos.write(PCLByteWriterUtil.unsignedLongInt(0));
   }

   private ByteArrayOutputStream copyTables(List tableOffsets, ByteArrayOutputStream baos, byte[] hmtxTable, int hmtxSize) throws IOException {
      Map offsetValues = new HashMap();
      Iterator var6 = tableOffsets.iterator();

      while(var6.hasNext()) {
         TableOffset tableOffset = (TableOffset)var6.next();
         offsetValues.put(tableOffset.getNewOffset(), PCLByteWriterUtil.unsignedLongInt(baos.size()));
         if (tableOffset.getOriginOffset() == -1L) {
            baos.write(hmtxTable);
         } else {
            byte[] tableData = this.reader.getBytes((int)tableOffset.getOriginOffset(), (int)tableOffset.getOriginLength());
            int index = tableOffsets.indexOf(tableOffset);
            if (index == 1) {
               tableData = this.updateHHEA(tableData, hmtxSize + 33);
            }

            baos.write(tableData);
         }
      }

      baos = this.updateOffsets(baos, offsetValues);
      return baos;
   }

   private byte[] updateHHEA(byte[] tableData, int hmtxSize) {
      this.writeUShort(tableData, tableData.length - 2, hmtxSize);
      return tableData;
   }

   private ByteArrayOutputStream updateOffsets(ByteArrayOutputStream baos, Map offsets) throws IOException {
      byte[] softFont = baos.toByteArray();
      Iterator var4 = offsets.entrySet().iterator();

      while(var4.hasNext()) {
         Map.Entry integerEntry = (Map.Entry)var4.next();
         PCLByteWriterUtil.updateDataAtLocation(softFont, (byte[])integerEntry.getValue(), (Integer)integerEntry.getKey());
      }

      baos = new ByteArrayOutputStream();
      baos.write(softFont);
      return baos;
   }

   public Map getCharacterOffsets() throws IOException {
      if (this.charOffsets == null) {
         List mtx = this.ttfFont.getMtx();
         OFTableName glyfTag = OFTableName.GLYF;
         this.charOffsets = new HashMap();
         OFDirTabEntry tabEntry = this.ttfFont.getDirectoryEntry(glyfTag);
         if (this.ttfFont.seekTab(this.reader, glyfTag, 0L)) {
            for(int i = 1; i < mtx.size(); ++i) {
               OFMtxEntry entry = (OFMtxEntry)mtx.get(i);
               int nextOffset = false;
               int charCode = false;
               int charCode;
               if (entry.getUnicodeIndex().size() > 0) {
                  charCode = (Integer)entry.getUnicodeIndex().get(0);
               } else {
                  charCode = entry.getIndex();
               }

               int nextOffset;
               if (i < mtx.size() - 1) {
                  OFMtxEntry nextEntry = (OFMtxEntry)mtx.get(i + 1);
                  nextOffset = (int)nextEntry.getOffset();
               } else {
                  nextOffset = (int)this.ttfFont.getLastGlyfLocation();
               }

               int glyphOffset = (int)entry.getOffset();
               int glyphLength = nextOffset - glyphOffset;
               this.charOffsets.put(charCode, new int[]{(int)tabEntry.getOffset() + glyphOffset, glyphLength});
            }
         }
      }

      return this.charOffsets;
   }

   public OpenFont getFontFile() {
      return this.ttfFont;
   }

   public FontFileReader getFontFileReader() {
      return this.reader;
   }

   private void writeSubsetHMTX(ByteArrayOutputStream baos, OFTableName table, List tableOffsets, byte[] hmtxTable) throws IOException {
      OFDirTabEntry tabEntry = this.ttfFont.getDirectoryEntry(table);
      if (tabEntry != null) {
         baos.write(tabEntry.getTag());
         baos.write(PCLByteWriterUtil.unsignedLongInt(getCheckSum(hmtxTable, 0, hmtxTable.length)));
         TableOffset newTableOffset = new TableOffset(-1L, (long)hmtxTable.length, baos.size());
         tableOffsets.add(newTableOffset);
         baos.write(PCLByteWriterUtil.unsignedLongInt(0));
         baos.write(PCLByteWriterUtil.unsignedLongInt(hmtxTable.length));
      }

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

   protected byte[] createHmtx(Map mappedGlyphs) {
      byte[] hmtxTable = new byte[(mappedGlyphs.size() + 32) * 4];
      OFDirTabEntry entry = this.ttfFont.getDirectoryEntry(OFTableName.HMTX);
      if (entry != null) {
         Iterator var4 = mappedGlyphs.entrySet().iterator();

         while(var4.hasNext()) {
            Map.Entry glyphSubset = (Map.Entry)var4.next();
            char unicode = (Character)glyphSubset.getKey();
            int softFontGlyphIndex = (Integer)glyphSubset.getValue();
            int originalIndex;
            if (this.font instanceof MultiByteFont) {
               originalIndex = ((MultiByteFont)this.font).getGIDFromChar(unicode);
               this.writeUShort(hmtxTable, softFontGlyphIndex * 4, ((OFMtxEntry)this.ttfFont.getMtx().get(originalIndex)).getWx());
               this.writeUShort(hmtxTable, softFontGlyphIndex * 4 + 2, ((OFMtxEntry)this.ttfFont.getMtx().get(originalIndex)).getLsb());
            } else {
               originalIndex = ((SingleByteFont)this.font).getGIDFromChar(unicode);
               this.writeUShort(hmtxTable, softFontGlyphIndex * 4, this.font.getWidth(originalIndex, 1));
               this.writeUShort(hmtxTable, softFontGlyphIndex * 4 + 2, 0);
            }
         }
      }

      return hmtxTable;
   }

   private void writeUShort(byte[] out, int offset, int s) {
      byte b1 = (byte)(s >> 8 & 255);
      byte b2 = (byte)(s & 255);
      out[offset] = b1;
      out[offset + 1] = b2;
   }

   public Map scanMtxCharacters() throws IOException {
      if (this.charMtxOffsets == null) {
         this.charMtxOffsets = new HashMap();
         List mtx = this.ttfFont.getMtx();
         OFTableName glyfTag = OFTableName.GLYF;
         if (this.ttfFont.seekTab(this.reader, glyfTag, 0L)) {
            for(int i = 1; i < mtx.size(); ++i) {
               OFMtxEntry entry = (OFMtxEntry)mtx.get(i);
               int charCode = false;
               int charCode;
               if (entry.getUnicodeIndex().size() > 0) {
                  charCode = (Integer)entry.getUnicodeIndex().get(0);
               } else {
                  charCode = entry.getIndex();
               }

               this.charMtxOffsets.put(charCode, i);
            }
         }
      }

      return this.charMtxOffsets;
   }

   private static class TableOffset {
      private long originOffset;
      private long originLength;
      private int newOffset;

      public TableOffset(long originOffset, long originLength, int newOffset) {
         this.originOffset = originOffset;
         this.originLength = originLength;
         this.newOffset = newOffset;
      }

      public long getOriginOffset() {
         return this.originOffset;
      }

      public long getOriginLength() {
         return this.originLength;
      }

      public int getNewOffset() {
         return this.newOffset;
      }
   }
}
