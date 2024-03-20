package org.apache.fop.fonts.cff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.fontbox.cff.CFFDataInput;
import org.apache.fontbox.cff.CFFOperator;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OTFFile;

public class CFFDataReader {
   private CFFDataInput cffData;
   private byte[] header;
   private CFFIndexData nameIndex;
   private CFFIndexData topDICTIndex;
   private CFFIndexData stringIndex;
   private CFFIndexData charStringIndex;
   private CFFIndexData globalIndexSubr;
   private CFFIndexData localIndexSubr;
   private CustomEncoding encoding;
   private FDSelect fdSelect;
   private List fdFonts;
   private static final int DOUBLE_BYTE_OPERATOR = 12;
   private static final int NUM_STANDARD_STRINGS = 391;
   private LinkedHashMap topDict;

   public CFFDataReader() {
   }

   public CFFDataReader(byte[] cffDataArray) throws IOException {
      this.cffData = new CFFDataInput(cffDataArray);
      this.readCFFData();
   }

   public CFFDataReader(FontFileReader fontFile) throws IOException {
      this.cffData = new CFFDataInput(OTFFile.getCFFData(fontFile));
      this.readCFFData();
   }

   private void readCFFData() throws IOException {
      this.header = this.readHeader();
      this.nameIndex = this.readIndex();
      this.topDICTIndex = this.readIndex();
      this.topDict = this.parseDictData(this.topDICTIndex.getData());
      this.stringIndex = this.readIndex();
      this.globalIndexSubr = this.readIndex();
      this.charStringIndex = this.readCharStringIndex();
      this.encoding = this.readEncoding();
      this.fdSelect = this.readFDSelect();
      this.localIndexSubr = this.readLocalIndexSubrs();
      this.fdFonts = this.parseCIDData();
   }

   public Map getPrivateDict(DICTEntry privateEntry) throws IOException {
      return this.parseDictData(this.getPrivateDictBytes(privateEntry));
   }

   public byte[] getPrivateDictBytes(DICTEntry privateEntry) throws IOException {
      int privateLength = ((Number)privateEntry.getOperands().get(0)).intValue();
      int privateOffset = ((Number)privateEntry.getOperands().get(1)).intValue();
      return this.getCFFOffsetBytes(privateOffset, privateLength);
   }

   private byte[] getCFFOffsetBytes(int offset, int length) throws IOException {
      this.cffData.setPosition(offset);
      return this.cffData.readBytes(length);
   }

   public LinkedHashMap parseDictData(byte[] dictData) throws IOException {
      LinkedHashMap dictEntries = new LinkedHashMap();
      List operands = new ArrayList();
      List operandLengths = new ArrayList();
      int lastOperandLength = 0;

      for(int i = 0; i < dictData.length; ++i) {
         int readByte = dictData[i] & 255;
         if (readByte < 28) {
            int[] operator = new int[readByte == 12 ? 2 : 1];
            if (readByte == 12) {
               operator[0] = dictData[i];
               operator[1] = dictData[i + 1];
               ++i;
            } else {
               operator[0] = dictData[i];
            }

            String operatorName = "";
            CFFOperator tempOp = null;
            if (operator.length > 1) {
               tempOp = CFFOperator.getOperator(new CFFOperator.Key(operator[0], operator[1]));
            } else {
               tempOp = CFFOperator.getOperator(new CFFOperator.Key(operator[0]));
            }

            if (tempOp != null) {
               operatorName = tempOp.getName();
            }

            DICTEntry newEntry = new DICTEntry();
            newEntry.setOperator(operator);
            newEntry.setOperands(new ArrayList(operands));
            newEntry.setOperatorName(operatorName);
            newEntry.setOffset(i - lastOperandLength);
            newEntry.setOperandLength(lastOperandLength);
            newEntry.setOperandLengths(new ArrayList(operandLengths));
            byte[] byteData = new byte[lastOperandLength + operator.length];
            System.arraycopy(dictData, i - operator.length - (lastOperandLength - 1), byteData, 0, operator.length + lastOperandLength);
            newEntry.setByteData(byteData);
            dictEntries.put(operatorName, newEntry);
            operands.clear();
            operandLengths.clear();
            lastOperandLength = 0;
         } else if (readByte >= 32 && readByte <= 246) {
            operands.add(readByte - 139);
            ++lastOperandLength;
            operandLengths.add(1);
         } else if (readByte >= 247 && readByte <= 250) {
            operands.add((readByte - 247) * 256 + (dictData[i + 1] & 255) + 108);
            lastOperandLength += 2;
            operandLengths.add(2);
            ++i;
         } else if (readByte >= 251 && readByte <= 254) {
            operands.add(-(readByte - 251) * 256 - (dictData[i + 1] & 255) - 108);
            lastOperandLength += 2;
            operandLengths.add(2);
            ++i;
         } else if (readByte == 28) {
            operands.add((dictData[i + 1] & 255) << 8 | dictData[i + 2] & 255);
            lastOperandLength += 3;
            operandLengths.add(3);
            i += 2;
         } else if (readByte == 29) {
            operands.add((dictData[i + 1] & 255) << 24 | (dictData[i + 2] & 255) << 16 | (dictData[i + 3] & 255) << 8 | dictData[i + 4] & 255);
            lastOperandLength += 5;
            operandLengths.add(5);
            i += 4;
         } else if (readByte == 30) {
            boolean terminatorFound = false;
            StringBuilder realNumber = new StringBuilder();
            int byteCount = 1;

            do {
               ++i;
               byte nibblesByte = dictData[i];
               ++byteCount;
               terminatorFound = this.readNibble(realNumber, nibblesByte >> 4 & 15);
               if (!terminatorFound) {
                  terminatorFound = this.readNibble(realNumber, nibblesByte & 15);
               }
            } while(!terminatorFound);

            operands.add(Double.valueOf(realNumber.toString()));
            lastOperandLength += byteCount;
            operandLengths.add(byteCount);
         }
      }

      return dictEntries;
   }

   private boolean readNibble(StringBuilder realNumber, int nibble) {
      if (nibble <= 9) {
         realNumber.append(nibble);
      } else {
         switch (nibble) {
            case 10:
               realNumber.append(".");
               break;
            case 11:
               realNumber.append("E");
               break;
            case 12:
               realNumber.append("E-");
            case 13:
               break;
            case 14:
               realNumber.append("-");
               break;
            case 15:
               return true;
            default:
               throw new AssertionError("Unexpected nibble value");
         }
      }

      return false;
   }

   private byte[] readHeader() throws IOException {
      byte[] fixedHeader = this.cffData.readBytes(4);
      int hdrSize = fixedHeader[2] & 255;
      byte[] extra = this.cffData.readBytes(hdrSize - 4);
      byte[] header = new byte[hdrSize];

      int i;
      for(i = 0; i < fixedHeader.length; ++i) {
         header[i] = fixedHeader[i];
      }

      for(i = 4; i < extra.length; ++i) {
         header[i] = extra[i - 4];
      }

      return header;
   }

   public CFFIndexData readIndex(int offset) throws IOException {
      this.cffData.setPosition(offset);
      return this.readIndex();
   }

   private CFFIndexData readIndex() throws IOException {
      return this.readIndex(this.cffData);
   }

   public CFFIndexData readIndex(CFFDataInput input) throws IOException {
      CFFIndexData nameIndex = new CFFIndexData();
      if (input != null) {
         int origPos = input.getPosition();
         nameIndex.parseIndexHeader(input);
         int tableSize = input.getPosition() - origPos;
         nameIndex.setByteData(input.getPosition() - tableSize, tableSize);
      }

      return nameIndex;
   }

   public int getSIDFromGID(int charsetOffset, int gid) throws IOException {
      if (gid == 0) {
         return 0;
      } else {
         this.cffData.setPosition(charsetOffset);
         int charsetFormat = this.cffData.readCard8();
         switch (charsetFormat) {
            case 0:
               CFFDataInput var10000 = this.cffData;
               int var10001 = this.cffData.getPosition();
               --gid;
               var10000.setPosition(var10001 + gid * 2);
               return this.cffData.readSID();
            case 1:
               return this.getSIDFromGIDFormat(gid, 1);
            case 2:
               return this.getSIDFromGIDFormat(gid, 2);
            default:
               return 0;
         }
      }
   }

   private int getSIDFromGIDFormat(int gid, int format) throws IOException {
      int glyphCount = 0;

      int oldGlyphCount;
      int start;
      do {
         oldGlyphCount = glyphCount;
         start = this.cffData.readSID();
         glyphCount += (format == 1 ? this.cffData.readCard8() : this.cffData.readCard16()) + 1;
      } while(gid > glyphCount);

      return start + (gid - oldGlyphCount) - 1;
   }

   public byte[] getHeader() {
      return (byte[])this.header.clone();
   }

   public CFFIndexData getNameIndex() {
      return this.nameIndex;
   }

   public CFFIndexData getTopDictIndex() {
      return this.topDICTIndex;
   }

   public LinkedHashMap getTopDictEntries() {
      return this.topDict;
   }

   public CFFIndexData getStringIndex() {
      return this.stringIndex;
   }

   public CFFIndexData getGlobalIndexSubr() {
      return this.globalIndexSubr;
   }

   public CFFIndexData getLocalIndexSubr() {
      return this.localIndexSubr;
   }

   public CFFIndexData getCharStringIndex() {
      return this.charStringIndex;
   }

   public CFFDataInput getCFFData() {
      return this.cffData;
   }

   public CustomEncoding getEncoding() {
      return this.encoding;
   }

   public FDSelect getFDSelect() {
      return this.fdSelect;
   }

   public List getFDFonts() {
      return this.fdFonts;
   }

   public CFFDataInput getLocalSubrsForGlyph(int glyph) throws IOException {
      FDSelect fontDictionary = this.getFDSelect();
      int index;
      FontDict font;
      byte[] localSubrsData;
      if (fontDictionary instanceof Format0FDSelect) {
         Format0FDSelect fdSelect = (Format0FDSelect)fontDictionary;
         index = fdSelect.getFDIndexes()[glyph];
         font = (FontDict)this.getFDFonts().get(index);
         localSubrsData = font.getLocalSubrData().getByteData();
         return localSubrsData != null ? new CFFDataInput(localSubrsData) : null;
      } else if (!(fontDictionary instanceof Format3FDSelect)) {
         return null;
      } else {
         Format3FDSelect fdSelect = (Format3FDSelect)fontDictionary;
         index = 0;

         for(Iterator var5 = fdSelect.getRanges().keySet().iterator(); var5.hasNext(); ++index) {
            int first = (Integer)var5.next();
            if (first > glyph) {
               break;
            }
         }

         font = (FontDict)this.getFDFonts().get(index);
         localSubrsData = font.getLocalSubrData().getByteData();
         return localSubrsData != null ? new CFFDataInput(localSubrsData) : null;
      }
   }

   public CFFIndexData readCharStringIndex() throws IOException {
      int offset = ((Number)((DICTEntry)this.topDict.get("CharStrings")).getOperands().get(0)).intValue();
      this.cffData.setPosition(offset);
      return this.readIndex();
   }

   private CustomEncoding readEncoding() throws IOException {
      CustomEncoding foundEncoding = null;
      if (this.topDict.get("Encoding") != null) {
         int offset = ((Number)((DICTEntry)this.topDict.get("Encoding")).getOperands().get(0)).intValue();
         if (offset != 0 && offset != 1) {
            int format = this.cffData.readCard8();
            int numEntries = this.cffData.readCard8();
            switch (format) {
               case 0:
                  foundEncoding = this.readFormat0Encoding(format, numEntries);
                  break;
               case 1:
                  foundEncoding = this.readFormat1Encoding(format, numEntries);
            }
         }
      }

      return (CustomEncoding)foundEncoding;
   }

   private Format0Encoding readFormat0Encoding(int format, int numEntries) throws IOException {
      Format0Encoding newEncoding = new Format0Encoding();
      newEncoding.setFormat(format);
      newEncoding.setNumEntries(numEntries);
      int[] codes = new int[numEntries];

      for(int i = 0; i < numEntries; ++i) {
         codes[i] = this.cffData.readCard8();
      }

      newEncoding.setCodes(codes);
      return newEncoding;
   }

   private Format1Encoding readFormat1Encoding(int format, int numEntries) throws IOException {
      Format1Encoding newEncoding = new Format1Encoding();
      newEncoding.setFormat(format);
      newEncoding.setNumEntries(numEntries);
      Map ranges = new LinkedHashMap();

      for(int i = 0; i < numEntries; ++i) {
         int first = this.cffData.readCard8();
         int left = this.cffData.readCard8();
         ranges.put(first, left);
      }

      newEncoding.setRanges(ranges);
      return newEncoding;
   }

   private FDSelect readFDSelect() throws IOException {
      FDSelect fdSelect = null;
      DICTEntry fdSelectEntry = (DICTEntry)this.topDict.get("FDSelect");
      if (fdSelectEntry != null) {
         int fdOffset = ((Number)fdSelectEntry.getOperands().get(0)).intValue();
         this.cffData.setPosition(fdOffset);
         int format = this.cffData.readCard8();
         switch (format) {
            case 0:
               fdSelect = this.readFormat0FDSelect();
               break;
            case 3:
               fdSelect = this.readFormat3FDSelect();
         }
      }

      return (FDSelect)fdSelect;
   }

   private Format0FDSelect readFormat0FDSelect() throws IOException {
      Format0FDSelect newFDs = new Format0FDSelect();
      newFDs.setFormat(0);
      int glyphCount = this.charStringIndex.getNumObjects();
      int[] fds = new int[glyphCount];

      for(int i = 0; i < glyphCount; ++i) {
         fds[i] = this.cffData.readCard8();
      }

      newFDs.setFDIndexes(fds);
      return newFDs;
   }

   private Format3FDSelect readFormat3FDSelect() throws IOException {
      Format3FDSelect newFDs = new Format3FDSelect();
      newFDs.setFormat(3);
      int rangeCount = this.cffData.readCard16();
      newFDs.setRangeCount(rangeCount);
      Map ranges = new LinkedHashMap();

      for(int i = 0; i < rangeCount; ++i) {
         int first = this.cffData.readCard16();
         int fd = this.cffData.readCard8();
         ranges.put(first, fd);
      }

      newFDs.setRanges(ranges);
      newFDs.setSentinelGID(this.cffData.readCard16());
      return newFDs;
   }

   private List parseCIDData() throws IOException {
      List fdFonts = new ArrayList();
      if (this.topDict.get("ROS") != null) {
         DICTEntry fdArray = (DICTEntry)this.topDict.get("FDArray");
         if (fdArray != null) {
            int fdIndex = ((Number)fdArray.getOperands().get(0)).intValue();
            CFFIndexData fontDicts = this.readIndex(fdIndex);

            for(int i = 0; i < fontDicts.getNumObjects(); ++i) {
               FontDict newFontDict = new FontDict();
               byte[] fdData = fontDicts.getValue(i);
               Map fdEntries = this.parseDictData(fdData);
               newFontDict.setByteData(fontDicts.getValuePosition(i), fontDicts.getValueLength(i));
               DICTEntry fontFDEntry = (DICTEntry)fdEntries.get("FontName");
               if (fontFDEntry != null) {
                  newFontDict.setFontName(this.getString(((Number)fontFDEntry.getOperands().get(0)).intValue()));
               }

               DICTEntry privateFDEntry = (DICTEntry)fdEntries.get("Private");
               if (privateFDEntry != null) {
                  newFontDict = this.setFDData(privateFDEntry, newFontDict);
               }

               fdFonts.add(newFontDict);
            }
         }
      }

      return fdFonts;
   }

   private FontDict setFDData(DICTEntry privateFDEntry, FontDict newFontDict) throws IOException {
      int privateFDLength = ((Number)privateFDEntry.getOperands().get(0)).intValue();
      int privateFDOffset = ((Number)privateFDEntry.getOperands().get(1)).intValue();
      this.cffData.setPosition(privateFDOffset);
      byte[] privateDict = this.cffData.readBytes(privateFDLength);
      newFontDict.setPrivateDictData(privateFDOffset, privateFDLength);
      Map privateEntries = this.parseDictData(privateDict);
      DICTEntry subroutines = (DICTEntry)privateEntries.get("Subrs");
      if (subroutines != null) {
         CFFIndexData localSubrs = this.readIndex(privateFDOffset + ((Number)subroutines.getOperands().get(0)).intValue());
         newFontDict.setLocalSubrData(localSubrs);
      } else {
         newFontDict.setLocalSubrData(new CFFIndexData());
      }

      return newFontDict;
   }

   private String getString(int sid) throws IOException {
      return new String(this.stringIndex.getValue(sid - 391));
   }

   private CFFIndexData readLocalIndexSubrs() throws IOException {
      CFFIndexData localSubrs = null;
      DICTEntry privateEntry = (DICTEntry)this.topDict.get("Private");
      if (privateEntry != null) {
         int length = ((Number)privateEntry.getOperands().get(0)).intValue();
         int offset = ((Number)privateEntry.getOperands().get(1)).intValue();
         this.cffData.setPosition(offset);
         byte[] privateData = this.cffData.readBytes(length);
         Map privateDict = this.parseDictData(privateData);
         DICTEntry localSubrsEntry = (DICTEntry)privateDict.get("Subrs");
         if (localSubrsEntry != null) {
            int localOffset = offset + ((Number)localSubrsEntry.getOperands().get(0)).intValue();
            this.cffData.setPosition(localOffset);
            localSubrs = this.readIndex();
         }
      }

      return localSubrs;
   }

   private static class DataLocation {
      private int dataPosition;
      private int dataLength;

      public DataLocation() {
         this.dataPosition = 0;
         this.dataLength = 0;
      }

      public DataLocation(int position, int length) {
         this.dataPosition = position;
         this.dataLength = length;
      }

      public int getDataPosition() {
         return this.dataPosition;
      }

      public int getDataLength() {
         return this.dataLength;
      }
   }

   public class FontDict extends CFFSubTable {
      private String fontName;
      private DataLocation dataLocation = new DataLocation();
      private CFFIndexData localSubrData;

      public FontDict() {
         super();
      }

      public void setFontName(String groupName) {
         this.fontName = groupName;
      }

      public String getFontName() {
         return this.fontName;
      }

      public void setPrivateDictData(int position, int length) {
         this.dataLocation = new DataLocation(position, length);
      }

      public byte[] getPrivateDictData() throws IOException {
         int origPos = CFFDataReader.this.cffData.getPosition();

         byte[] var2;
         try {
            CFFDataReader.this.cffData.setPosition(this.dataLocation.getDataPosition());
            var2 = CFFDataReader.this.cffData.readBytes(this.dataLocation.getDataLength());
         } finally {
            CFFDataReader.this.cffData.setPosition(origPos);
         }

         return var2;
      }

      public void setLocalSubrData(CFFIndexData localSubrData) {
         this.localSubrData = localSubrData;
      }

      public CFFIndexData getLocalSubrData() {
         return this.localSubrData;
      }
   }

   public class Format3FDSelect extends FDSelect {
      private int rangeCount;
      private Map ranges;
      private int sentinelGID;

      public Format3FDSelect() {
         super();
      }

      public void setRangeCount(int rangeCount) {
         this.rangeCount = rangeCount;
      }

      public int getRangeCount() {
         return this.rangeCount;
      }

      public void setRanges(Map ranges) {
         this.ranges = ranges;
      }

      public Map getRanges() {
         return this.ranges;
      }

      public void setSentinelGID(int sentinelGID) {
         this.sentinelGID = sentinelGID;
      }

      public int getSentinelGID() {
         return this.sentinelGID;
      }
   }

   public class Format0FDSelect extends FDSelect {
      private int[] fds = new int[0];

      public Format0FDSelect() {
         super();
      }

      public void setFDIndexes(int[] fds) {
         this.fds = (int[])fds.clone();
      }

      public int[] getFDIndexes() {
         return (int[])this.fds.clone();
      }
   }

   public abstract class FDSelect {
      private int format;

      public void setFormat(int format) {
         this.format = format;
      }

      public int getFormat() {
         return this.format;
      }
   }

   public class Format1Encoding extends CustomEncoding {
      private Map ranges;

      public Format1Encoding() {
         super();
      }

      public void setRanges(Map ranges) {
         this.ranges = ranges;
      }

      public Map getRanges() {
         return this.ranges;
      }
   }

   public class Format0Encoding extends CustomEncoding {
      private int[] codes = new int[0];

      public Format0Encoding() {
         super();
      }

      public void setCodes(int[] codes) {
         this.codes = (int[])codes.clone();
      }

      public int[] getCodes() {
         return (int[])this.codes.clone();
      }
   }

   public abstract class CustomEncoding {
      private int format;
      private int numEntries;

      public void setFormat(int format) {
         this.format = format;
      }

      public int getFormat() {
         return this.format;
      }

      public void setNumEntries(int numEntries) {
         this.numEntries = numEntries;
      }

      public int getNumEntries() {
         return this.numEntries;
      }
   }

   public class CFFIndexData extends CFFSubTable {
      private int numObjects;
      private int offSize;
      private int[] offsets = new int[0];
      private DataLocation dataLocation = new DataLocation();

      public CFFIndexData() {
         super();
      }

      public void setNumObjects(int numObjects) {
         this.numObjects = numObjects;
      }

      public int getNumObjects() {
         return this.numObjects;
      }

      public void setOffSize(int offSize) {
         this.offSize = offSize;
      }

      public int getOffSize() {
         return this.offSize;
      }

      public void setOffsets(int[] offsets) {
         this.offsets = (int[])offsets.clone();
      }

      public int[] getOffsets() {
         return (int[])this.offsets.clone();
      }

      public void setData(int position, int length) {
         this.dataLocation = new DataLocation(position, length);
      }

      public byte[] getData() throws IOException {
         int origPos = CFFDataReader.this.cffData.getPosition();

         byte[] var2;
         try {
            CFFDataReader.this.cffData.setPosition(this.dataLocation.getDataPosition());
            var2 = CFFDataReader.this.cffData.readBytes(this.dataLocation.getDataLength());
         } finally {
            CFFDataReader.this.cffData.setPosition(origPos);
         }

         return var2;
      }

      public void parseIndexHeader(CFFDataInput cffData) throws IOException {
         this.setNumObjects(cffData.readCard16());
         this.setOffSize(cffData.readOffSize());
         int[] offsets = new int[this.getNumObjects() + 1];

         int i;
         for(i = 0; i <= this.getNumObjects(); ++i) {
            byte[] bytes;
            switch (this.getOffSize()) {
               case 1:
                  offsets[i] = cffData.readCard8();
                  break;
               case 2:
                  offsets[i] = cffData.readCard16();
                  break;
               case 3:
                  bytes = cffData.readBytes(3);
                  offsets[i] = ((bytes[0] & 255) << 16) + ((bytes[1] & 255) << 8) + (bytes[2] & 255);
                  break;
               case 4:
                  bytes = cffData.readBytes(4);
                  offsets[i] = ((bytes[0] & 255) << 24) + ((bytes[1] & 255) << 16) + ((bytes[2] & 255) << 8) + (bytes[3] & 255);
            }
         }

         this.setOffsets(offsets);
         i = cffData.getPosition();
         int dataSize = offsets[offsets.length - 1] - offsets[0];
         cffData.setPosition(cffData.getPosition() + dataSize);
         this.setData(i, dataSize);
      }

      public byte[] getValue(int index) throws IOException {
         int oldPos = CFFDataReader.this.cffData.getPosition();

         byte[] var3;
         try {
            CFFDataReader.this.cffData.setPosition(this.dataLocation.getDataPosition() + (this.offsets[index] - 1));
            var3 = CFFDataReader.this.cffData.readBytes(this.offsets[index + 1] - this.offsets[index]);
         } finally {
            CFFDataReader.this.cffData.setPosition(oldPos);
         }

         return var3;
      }

      public int getValuePosition(int index) {
         return this.dataLocation.getDataPosition() + (this.offsets[index] - 1);
      }

      public int getValueLength(int index) {
         return this.offsets[index + 1] - this.offsets[index];
      }
   }

   public class CFFSubTable {
      private DataLocation dataLocation = new DataLocation();

      public void setByteData(int position, int length) {
         this.dataLocation = new DataLocation(position, length);
      }

      public byte[] getByteData() throws IOException {
         int oldPos = CFFDataReader.this.cffData.getPosition();

         byte[] var2;
         try {
            CFFDataReader.this.cffData.setPosition(this.dataLocation.getDataPosition());
            var2 = CFFDataReader.this.cffData.readBytes(this.dataLocation.getDataLength());
         } finally {
            CFFDataReader.this.cffData.setPosition(oldPos);
         }

         return var2;
      }
   }

   public static class DICTEntry {
      private int[] operator;
      private List operands;
      private List operandLengths;
      private String operatorName;
      private int offset;
      private int operandLength;
      private byte[] data = new byte[0];

      public void setOperator(int[] operator) {
         this.operator = operator;
      }

      public int[] getOperator() {
         return this.operator;
      }

      public void setOperands(List operands) {
         this.operands = operands;
      }

      public List getOperands() {
         return this.operands;
      }

      public void setOperatorName(String operatorName) {
         this.operatorName = operatorName;
      }

      public String getOperatorName() {
         return this.operatorName;
      }

      public void setOffset(int offset) {
         this.offset = offset;
      }

      public int getOffset() {
         return this.offset;
      }

      public void setOperandLength(int operandLength) {
         this.operandLength = operandLength;
      }

      public int getOperandLength() {
         return this.operandLength;
      }

      public void setByteData(byte[] data) {
         this.data = (byte[])data.clone();
      }

      public byte[] getByteData() {
         return (byte[])this.data.clone();
      }

      public void setOperandLengths(List operandLengths) {
         this.operandLengths = operandLengths;
      }

      public List getOperandLengths() {
         return this.operandLengths;
      }
   }
}
