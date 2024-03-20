package org.apache.fop.fonts.truetype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFDataInput;
import org.apache.fontbox.cff.CFFStandardString;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.CharStringCommand;
import org.apache.fontbox.cff.Type2CharString;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.cff.CFFDataReader;
import org.apache.fop.fonts.type1.AdobeStandardEncoding;

public class OTFSubSetFile extends OTFSubSetWriter {
   protected Map subsetGlyphs = new LinkedHashMap();
   protected Map gidToSID;
   protected CFFDataReader.CFFIndexData localIndexSubr;
   protected CFFDataReader.CFFIndexData globalIndexSubr;
   protected List subsetLocalIndexSubr;
   protected List subsetGlobalIndexSubr;
   protected List fdSubrs;
   private Map subsetFDSelect;
   protected List localUniques;
   protected List globalUniques;
   protected int subsetLocalSubrCount;
   protected int subsetGlobalSubrCount;
   protected List subsetCharStringsIndex;
   protected String embeddedName;
   protected List stringIndexData = new ArrayList();
   protected CFFDataReader cffReader;
   private MultiByteFont mbFont;
   public static final int NUM_STANDARD_STRINGS = 391;
   private static final int LOCAL_SUBROUTINE = 10;
   private static final int GLOBAL_SUBROUTINE = 29;
   private static final String ACCENT_CMD = "seac";
   private Type2Parser type2Parser;

   public OTFSubSetFile() throws IOException {
   }

   public void readFont(FontFileReader in, String embeddedName, MultiByteFont mbFont) throws IOException {
      this.readFont(in, embeddedName, mbFont, mbFont.getUsedGlyphs());
   }

   void readFont(FontFileReader in, String embeddedName, MultiByteFont mbFont, Map usedGlyphs) throws IOException {
      this.mbFont = mbFont;
      this.fontFile = in;
      this.embeddedName = embeddedName;
      this.initializeFont(in);
      this.cffReader = new CFFDataReader(this.fontFile);
      this.mapChars(usedGlyphs);
      this.subsetGlyphs = this.sortByValue(usedGlyphs);
      this.createCFF();
   }

   private void mapChars(Map usedGlyphs) throws IOException {
      if (this.fileFont instanceof CFFType1Font) {
         CFFType1Font cffType1Font = (CFFType1Font)this.fileFont;
         this.subsetGlyphs = this.sortByValue(usedGlyphs);
         Iterator var3 = this.subsetGlyphs.keySet().iterator();

         while(var3.hasNext()) {
            int gid = (Integer)var3.next();
            Type2CharString type2CharString = cffType1Font.getType2CharString(gid);
            List stack = new ArrayList();
            Iterator var7 = type2CharString.getType1Sequence().iterator();

            while(var7.hasNext()) {
               Object obj = var7.next();
               if (obj instanceof CharStringCommand) {
                  String name = (String)CharStringCommand.TYPE1_VOCABULARY.get(((CharStringCommand)obj).getKey());
                  if ("seac".equals(name)) {
                     int first = ((Number)stack.get(3)).intValue();
                     int second = ((Number)stack.get(4)).intValue();
                     this.mbFont.mapChar(AdobeStandardEncoding.getUnicodeFromCodePoint(first));
                     this.mbFont.mapChar(AdobeStandardEncoding.getUnicodeFromCodePoint(second));
                  }

                  stack.clear();
               } else {
                  stack.add((Number)obj);
               }
            }
         }
      }

   }

   private Map sortByValue(Map map) {
      List list = new ArrayList(map.entrySet());
      Collections.sort(list, new Comparator() {
         public int compare(Map.Entry o1, Map.Entry o2) {
            return ((Comparable)o1.getValue()).compareTo(o2.getValue());
         }
      });
      Map result = new LinkedHashMap();
      Iterator var4 = list.iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         result.put(entry.getKey(), entry.getValue());
      }

      return result;
   }

   protected void createCFF() throws IOException {
      this.writeBytes(this.cffReader.getHeader());
      this.writeIndex(Arrays.asList(this.embedFontName.getBytes("UTF-8")));
      Offsets offsets = new Offsets();
      offsets.topDictData = this.currentPos + this.writeTopDICT();
      boolean hasFDSelect = this.cffReader.getFDSelect() != null;
      if (hasFDSelect) {
         this.createCharStringDataCID();
      } else {
         this.createCharStringData();
      }

      List fontNameSIDs = null;
      List subsetFDFonts = null;
      if (hasFDSelect) {
         subsetFDFonts = this.getUsedFDFonts();
         fontNameSIDs = this.storeFDStrings(subsetFDFonts);
      }

      this.writeStringIndex();
      this.writeIndex(this.subsetGlobalIndexSubr);
      offsets.encoding = this.currentPos;
      offsets.charset = this.currentPos;
      this.writeCharsetTable(hasFDSelect);
      offsets.fdSelect = this.currentPos;
      if (hasFDSelect) {
         this.writeFDSelect();
         if (!this.isCharStringBeforeFD()) {
            offsets.fdArray = this.writeFDArray(subsetFDFonts, fontNameSIDs);
         }
      }

      offsets.charString = this.currentPos;
      this.writeIndex(this.subsetCharStringsIndex);
      if (hasFDSelect) {
         if (this.isCharStringBeforeFD()) {
            offsets.fdArray = this.writeFDArray(subsetFDFonts, fontNameSIDs);
         }

         this.updateCIDOffsets(offsets);
      } else {
         offsets.privateDict = this.currentPos;
         this.writePrivateDict();
         offsets.localIndex = this.currentPos;
         this.writeIndex(this.subsetLocalIndexSubr);
         this.updateOffsets(offsets);
      }

   }

   private int writeFDArray(List subsetFDFonts, List fontNameSIDs) throws IOException {
      List privateDictOffsets = this.writeCIDDictsAndSubrs(subsetFDFonts);
      return this.writeFDArray(subsetFDFonts, privateDictOffsets, fontNameSIDs);
   }

   private boolean isCharStringBeforeFD() {
      LinkedHashMap entries = this.cffReader.getTopDictEntries();
      int len = ((CFFDataReader.DICTEntry)entries.get("CharStrings")).getOperandLength();
      if (entries.containsKey("FDArray")) {
         int len2 = ((CFFDataReader.DICTEntry)entries.get("FDArray")).getOperandLength();
         return len < len2;
      } else {
         return true;
      }
   }

   protected List storeFDStrings(List uniqueNewRefs) throws IOException {
      List fontNameSIDs = new ArrayList();
      List fdFonts = this.cffReader.getFDFonts();
      Iterator var4 = uniqueNewRefs.iterator();

      while(var4.hasNext()) {
         int uniqueNewRef = (Integer)var4.next();
         CFFDataReader.FontDict fdFont = (CFFDataReader.FontDict)fdFonts.get(uniqueNewRef);
         byte[] fdFontByteData = fdFont.getByteData();
         Map fdFontDict = this.cffReader.parseDictData(fdFontByteData);
         fontNameSIDs.add(this.stringIndexData.size() + 391);
         this.stringIndexData.add(this.cffReader.getStringIndex().getValue(((Number)((CFFDataReader.DICTEntry)fdFontDict.get("FontName")).getOperands().get(0)).intValue() - 391));
      }

      return fontNameSIDs;
   }

   protected int writeTopDICT() throws IOException {
      Map topDICT = this.cffReader.getTopDictEntries();
      List topDictStringEntries = Arrays.asList("version", "Notice", "Copyright", "FullName", "FamilyName", "Weight", "PostScript");
      ByteArrayOutputStream dict = new ByteArrayOutputStream();
      int offsetExtra = 0;
      Iterator var5 = topDICT.entrySet().iterator();

      while(true) {
         while(var5.hasNext()) {
            Map.Entry dictEntry = (Map.Entry)var5.next();
            String dictKey = (String)dictEntry.getKey();
            CFFDataReader.DICTEntry entry = (CFFDataReader.DICTEntry)dictEntry.getValue();
            entry.setOffset(entry.getOffset() + offsetExtra);
            if (dictKey.equals("CharStrings") && entry.getOperandLength() < 5) {
               byte[] extra = new byte[5 - entry.getOperandLength()];
               offsetExtra += extra.length;
               dict.write(extra);
               dict.write(entry.getByteData());
               entry.setOperandLength(5);
            } else if (dictKey.equals("ROS")) {
               dict.write(this.writeROSEntry(entry));
            } else if (dictKey.equals("CIDCount")) {
               dict.write(this.writeCIDCount(entry));
            } else if (topDictStringEntries.contains(dictKey)) {
               if (entry.getOperandLength() < 2) {
                  entry.setOperandLength(2);
                  ++offsetExtra;
               }

               dict.write(this.writeTopDictStringEntry(entry));
            } else {
               dict.write(entry.getByteData());
            }
         }

         byte[] topDictIndex = this.cffReader.getTopDictIndex().getByteData();
         int offSize = topDictIndex[2];
         return this.writeIndex(Arrays.asList(dict.toByteArray()), offSize) - dict.size();
      }
   }

   private byte[] writeROSEntry(CFFDataReader.DICTEntry dictEntry) throws IOException {
      int sidA = ((Number)dictEntry.getOperands().get(0)).intValue();
      if (sidA > 390) {
         this.stringIndexData.add(this.cffReader.getStringIndex().getValue(sidA - 391));
      }

      int sidAStringIndex = this.stringIndexData.size() + 390;
      int sidB = ((Number)dictEntry.getOperands().get(1)).intValue();
      if (sidB > 390) {
         this.stringIndexData.add("Identity".getBytes("UTF-8"));
      }

      int sidBStringIndex = this.stringIndexData.size() + 390;
      byte[] cidEntryByteData = dictEntry.getByteData();
      this.updateOffset(cidEntryByteData, 0, (Integer)dictEntry.getOperandLengths().get(0), sidAStringIndex);
      this.updateOffset(cidEntryByteData, (Integer)dictEntry.getOperandLengths().get(0), (Integer)dictEntry.getOperandLengths().get(1), sidBStringIndex);
      this.updateOffset(cidEntryByteData, (Integer)dictEntry.getOperandLengths().get(0) + (Integer)dictEntry.getOperandLengths().get(1), (Integer)dictEntry.getOperandLengths().get(2), 0);
      return cidEntryByteData;
   }

   protected byte[] writeCIDCount(CFFDataReader.DICTEntry dictEntry) throws IOException {
      byte[] cidCountByteData = dictEntry.getByteData();
      this.updateOffset(cidCountByteData, 0, (Integer)dictEntry.getOperandLengths().get(0), this.subsetGlyphs.size());
      return cidCountByteData;
   }

   private byte[] writeTopDictStringEntry(CFFDataReader.DICTEntry dictEntry) throws IOException {
      int sid = ((Number)dictEntry.getOperands().get(0)).intValue();
      if (sid > 391) {
         this.stringIndexData.add(this.cffReader.getStringIndex().getValue(sid - 391));
      }

      byte[] newDictEntry = createNewRef(this.stringIndexData.size() + 390, dictEntry.getOperator(), dictEntry.getOperandLength(), true);
      return newDictEntry;
   }

   private void writeStringIndex() throws IOException {
      Map topDICT = this.cffReader.getTopDictEntries();
      int charsetOffset = ((Number)((CFFDataReader.DICTEntry)topDICT.get("charset")).getOperands().get(0)).intValue();
      this.gidToSID = new LinkedHashMap();
      Iterator var3 = this.subsetGlyphs.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry subsetGlyph = (Map.Entry)var3.next();
         int gid = (Integer)subsetGlyph.getKey();
         int v = (Integer)subsetGlyph.getValue();
         int sid = this.cffReader.getSIDFromGID(charsetOffset, gid);
         if (sid < 391) {
            this.gidToSID.put(v, sid);
            if (this.mbFont != null) {
               this.mbFont.mapUsedGlyphName(v, CFFStandardString.getName(sid));
            }
         } else {
            int index = sid - 391;
            if (index < this.cffReader.getStringIndex().getNumObjects()) {
               byte[] value = this.cffReader.getStringIndex().getValue(index);
               if (this.mbFont != null) {
                  this.mbFont.mapUsedGlyphName(v, new String(value, "UTF-8"));
               }

               this.gidToSID.put(v, this.stringIndexData.size() + 391);
               this.stringIndexData.add(value);
            } else {
               if (this.mbFont != null) {
                  this.mbFont.mapUsedGlyphName(v, ".notdef");
               }

               this.gidToSID.put(v, index);
            }
         }
      }

      this.writeIndex(this.stringIndexData);
   }

   protected void createCharStringDataCID() throws IOException {
      CFFDataReader.CFFIndexData charStringsIndex = this.cffReader.getCharStringIndex();
      CFFDataReader.FDSelect fontDictionary = this.cffReader.getFDSelect();
      if (fontDictionary instanceof CFFDataReader.Format0FDSelect) {
         throw new UnsupportedOperationException("OTF CFF CID Format0 currently not implemented");
      } else {
         if (fontDictionary instanceof CFFDataReader.Format3FDSelect) {
            CFFDataReader.Format3FDSelect fdSelect = (CFFDataReader.Format3FDSelect)fontDictionary;
            Map subsetGroups = new HashMap();
            List uniqueGroups = new ArrayList();
            Map rangeMap = fdSelect.getRanges();
            Integer[] ranges = (Integer[])rangeMap.keySet().toArray(new Integer[rangeMap.size()]);
            Iterator var8 = this.subsetGlyphs.keySet().iterator();

            int i;
            Iterator var11;
            Map.Entry subsetGlyph;
            int gid;
            int value;
            while(var8.hasNext()) {
               int gid = (Integer)var8.next();
               i = 0;

               for(var11 = rangeMap.entrySet().iterator(); var11.hasNext(); ++i) {
                  subsetGlyph = (Map.Entry)var11.next();
                  if (i < ranges.length - 1) {
                     gid = ranges[i + 1];
                  } else {
                     gid = fdSelect.getSentinelGID();
                  }

                  if (gid >= (Integer)subsetGlyph.getKey() && gid < gid) {
                     value = (Integer)subsetGlyph.getValue();
                     subsetGroups.put(gid, value);
                     if (!uniqueGroups.contains(value)) {
                        uniqueGroups.add(value);
                     }
                  }
               }
            }

            this.globalIndexSubr = this.cffReader.getGlobalIndexSubr();
            this.subsetCharStringsIndex = new ArrayList();
            this.globalUniques = new ArrayList();
            this.subsetFDSelect = new LinkedHashMap();
            List foundLocalUniques = new ArrayList();
            Iterator var19 = uniqueGroups.iterator();

            while(var19.hasNext()) {
               i = (Integer)var19.next();
               foundLocalUniques.add(new ArrayList());
            }

            Map gidHintMaskLengths = new HashMap();
            Iterator var21 = this.subsetGlyphs.entrySet().iterator();

            int u;
            while(var21.hasNext()) {
               Map.Entry subsetGlyph = (Map.Entry)var21.next();
               u = (Integer)subsetGlyph.getKey();
               gid = (Integer)subsetGroups.get(u);
               this.localIndexSubr = ((CFFDataReader.FontDict)this.cffReader.getFDFonts().get(gid)).getLocalSubrData();
               this.localUniques = (List)foundLocalUniques.get(uniqueGroups.indexOf(gid));
               this.type2Parser = new Type2Parser();
               FDIndexReference newFDReference = new FDIndexReference(uniqueGroups.indexOf(gid), gid);
               this.subsetFDSelect.put(subsetGlyph.getValue(), newFDReference);
               byte[] data = charStringsIndex.getValue(u);
               this.preScanForSubsetIndexSize(data);
               gidHintMaskLengths.put(u, this.type2Parser.getMaskLength());
            }

            this.subsetGlobalIndexSubr = new ArrayList();
            this.fdSubrs = new ArrayList();
            this.subsetGlobalSubrCount = this.globalUniques.size();
            this.globalUniques.clear();
            this.localUniques = null;
            var21 = foundLocalUniques.iterator();

            while(var21.hasNext()) {
               List foundLocalUnique = (List)var21.next();
               this.fdSubrs.add(new ArrayList());
            }

            List foundLocalUniquesB = new ArrayList();
            var11 = uniqueGroups.iterator();

            while(var11.hasNext()) {
               u = (Integer)var11.next();
               foundLocalUniquesB.add(new ArrayList());
            }

            var11 = this.subsetGlyphs.entrySet().iterator();

            while(var11.hasNext()) {
               subsetGlyph = (Map.Entry)var11.next();
               gid = (Integer)subsetGlyph.getKey();
               value = (Integer)subsetGlyph.getValue();
               int group = (Integer)subsetGroups.get(gid);
               this.localIndexSubr = ((CFFDataReader.FontDict)this.cffReader.getFDFonts().get(group)).getLocalSubrData();
               int newFDIndex = ((FDIndexReference)this.subsetFDSelect.get(value)).getNewFDIndex();
               this.localUniques = (List)foundLocalUniquesB.get(newFDIndex);
               byte[] data = charStringsIndex.getValue(gid);
               this.subsetLocalIndexSubr = (List)this.fdSubrs.get(newFDIndex);
               this.subsetLocalSubrCount = ((List)foundLocalUniques.get(newFDIndex)).size();
               this.type2Parser = new Type2Parser();
               this.type2Parser.setMaskLength((Integer)gidHintMaskLengths.get(gid));
               data = this.readCharStringData(data, this.subsetLocalSubrCount);
               this.subsetCharStringsIndex.add(data);
            }
         }

      }
   }

   protected void writeFDSelect() {
      if (((CFFDataReader.DICTEntry)this.cffReader.getTopDictEntries().get("CharStrings")).getOperandLength() == 2) {
         Map indexs = this.getFormat3Index();
         this.writeByte(3);
         this.writeCard16(indexs.size());
         int count = 0;

         Map.Entry x;
         for(Iterator var3 = indexs.entrySet().iterator(); var3.hasNext(); count += (Integer)x.getValue()) {
            x = (Map.Entry)var3.next();
            this.writeCard16(count);
            this.writeByte((Integer)x.getKey());
         }

         this.writeCard16(this.subsetFDSelect.size());
      } else {
         this.writeByte(0);
         Iterator var5 = this.subsetFDSelect.values().iterator();

         while(var5.hasNext()) {
            FDIndexReference e = (FDIndexReference)var5.next();
            this.writeByte(e.getNewFDIndex());
         }
      }

   }

   private Map getFormat3Index() {
      Map indexs = new LinkedHashMap();
      int last = -1;
      int count = 0;

      int i;
      for(Iterator var4 = this.subsetFDSelect.values().iterator(); var4.hasNext(); last = i) {
         FDIndexReference e = (FDIndexReference)var4.next();
         i = e.getNewFDIndex();
         ++count;
         if (i != last) {
            indexs.put(i, count);
            count = 1;
         }
      }

      indexs.put(last, count);
      return indexs;
   }

   protected List getUsedFDFonts() {
      List uniqueNewRefs = new ArrayList();
      Iterator var2 = this.subsetFDSelect.values().iterator();

      while(var2.hasNext()) {
         FDIndexReference e = (FDIndexReference)var2.next();
         int fdIndex = e.getOldFDIndex();
         if (!uniqueNewRefs.contains(fdIndex)) {
            uniqueNewRefs.add(fdIndex);
         }
      }

      return uniqueNewRefs;
   }

   protected List writeCIDDictsAndSubrs(List uniqueNewRefs) throws IOException {
      List privateDictOffsets = new ArrayList();
      List fdFonts = this.cffReader.getFDFonts();
      int i = 0;

      for(Iterator var5 = uniqueNewRefs.iterator(); var5.hasNext(); ++i) {
         int ref = (Integer)var5.next();
         CFFDataReader.FontDict curFDFont = (CFFDataReader.FontDict)fdFonts.get(ref);
         byte[] fdPrivateDictByteData = curFDFont.getPrivateDictData();
         Map fdPrivateDict = this.cffReader.parseDictData(fdPrivateDictByteData);
         int privateDictOffset = this.currentPos;
         privateDictOffsets.add(privateDictOffset);
         CFFDataReader.DICTEntry subrs = (CFFDataReader.DICTEntry)fdPrivateDict.get("Subrs");
         if (subrs != null) {
            fdPrivateDictByteData = this.resizeToFitOpLen(fdPrivateDictByteData, subrs);
            this.updateOffset(fdPrivateDictByteData, subrs.getOffset(), subrs.getOperandLength(), fdPrivateDictByteData.length);
         }

         this.writeBytes(fdPrivateDictByteData);
         this.writeIndex((List)this.fdSubrs.get(i));
      }

      return privateDictOffsets;
   }

   private byte[] resizeToFitOpLen(byte[] fdPrivateDictByteData, CFFDataReader.DICTEntry subrs) throws IOException {
      if (subrs.getOperandLength() == 2 && fdPrivateDictByteData.length < 108) {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         bos.write(fdPrivateDictByteData);
         bos.write(new byte[108 - fdPrivateDictByteData.length]);
         fdPrivateDictByteData = bos.toByteArray();
      }

      return fdPrivateDictByteData;
   }

   protected int writeFDArray(List uniqueNewRefs, List privateDictOffsets, List fontNameSIDs) throws IOException {
      int offset = this.currentPos;
      List fdFonts = this.cffReader.getFDFonts();
      List index = new ArrayList();
      int i = 0;

      for(Iterator var8 = uniqueNewRefs.iterator(); var8.hasNext(); ++i) {
         int ref = (Integer)var8.next();
         CFFDataReader.FontDict fdFont = (CFFDataReader.FontDict)fdFonts.get(ref);
         byte[] fdFontByteData = fdFont.getByteData();
         Map fdFontDict = this.cffReader.parseDictData(fdFontByteData);
         this.updateOffset(fdFontByteData, ((CFFDataReader.DICTEntry)fdFontDict.get("FontName")).getOffset() - 1, (Integer)((CFFDataReader.DICTEntry)fdFontDict.get("FontName")).getOperandLengths().get(0), (Integer)fontNameSIDs.get(i));
         this.updateOffset(fdFontByteData, ((CFFDataReader.DICTEntry)fdFontDict.get("Private")).getOffset() + (Integer)((CFFDataReader.DICTEntry)fdFontDict.get("Private")).getOperandLengths().get(0), (Integer)((CFFDataReader.DICTEntry)fdFontDict.get("Private")).getOperandLengths().get(1), (Integer)privateDictOffsets.get(i));
         index.add(fdFontByteData);
      }

      this.writeIndex(index);
      return offset;
   }

   private void createCharStringData() throws IOException {
      Map topDICT = this.cffReader.getTopDictEntries();
      CFFDataReader.CFFIndexData charStringsIndex = this.cffReader.getCharStringIndex();
      CFFDataReader.DICTEntry privateEntry = (CFFDataReader.DICTEntry)topDICT.get("Private");
      int gid;
      if (privateEntry != null) {
         int privateOffset = ((Number)privateEntry.getOperands().get(1)).intValue();
         Map privateDICT = this.cffReader.getPrivateDict(privateEntry);
         if (privateDICT.get("Subrs") != null) {
            gid = privateOffset + ((Number)((CFFDataReader.DICTEntry)privateDICT.get("Subrs")).getOperands().get(0)).intValue();
            this.localIndexSubr = this.cffReader.readIndex(gid);
         } else {
            this.localIndexSubr = this.cffReader.readIndex((CFFDataInput)null);
         }
      }

      this.globalIndexSubr = this.cffReader.getGlobalIndexSubr();
      this.subsetLocalIndexSubr = new ArrayList();
      this.subsetGlobalIndexSubr = new ArrayList();
      this.subsetCharStringsIndex = new ArrayList();
      this.localUniques = new ArrayList();
      this.globalUniques = new ArrayList();
      Map gidHintMaskLengths = new HashMap();
      Iterator var9 = this.subsetGlyphs.keySet().iterator();

      byte[] data;
      while(var9.hasNext()) {
         gid = (Integer)var9.next();
         this.type2Parser = new Type2Parser();
         data = charStringsIndex.getValue(gid);
         this.preScanForSubsetIndexSize(data);
         gidHintMaskLengths.put(gid, this.type2Parser.getMaskLength());
      }

      this.subsetLocalSubrCount = this.localUniques.size();
      this.subsetGlobalSubrCount = this.globalUniques.size();
      this.localUniques.clear();
      this.globalUniques.clear();
      var9 = this.subsetGlyphs.keySet().iterator();

      while(var9.hasNext()) {
         gid = (Integer)var9.next();
         data = charStringsIndex.getValue(gid);
         this.type2Parser = new Type2Parser();
         this.type2Parser.setMaskLength((Integer)gidHintMaskLengths.get(gid));
         data = this.readCharStringData(data, this.subsetLocalSubrCount);
         this.subsetCharStringsIndex.add(data);
      }

   }

   private void preScanForSubsetIndexSize(byte[] data) throws IOException {
      boolean hasLocalSubroutines = this.localIndexSubr != null && this.localIndexSubr.getNumObjects() > 0;
      boolean hasGlobalSubroutines = this.globalIndexSubr != null && this.globalIndexSubr.getNumObjects() > 0;

      for(int dataPos = 0; dataPos < data.length; ++dataPos) {
         int b0 = data[dataPos] & 255;
         if (b0 == 10 && hasLocalSubroutines) {
            this.preScanForSubsetIndexSize(this.localIndexSubr, this.localUniques);
         } else if (b0 == 29 && hasGlobalSubroutines) {
            this.preScanForSubsetIndexSize(this.globalIndexSubr, this.globalUniques);
         } else {
            dataPos += this.type2Parser.exec(b0, data, dataPos);
         }
      }

   }

   private void preScanForSubsetIndexSize(CFFDataReader.CFFIndexData indexSubr, List uniques) throws IOException {
      int subrNumber = this.getSubrNumber(indexSubr.getNumObjects(), this.type2Parser.popOperand().getNumber());
      if (!uniques.contains(subrNumber) && subrNumber < indexSubr.getNumObjects()) {
         uniques.add(subrNumber);
      }

      if (subrNumber < indexSubr.getNumObjects()) {
         byte[] subr = indexSubr.getValue(subrNumber);
         this.preScanForSubsetIndexSize(subr);
      } else {
         throw new IllegalArgumentException("callgsubr out of range");
      }
   }

   private int getSubrNumber(int numSubroutines, int operand) {
      int bias = this.getBias(numSubroutines);
      return bias + operand;
   }

   private byte[] readCharStringData(byte[] data, int subsetLocalSubrCount) throws IOException {
      boolean hasLocalSubroutines = this.localIndexSubr != null && this.localIndexSubr.getNumObjects() > 0;
      boolean hasGlobalSubroutines = this.globalIndexSubr != null && this.globalIndexSubr.getNumObjects() > 0;

      for(int dataPos = 0; dataPos < data.length; ++dataPos) {
         int b0 = data[dataPos] & 255;
         BytesNumber operand;
         int subrNumber;
         int newRef;
         byte[] newData;
         if (b0 == 10 && hasLocalSubroutines) {
            operand = this.type2Parser.popOperand();
            subrNumber = this.getSubrNumber(this.localIndexSubr.getNumObjects(), operand.getNumber());
            newRef = this.getNewRefForReference(subrNumber, this.localUniques, this.localIndexSubr, this.subsetLocalIndexSubr, subsetLocalSubrCount);
            if (newRef != -1) {
               newData = this.constructNewRefData(dataPos, data, operand, subsetLocalSubrCount, newRef, new int[]{10});
               dataPos -= data.length - newData.length;
               data = newData;
            }
         } else if (b0 == 29 && hasGlobalSubroutines) {
            operand = this.type2Parser.popOperand();
            subrNumber = this.getSubrNumber(this.globalIndexSubr.getNumObjects(), operand.getNumber());
            newRef = this.getNewRefForReference(subrNumber, this.globalUniques, this.globalIndexSubr, this.subsetGlobalIndexSubr, this.subsetGlobalSubrCount);
            if (newRef != -1) {
               newData = this.constructNewRefData(dataPos, data, operand, this.subsetGlobalSubrCount, newRef, new int[]{29});
               dataPos -= data.length - newData.length;
               data = newData;
            }
         } else {
            dataPos += this.type2Parser.exec(b0, data, dataPos);
         }
      }

      return data;
   }

   private int getNewRefForReference(int subrNumber, List uniquesArray, CFFDataReader.CFFIndexData indexSubr, List subsetIndexSubr, int subrCount) throws IOException {
      int newRef;
      if (!uniquesArray.contains(subrNumber)) {
         if (subrNumber >= indexSubr.getNumObjects()) {
            throw new IllegalArgumentException("subrNumber out of range");
         }

         byte[] subr = indexSubr.getValue(subrNumber);
         subr = this.readCharStringData(subr, subrCount);
         uniquesArray.add(subrNumber);
         subsetIndexSubr.add(subr);
         newRef = subsetIndexSubr.size() - 1;
      } else {
         newRef = uniquesArray.indexOf(subrNumber);
      }

      return newRef;
   }

   private int getBias(int subrCount) {
      if (subrCount < 1240) {
         return 107;
      } else {
         return subrCount < 33900 ? 1131 : '耀';
      }
   }

   private byte[] constructNewRefData(int curDataPos, byte[] currentData, BytesNumber operand, int fullSubsetIndexSize, int curSubsetIndexSize, int[] operatorCode) throws IOException {
      ByteArrayOutputStream newData = new ByteArrayOutputStream();
      int startRef = curDataPos - operand.getNumBytes();
      int length = operand.getNumBytes() + 1;
      int newBias = this.getBias(fullSubsetIndexSize);
      int newRef = curSubsetIndexSize - newBias;
      byte[] newRefBytes = createNewRef(newRef, operatorCode, -1, false);
      newData.write(currentData, 0, startRef);
      newData.write(newRefBytes);
      newData.write(currentData, startRef + length, currentData.length - (startRef + length));
      return newData.toByteArray();
   }

   public static byte[] createNewRef(int newRef, int[] operatorCode, int forceLength, boolean isDict) {
      ByteArrayOutputStream newRefBytes = new ByteArrayOutputStream();
      if ((forceLength != -1 || newRef < -107 || newRef > 107) && forceLength != 1) {
         if ((forceLength != -1 || newRef < -1131 || newRef > 1131) && forceLength != 2) {
            if ((forceLength != -1 || newRef < -32768 || newRef > 32767) && forceLength != 3) {
               if (isDict) {
                  newRefBytes.write(29);
               } else {
                  newRefBytes.write(255);
               }

               newRefBytes.write(newRef >> 24);
               newRefBytes.write(newRef >> 16);
               newRefBytes.write(newRef >> 8);
               newRefBytes.write(newRef);
            } else {
               newRefBytes.write(28);
               newRefBytes.write(newRef >> 8);
               newRefBytes.write(newRef);
            }
         } else {
            if (newRef <= -876) {
               newRefBytes.write(254);
            } else if (newRef <= -620) {
               newRefBytes.write(253);
            } else if (newRef <= -364) {
               newRefBytes.write(252);
            } else if (newRef <= -108) {
               newRefBytes.write(251);
            } else if (newRef <= 363) {
               newRefBytes.write(247);
            } else if (newRef <= 619) {
               newRefBytes.write(248);
            } else if (newRef <= 875) {
               newRefBytes.write(249);
            } else {
               newRefBytes.write(250);
            }

            if (newRef > 0) {
               newRefBytes.write(newRef - 108);
            } else {
               newRefBytes.write(-newRef - 108);
            }
         }
      } else {
         newRefBytes.write(newRef + 139);
      }

      int[] var5 = operatorCode;
      int var6 = operatorCode.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         int i = var5[var7];
         newRefBytes.write(i);
      }

      return newRefBytes.toByteArray();
   }

   protected int writeIndex(List dataArray) {
      int totLength = 1;

      byte[] data;
      for(Iterator var3 = dataArray.iterator(); var3.hasNext(); totLength += data.length) {
         data = (byte[])var3.next();
      }

      int offSize = this.getOffSize(totLength);
      return this.writeIndex(dataArray, offSize);
   }

   protected int writeIndex(List dataArray, int offSize) {
      int hdrTotal = 3;
      this.writeCard16(dataArray.size());
      this.writeByte(offSize);
      hdrTotal += offSize;
      int total = 0;
      int i = 0;

      Iterator var6;
      byte[] aDataArray;
      for(var6 = dataArray.iterator(); var6.hasNext(); ++i) {
         aDataArray = (byte[])var6.next();
         hdrTotal += offSize;
         int length = aDataArray.length;
         switch (offSize) {
            case 1:
               if (i == 0) {
                  this.writeByte(1);
               }

               total += length;
               this.writeByte(total + 1);
               break;
            case 2:
               if (i == 0) {
                  this.writeCard16(1);
               }

               total += length;
               this.writeCard16(total + 1);
               break;
            case 3:
               if (i == 0) {
                  this.writeThreeByteNumber(1);
               }

               total += length;
               this.writeThreeByteNumber(total + 1);
               break;
            case 4:
               if (i == 0) {
                  this.writeULong(1);
               }

               total += length;
               this.writeULong(total + 1);
               break;
            default:
               throw new AssertionError("Offset Size was not an expected value.");
         }
      }

      var6 = dataArray.iterator();

      while(var6.hasNext()) {
         aDataArray = (byte[])var6.next();
         this.writeBytes(aDataArray);
      }

      return hdrTotal + total;
   }

   private int getOffSize(int totLength) {
      int offSize = true;
      byte offSize;
      if (totLength < 256) {
         offSize = 1;
      } else if (totLength < 65536) {
         offSize = 2;
      } else if (totLength < 16777216) {
         offSize = 3;
      } else {
         offSize = 4;
      }

      return offSize;
   }

   private void writeCharsetTable(boolean cidFont) throws IOException {
      Iterator var2;
      int entry;
      if (cidFont) {
         this.writeByte(2);
         var2 = this.gidToSID.keySet().iterator();

         while(var2.hasNext()) {
            entry = (Integer)var2.next();
            if (entry != 0) {
               this.writeCard16(entry);
               this.writeCard16(this.gidToSID.size() - 1);
               break;
            }
         }
      } else {
         this.writeByte(0);
         var2 = this.gidToSID.values().iterator();

         while(var2.hasNext()) {
            entry = (Integer)var2.next();
            if (entry != 0) {
               this.writeCard16(entry);
            }
         }
      }

   }

   protected void writePrivateDict() throws IOException {
      Map topDICT = this.cffReader.getTopDictEntries();
      CFFDataReader.DICTEntry privateEntry = (CFFDataReader.DICTEntry)topDICT.get("Private");
      if (privateEntry != null) {
         this.writeBytes(this.cffReader.getPrivateDictBytes(privateEntry));
      }

   }

   protected void updateOffsets(Offsets offsets) throws IOException {
      Map topDICT = this.cffReader.getTopDictEntries();
      Map privateDICT = null;
      CFFDataReader.DICTEntry privateEntry = (CFFDataReader.DICTEntry)topDICT.get("Private");
      if (privateEntry != null) {
         privateDICT = this.cffReader.getPrivateDict(privateEntry);
      }

      this.updateFixedOffsets(topDICT, offsets);
      if (privateDICT != null) {
         int oldPrivateOffset = offsets.topDictData + privateEntry.getOffset();
         this.updateOffset(oldPrivateOffset + (Integer)privateEntry.getOperandLengths().get(0), (Integer)privateEntry.getOperandLengths().get(1), offsets.privateDict);
         CFFDataReader.DICTEntry subroutines = (CFFDataReader.DICTEntry)privateDICT.get("Subrs");
         if (subroutines != null) {
            int oldLocalSubrOffset = offsets.privateDict + subroutines.getOffset();
            this.updateOffset(oldLocalSubrOffset, subroutines.getOperandLength(), offsets.localIndex - offsets.privateDict);
         }
      }

   }

   protected void updateFixedOffsets(Map topDICT, Offsets offsets) throws IOException {
      CFFDataReader.DICTEntry charset = (CFFDataReader.DICTEntry)topDICT.get("charset");
      int oldCharsetOffset = offsets.topDictData + charset.getOffset();
      this.updateOffset(oldCharsetOffset, charset.getOperandLength(), offsets.charset);
      CFFDataReader.DICTEntry charString = (CFFDataReader.DICTEntry)topDICT.get("CharStrings");
      int oldCharStringOffset = offsets.topDictData + charString.getOffset();
      this.updateOffset(oldCharStringOffset, charString.getOperandLength(), offsets.charString);
      CFFDataReader.DICTEntry encodingEntry = (CFFDataReader.DICTEntry)topDICT.get("Encoding");
      if (encodingEntry != null && ((Number)encodingEntry.getOperands().get(0)).intValue() != 0 && ((Number)encodingEntry.getOperands().get(0)).intValue() != 1) {
         int oldEncodingOffset = offsets.topDictData + encodingEntry.getOffset();
         this.updateOffset(oldEncodingOffset, encodingEntry.getOperandLength(), offsets.encoding);
      }

   }

   protected void updateCIDOffsets(Offsets offsets) throws IOException {
      Map topDict = this.cffReader.getTopDictEntries();
      CFFDataReader.DICTEntry fdArrayEntry = (CFFDataReader.DICTEntry)topDict.get("FDArray");
      if (fdArrayEntry != null) {
         this.updateOffset(offsets.topDictData + fdArrayEntry.getOffset() - 1, fdArrayEntry.getOperandLength(), offsets.fdArray);
      }

      CFFDataReader.DICTEntry fdSelect = (CFFDataReader.DICTEntry)topDict.get("FDSelect");
      if (fdSelect != null) {
         this.updateOffset(offsets.topDictData + fdSelect.getOffset() - 1, fdSelect.getOperandLength(), offsets.fdSelect);
      }

      this.updateFixedOffsets(topDict, offsets);
   }

   private void updateOffset(int position, int length, int replacement) throws IOException {
      byte[] outBytes = this.output.toByteArray();
      this.updateOffset(outBytes, position, length, replacement);
      this.output.reset();
      this.output.write(outBytes);
   }

   private void updateOffset(byte[] out, int position, int length, int replacement) {
      switch (length) {
         case 1:
            out[position] = (byte)(replacement + 139);
            break;
         case 2:
            assert replacement <= 1131;

            if (replacement <= -876) {
               out[position] = -2;
            } else if (replacement <= -620) {
               out[position] = -3;
            } else if (replacement <= -364) {
               out[position] = -4;
            } else if (replacement <= -108) {
               out[position] = -5;
            } else if (replacement <= 363) {
               out[position] = -9;
            } else if (replacement <= 619) {
               out[position] = -8;
            } else if (replacement <= 875) {
               out[position] = -7;
            } else {
               out[position] = -6;
            }

            if (replacement > 0) {
               out[position + 1] = (byte)(replacement - 108);
            } else {
               out[position + 1] = (byte)(-replacement - 108);
            }
            break;
         case 3:
            assert replacement <= 32767;

            out[position] = 28;
            out[position + 1] = (byte)(replacement >> 8 & 255);
            out[position + 2] = (byte)(replacement & 255);
         case 4:
         default:
            break;
         case 5:
            out[position] = 29;
            out[position + 1] = (byte)(replacement >> 24 & 255);
            out[position + 2] = (byte)(replacement >> 16 & 255);
            out[position + 3] = (byte)(replacement >> 8 & 255);
            out[position + 4] = (byte)(replacement & 255);
      }

   }

   public CFFDataReader getCFFReader() {
      return this.cffReader;
   }

   static class BytesNumber {
      private int number;
      private int numBytes;

      public BytesNumber(int number, int numBytes) {
         this.number = number;
         this.numBytes = numBytes;
      }

      public int getNumber() {
         return this.number;
      }

      public int getNumBytes() {
         return this.numBytes;
      }

      public void clearNumber() {
         this.number = -1;
         this.numBytes = -1;
      }

      public String toString() {
         return Integer.toString(this.number);
      }

      public boolean equals(Object entry) {
         assert entry instanceof BytesNumber;

         BytesNumber bnEntry = (BytesNumber)entry;
         return this.number == bnEntry.getNumber() && this.numBytes == bnEntry.getNumBytes();
      }

      public int hashCode() {
         int hash = 1;
         hash = hash * 17 + this.number;
         hash = hash * 31 + this.numBytes;
         return hash;
      }
   }

   static class Type2Parser {
      protected Log log = LogFactory.getLog(Type2Parser.class);
      private List stack = new ArrayList();
      private int hstemCount;
      private int vstemCount;
      private int lastOp = -1;
      private int maskLength = -1;

      public void pushOperand(BytesNumber v) {
         this.stack.add(v);
      }

      public BytesNumber popOperand() {
         return (BytesNumber)this.stack.remove(this.stack.size() - 1);
      }

      public void clearStack() {
         this.stack.clear();
      }

      public int[] getOperands(int numbers) {
         int[] ret;
         for(ret = new int[numbers]; numbers > 0; ret[numbers] = this.popOperand().getNumber()) {
            --numbers;
         }

         return ret;
      }

      public void setMaskLength(int maskLength) {
         this.maskLength = maskLength;
      }

      public int getMaskLength() {
         return this.maskLength > 0 ? this.maskLength : 1 + (this.hstemCount + this.vstemCount - 1) / 8;
      }

      private int exec(int b0, byte[] input, int curPos) throws IOException {
         ByteArrayInputStream bis = new ByteArrayInputStream(input);
         bis.skip((long)(curPos + 1));
         return this.exec(b0, bis);
      }

      public int exec(int b0, InputStream data) throws IOException {
         int posDelta = 0;
         if ((b0 < 0 || b0 > 27) && (b0 < 29 || b0 > 31)) {
            if (b0 != 28 && (b0 < 32 || b0 > 255)) {
               throw new UnsupportedOperationException("Operator:" + b0 + " is not supported");
            }

            BytesNumber operand = this.readNumber(b0, data);
            this.pushOperand(operand);
            posDelta = operand.getNumBytes() - 1;
         } else {
            if (b0 == 12) {
               this.log.warn("May not guess the operand count correctly.");
               posDelta = 1;
            } else if (b0 != 1 && b0 != 18) {
               if (b0 != 19 && b0 != 20) {
                  if (b0 == 3 || b0 == 23) {
                     this.vstemCount += this.stack.size() / 2;
                     this.clearStack();
                  }
               } else {
                  if (this.lastOp == 1 || this.lastOp == 18) {
                     this.vstemCount += this.stack.size() / 2;
                  }

                  this.clearStack();
                  posDelta = this.getMaskLength();
               }
            } else {
               this.hstemCount += this.stack.size() / 2;
               this.clearStack();
            }

            if (b0 != 11 && b0 != 12) {
               this.lastOp = b0;
            }
         }

         return posDelta;
      }

      private BytesNumber readNumber(int b0, InputStream input) throws IOException {
         int b1;
         int b2;
         if (b0 == 28) {
            b1 = input.read();
            b2 = input.read();
            return new BytesNumber((short)(b1 << 8 | b2), 3);
         } else if (b0 >= 32 && b0 <= 246) {
            return new BytesNumber(b0 - 139, 1);
         } else if (b0 >= 247 && b0 <= 250) {
            b1 = input.read();
            return new BytesNumber((b0 - 247) * 256 + b1 + 108, 2);
         } else if (b0 >= 251 && b0 <= 254) {
            b1 = input.read();
            return new BytesNumber(-(b0 - 251) * 256 - b1 - 108, 2);
         } else if (b0 == 255) {
            b1 = input.read();
            b2 = input.read();
            int b3 = input.read();
            int b4 = input.read();
            return new BytesNumber(b1 << 24 | b2 << 16 | b3 << 8 | b4, 5);
         } else {
            throw new IllegalArgumentException();
         }
      }
   }

   private static class FDIndexReference {
      private int newFDIndex;
      private int oldFDIndex;

      public FDIndexReference(int newFDIndex, int oldFDIndex) {
         this.newFDIndex = newFDIndex;
         this.oldFDIndex = oldFDIndex;
      }

      public int getNewFDIndex() {
         return this.newFDIndex;
      }

      public int getOldFDIndex() {
         return this.oldFDIndex;
      }
   }

   static class Offsets {
      Integer topDictData;
      Integer encoding;
      Integer charset;
      Integer fdSelect;
      Integer charString;
      Integer fdArray;
      Integer privateDict;
      Integer localIndex;
   }
}
