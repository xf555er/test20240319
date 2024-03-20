package org.apache.fop.fonts.type1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.SingleByteFont;

public class Type1SubsetFile {
   protected static final Log LOG = LogFactory.getLog(Type1SubsetFile.class);
   protected HashMap subsetCharStrings;
   protected List charNames;
   protected LinkedHashMap uniqueSubs;
   private SingleByteFont sbfont;
   protected String eol = "\n";
   protected boolean subsetSubroutines = true;
   private byte[] fullFont;
   protected List headerSection;
   protected List mainSection;
   protected boolean standardEncoding;
   private static final int OP_SEAC = 6;
   private static final int OP_CALLSUBR = 10;
   private static final int OP_CALLOTHERSUBR = 16;

   public byte[] createSubset(InputStream in, SingleByteFont sbfont) throws IOException {
      this.fullFont = IOUtils.toByteArray(in);
      byte[] subsetFont = this.createSubset(sbfont, true);
      return subsetFont.length != 0 && subsetFont.length <= this.fullFont.length ? subsetFont : this.fullFont;
   }

   private byte[] createSubset(SingleByteFont sbfont, boolean subsetSubroutines) throws IOException {
      this.subsetSubroutines = subsetSubroutines;
      InputStream in = new ByteArrayInputStream(this.fullFont);
      this.sbfont = sbfont;
      PFBParser pfbParser = new PFBParser();
      PFBData pfbData = pfbParser.parsePFB(in);
      PostscriptParser psParser = new PostscriptParser();
      this.charNames = new ArrayList();
      if (this.headerSection == null) {
         this.headerSection = psParser.parse(pfbData.getHeaderSegment());
      }

      PostscriptParser.PSElement encoding = this.getElement("/Encoding", this.headerSection);
      if (encoding.getFoundUnexpected()) {
         return new byte[0];
      } else {
         List subsetEncodingEntries = this.readEncoding(encoding);
         byte[] decoded = Type1SubsetFile.BinaryCoder.decodeBytes(pfbData.getEncryptedSegment(), 55665, 4);
         this.uniqueSubs = new LinkedHashMap();
         this.subsetCharStrings = new HashMap();
         if (this.mainSection == null) {
            this.mainSection = psParser.parse(decoded);
         }

         PostscriptParser.PSElement charStrings = this.getElement("/CharStrings", this.mainSection);
         boolean result = this.readMainSection(this.mainSection, decoded, subsetEncodingEntries, charStrings);
         if (!result) {
            this.uniqueSubs.clear();
            this.subsetCharStrings.clear();
            this.charNames.clear();
            return this.createSubset(sbfont, false);
         } else {
            ByteArrayOutputStream boasHeader = this.writeHeader(pfbData, encoding);
            ByteArrayOutputStream boasMain = this.writeMainSection(decoded, this.mainSection, charStrings);
            byte[] mainSectionBytes = boasMain.toByteArray();
            mainSectionBytes = Type1SubsetFile.BinaryCoder.encodeBytes(mainSectionBytes, 55665, 4);
            boasMain.reset();
            boasMain.write(mainSectionBytes);
            ByteArrayOutputStream baosTrailer = new ByteArrayOutputStream();
            baosTrailer.write(pfbData.getTrailerSegment(), 0, pfbData.getTrailerSegment().length);
            return this.stitchFont(boasHeader, boasMain, baosTrailer);
         }
      }
   }

   public byte[] stitchFont(ByteArrayOutputStream boasHeader, ByteArrayOutputStream boasMain, ByteArrayOutputStream boasTrailer) throws IOException {
      int headerLength = boasHeader.size();
      int mainLength = boasMain.size();
      boasMain.write(128);
      boasMain.write(1);
      this.updateSectionSize(boasTrailer.size()).writeTo(boasMain);
      boasTrailer.write(128);
      boasTrailer.write(3);
      boasTrailer.writeTo(boasMain);
      boasHeader.write(128);
      boasHeader.write(2);
      this.updateSectionSize(mainLength).writeTo(boasHeader);
      boasMain.writeTo(boasHeader);
      ByteArrayOutputStream fullFont = new ByteArrayOutputStream();
      fullFont.write(128);
      fullFont.write(1);
      this.updateSectionSize(headerLength).writeTo(fullFont);
      boasHeader.writeTo(fullFont);
      return fullFont.toByteArray();
   }

   private List readEncoding(PostscriptParser.PSElement encoding) {
      Map usedGlyphs = this.sbfont.getUsedGlyphs();
      List glyphs = new ArrayList(usedGlyphs.keySet());
      Collections.sort(glyphs);
      List subsetEncodingEntries = new ArrayList();
      if (encoding instanceof PostscriptParser.PSFixedArray) {
         PostscriptParser.PSFixedArray encodingArray = (PostscriptParser.PSFixedArray)encoding;
         Iterator var6 = glyphs.iterator();

         while(var6.hasNext()) {
            int glyph = (Integer)var6.next();
            List matches = this.searchEntries(encodingArray.getEntries(), glyph);
            if (matches.size() == 0) {
               matches.clear();
               if (glyph == 0) {
                  matches.add("dup 0 /.notdef put");
               } else {
                  matches.add(String.format("dup %d /%s put", glyph, this.sbfont.getGlyphName(glyph)));
               }
            }

            Iterator var9 = matches.iterator();

            while(var9.hasNext()) {
               String match = (String)var9.next();
               subsetEncodingEntries.add(match);
               this.addToCharNames(match);
            }
         }
      } else if (encoding instanceof PostscriptParser.PSVariable) {
         if (((PostscriptParser.PSVariable)encoding).getValue().equals("StandardEncoding")) {
            this.standardEncoding = true;
            this.sbfont.mapUsedGlyphName(0, "/.notdef");
            Iterator var11 = glyphs.iterator();

            while(var11.hasNext()) {
               int glyph = (Integer)var11.next();
               String name = this.sbfont.getGlyphName(glyph);
               if (glyph != 0 && name != null && !name.trim().equals("")) {
                  this.sbfont.mapUsedGlyphName(glyph, "/" + name);
               }
            }
         } else {
            LOG.warn("Only Custom or StandardEncoding is supported when creating a Type 1 subset.");
         }
      }

      return subsetEncodingEntries;
   }

   protected List searchEntries(HashMap encodingEntries, int glyph) {
      List matches = new ArrayList();
      Iterator var4 = encodingEntries.entrySet().iterator();

      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         String tag = this.getEntryPart((String)entry.getValue(), 3);
         String name = this.sbfont.getGlyphName((Integer)this.sbfont.getUsedGlyphs().get(glyph));
         if (name.equals(tag)) {
            matches.add(entry.getValue());
         }
      }

      return matches;
   }

   protected ByteArrayOutputStream writeHeader(PFBData pfbData, PostscriptParser.PSElement encoding) throws IOException {
      ByteArrayOutputStream boasHeader = new ByteArrayOutputStream();
      boasHeader.write(pfbData.getHeaderSegment(), 0, encoding.getStartPoint() - 1);
      String encodingArray;
      if (!this.standardEncoding) {
         encodingArray = this.eol + "/Encoding 256 array" + this.eol + "0 1 255 {1 index exch /.notdef put } for" + this.eol;
         byte[] encodingDefinition = encodingArray.getBytes("ASCII");
         boasHeader.write(encodingDefinition, 0, encodingDefinition.length);
         Set entrySet = this.sbfont.getUsedGlyphNames().entrySet();
         Iterator var7 = entrySet.iterator();

         while(var7.hasNext()) {
            Map.Entry entry = (Map.Entry)var7.next();
            String arrayEntry = String.format("dup %d %s put", entry.getKey(), entry.getValue());
            this.writeString(arrayEntry + this.eol, boasHeader);
         }

         this.writeString("readonly def" + this.eol, boasHeader);
      } else {
         encodingArray = this.eol + "/Encoding StandardEncoding def" + this.eol;
         boasHeader.write(encodingArray.getBytes("ASCII"));
      }

      boasHeader.write(pfbData.getHeaderSegment(), encoding.getEndPoint(), pfbData.getHeaderSegment().length - encoding.getEndPoint());
      return boasHeader;
   }

   ByteArrayOutputStream updateSectionSize(int size) throws IOException {
      ByteArrayOutputStream boas = new ByteArrayOutputStream();
      byte[] lowOrderSize = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array();
      boas.write(lowOrderSize);
      return boas;
   }

   private boolean readMainSection(List mainSection, byte[] decoded, List subsetEncodingEntries, PostscriptParser.PSElement charStrings) {
      subsetEncodingEntries.add(0, "dup 0 /.notdef put");
      PostscriptParser.PSDictionary charStringsDict = (PostscriptParser.PSDictionary)charStrings;
      Iterator var6 = this.sbfont.getUsedGlyphNames().values().iterator();

      while(var6.hasNext()) {
         String tag = (String)var6.next();
         if (!tag.equals("/.notdef")) {
            this.charNames.add(tag);
         }

         int[] location = (int[])charStringsDict.getBinaryEntries().get(tag);
         if (location != null) {
            byte[] charStringEntry = this.getBinaryEntry(location, decoded);
            int skipBytes = 4;
            PostscriptParser.PSElement element = this.getElement("lenIV", mainSection);
            if (element != null && element instanceof PostscriptParser.PSVariable) {
               PostscriptParser.PSVariable lenIV = (PostscriptParser.PSVariable)element;

               try {
                  skipBytes = Integer.parseInt(lenIV.getValue());
               } catch (NumberFormatException var14) {
                  LOG.warn(String.format("Invalid value `%s` for lenIV found in font %s", lenIV.getValue(), this.sbfont.getEmbedFileURI().toString()));
               }
            }

            charStringEntry = Type1SubsetFile.BinaryCoder.decodeBytes(charStringEntry, 4330, skipBytes);
            PostscriptParser.PSFixedArray subroutines = (PostscriptParser.PSFixedArray)this.getElement("/Subrs", mainSection);
            if (this.subsetSubroutines) {
               charStringEntry = this.createSubsetCharStrings(decoded, charStringEntry, subroutines, subsetEncodingEntries);
            }

            if (charStringEntry.length == 0) {
               return false;
            }

            charStringEntry = Type1SubsetFile.BinaryCoder.encodeBytes(charStringEntry, 4330, skipBytes);
            this.subsetCharStrings.put(tag, charStringEntry);
         }
      }

      return true;
   }

   private byte[] createSubsetCharStrings(byte[] decoded, byte[] data, PostscriptParser.PSFixedArray subroutines, List subsetEncodingEntries) {
      List operands = new ArrayList();

      for(int i = 0; i < data.length; ++i) {
         int cur = data[i] & 255;
         int b1;
         int b2;
         int b3;
         int count;
         if (cur > 31) {
            if (cur <= 246) {
               operands.add(new BytesNumber(cur - 139, 1));
            } else if (cur <= 250) {
               operands.add(new BytesNumber((cur - 247) * 256 + (data[i + 1] & 255) + 108, 2));
               ++i;
            } else if (cur <= 254) {
               operands.add(new BytesNumber(-(cur - 251) * 256 - (data[i + 1] & 255) - 108, 2));
               ++i;
            } else if (cur == 255) {
               b1 = data[i + 1] & 255;
               b2 = data[i + 2] & 255;
               b3 = data[i + 3] & 255;
               count = data[i + 4] & 255;
               int value = b1 << 24 | b2 << 16 | b3 << 8 | count;
               operands.add(new BytesNumber(value, 5));
               i += 4;
            }
         } else {
            b1 = data.length;
            if (cur == 10) {
               if (operands.size() == 0) {
                  continue;
               }

               if (this.uniqueSubs.get(((BytesNumber)operands.get(operands.size() - 1)).getNumber()) == null) {
                  this.uniqueSubs.put(((BytesNumber)operands.get(operands.size() - 1)).getNumber(), new byte[0]);
                  data = this.addSubroutine(subroutines, operands, decoded, subsetEncodingEntries, data, i, 1, -1, ((BytesNumber)operands.get(operands.size() - 1)).getNumber());
               } else {
                  data = this.addSubroutine(subroutines, operands, decoded, subsetEncodingEntries, data, i, 1, this.getSubrIndex(((BytesNumber)operands.get(operands.size() - 1)).getNumber()), ((BytesNumber)operands.get(operands.size() - 1)).getNumber());
               }
            } else if (cur == 12) {
               ++i;
               b2 = data[i] & 255;
               if (b2 == 6) {
                  b3 = ((BytesNumber)operands.get(operands.size() - 2)).getNumber();
                  count = ((BytesNumber)operands.get(operands.size() - 1)).getNumber();
                  String charFirst = AdobeStandardEncoding.getCharFromCodePoint(b3);
                  String charSecond = AdobeStandardEncoding.getCharFromCodePoint(count);
                  subsetEncodingEntries.add(String.format("dup %d /%s put", b3, charFirst));
                  subsetEncodingEntries.add(String.format("dup %d /%s put", count, charSecond));
                  this.sbfont.mapUsedGlyphName(b3, "/" + charFirst);
                  this.sbfont.mapUsedGlyphName(count, "/" + charSecond);
               } else if (b2 == 16) {
                  int[] pattern = new int[]{12, 17, 10};
                  count = 0;
                  boolean matchesPattern = true;
                  if (data.length > i + 4) {
                     for(int pos = i + 1; pos < i + 4; ++pos) {
                        if (data[pos] != pattern[count++]) {
                           matchesPattern = false;
                        }
                     }
                  }

                  if (matchesPattern) {
                     return new byte[0];
                  }

                  data = this.addSubroutine(subroutines, operands, decoded, subsetEncodingEntries, data, i, 2, -1, ((BytesNumber)operands.get(0)).getNumber());
               }
            }

            if (data.length == 0) {
               return new byte[0];
            }

            i -= b1 - data.length;
            operands.clear();
         }
      }

      return data;
   }

   private int getSubrIndex(int subID) {
      int count = 0;

      for(Iterator var3 = this.uniqueSubs.keySet().iterator(); var3.hasNext(); ++count) {
         Integer key = (Integer)var3.next();
         if (key == subID) {
            return count;
         }
      }

      return -1;
   }

   private byte[] addSubroutine(PostscriptParser.PSFixedArray subroutines, List operands, byte[] decoded, List subsetEncodingEntries, byte[] data, int i, int opLength, int existingSubrRef, int subrID) {
      if (existingSubrRef == -1) {
         int[] subrData = subroutines.getBinaryEntryByIndex(subrID);
         byte[] subroutine = this.getBinaryEntry(subrData, decoded);
         subroutine = Type1SubsetFile.BinaryCoder.decodeBytes(subroutine, 4330, 4);
         subroutine = this.createSubsetCharStrings(decoded, subroutine, subroutines, subsetEncodingEntries);
         if (subroutine.length == 0) {
            return new byte[0];
         }

         subroutine = Type1SubsetFile.BinaryCoder.encodeBytes(subroutine, 4330, 4);
         this.uniqueSubs.put(subrID, subroutine);
      }

      int subRef = existingSubrRef != -1 ? existingSubrRef : this.uniqueSubs.size() - 1;
      data = this.constructNewRefData(i, data, operands, 1, subRef, opLength);
      return data;
   }

   protected ByteArrayOutputStream writeMainSection(byte[] decoded, List mainSection, PostscriptParser.PSElement charStrings) throws IOException {
      ByteArrayOutputStream main = new ByteArrayOutputStream();
      PostscriptParser.PSElement subrs = this.getElement("/Subrs", mainSection);
      String rd = this.findVariable(decoded, mainSection, new String[]{"string currentfile exch readstring pop"}, "RD");
      String nd = this.findVariable(decoded, mainSection, new String[]{"def", "noaccess def"}, "noaccess def");
      String np = this.findVariable(decoded, mainSection, new String[]{"put", "noaccess put"}, "noaccess put");
      main.write(decoded, 0, subrs.getStartPoint());
      int count;
      if (this.subsetSubroutines) {
         this.writeString(this.eol + String.format("/Subrs %d array", this.uniqueSubs.size()), main);
         count = 0;
         Iterator var10 = this.uniqueSubs.entrySet().iterator();

         while(var10.hasNext()) {
            Map.Entry entry = (Map.Entry)var10.next();
            this.writeString(this.eol + String.format("dup %d %d %s ", count++, ((byte[])entry.getValue()).length, rd), main);
            main.write((byte[])entry.getValue());
            this.writeString(" " + np, main);
         }

         this.writeString(this.eol + nd, main);
      } else {
         count = subrs.getEndPoint() - subrs.getStartPoint();
         main.write(decoded, subrs.getStartPoint(), count);
      }

      main.write(decoded, subrs.getEndPoint(), charStrings.getStartPoint() - subrs.getEndPoint());
      this.writeString(this.eol + String.format("/CharStrings %d dict dup begin", this.subsetCharStrings.size()), main);
      Iterator var12 = this.subsetCharStrings.entrySet().iterator();

      while(var12.hasNext()) {
         Map.Entry entry = (Map.Entry)var12.next();
         this.writeString(this.eol + String.format("%s %d %s ", entry.getKey(), ((byte[])entry.getValue()).length, rd), main);
         main.write((byte[])entry.getValue());
         this.writeString(" " + nd, main);
      }

      this.writeString(this.eol + "end", main);
      main.write(decoded, charStrings.getEndPoint(), decoded.length - charStrings.getEndPoint());
      return main;
   }

   protected String findVariable(byte[] decoded, List elements, String[] matches, String fallback) throws UnsupportedEncodingException {
      Iterator var5 = elements.iterator();

      while(true) {
         PostscriptParser.PSElement element;
         do {
            if (!var5.hasNext()) {
               return fallback;
            }

            element = (PostscriptParser.PSElement)var5.next();
         } while(!(element instanceof PostscriptParser.PSSubroutine));

         byte[] var = new byte[element.getEndPoint() - element.getStartPoint()];
         System.arraycopy(decoded, element.getStartPoint(), var, 0, element.getEndPoint() - element.getStartPoint());
         String found = this.readVariableContents(new String(var, "ASCII")).trim();
         String[] var9 = matches;
         int var10 = matches.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            String match = var9[var11];
            if (match.equals(found)) {
               return element.getOperator().substring(1, element.getOperator().length());
            }
         }
      }
   }

   String readVariableContents(String variable) {
      int level = 0;
      String result = "";
      int start = 0;
      int end = 0;
      boolean reading = false;
      List results = new ArrayList();

      int i;
      for(i = 0; i < variable.length(); ++i) {
         char curChar = variable.charAt(i);
         boolean sectionEnd = false;
         if (curChar == '{') {
            ++level;
            sectionEnd = true;
         } else if (curChar == '}') {
            --level;
            sectionEnd = true;
         } else if (level == 1) {
            if (!reading) {
               reading = true;
               start = i;
            }

            end = i;
         }

         if (sectionEnd && reading) {
            results.add(start);
            results.add(end);
            reading = false;
         }
      }

      for(i = 0; i < results.size(); i += 2) {
         result = result.concat(variable.substring((Integer)results.get(i), (Integer)results.get(i + 1) + 1));
      }

      return result;
   }

   private void addToCharNames(String encodingEntry) {
      int spaceCount = 0;
      int lastSpaceIndex = 0;
      int charIndex = 0;
      String charName = "";

      for(int i = 0; i < encodingEntry.length(); ++i) {
         boolean isSpace = encodingEntry.charAt(i) == ' ';
         if (isSpace) {
            ++spaceCount;
            switch (spaceCount - 1) {
               case 1:
                  charIndex = Integer.parseInt(encodingEntry.substring(lastSpaceIndex + 1, i));
                  break;
               case 2:
                  charName = encodingEntry.substring(lastSpaceIndex + 1, i);
            }
         }

         if (isSpace) {
            lastSpaceIndex = i;
         }
      }

      this.sbfont.mapUsedGlyphName(charIndex, charName);
   }

   protected void writeString(String entry, ByteArrayOutputStream boas) throws IOException {
      byte[] byteEntry = entry.getBytes("ASCII");
      boas.write(byteEntry);
   }

   private byte[] constructNewRefData(int curDataPos, byte[] currentData, List operands, int opNum, int curSubsetIndexSize, int operatorLength) {
      int operandsLenth = this.getOperandsLength(operands);
      int startRef = curDataPos - operandsLenth + this.getOpPosition(opNum, operands) + (1 - operatorLength);
      byte[] preBytes = new byte[startRef];
      System.arraycopy(currentData, 0, preBytes, 0, startRef);
      byte[] newRefBytes = this.createNewRef(curSubsetIndexSize, -1);
      byte[] newData = this.concatArray(preBytes, newRefBytes);
      byte[] postBytes = new byte[currentData.length - (startRef + ((BytesNumber)operands.get(opNum - 1)).getNumBytes())];
      System.arraycopy(currentData, startRef + ((BytesNumber)operands.get(opNum - 1)).getNumBytes(), postBytes, 0, currentData.length - (startRef + ((BytesNumber)operands.get(opNum - 1)).getNumBytes()));
      return this.concatArray(newData, postBytes);
   }

   int getOpPosition(int opNum, List operands) {
      int byteCount = 0;

      for(int i = 0; i < opNum - 1; ++i) {
         byteCount += ((BytesNumber)operands.get(i)).getNumBytes();
      }

      return byteCount;
   }

   int getOperandsLength(List operands) {
      int length = 0;

      BytesNumber number;
      for(Iterator var3 = operands.iterator(); var3.hasNext(); length += number.getNumBytes()) {
         number = (BytesNumber)var3.next();
      }

      return length;
   }

   private byte[] createNewRef(int newRef, int forceLength) {
      byte[] newRefBytes;
      if ((forceLength != -1 || newRef > 107) && forceLength != 1) {
         if ((forceLength != -1 || newRef > 1131) && forceLength != 2) {
            newRefBytes = new byte[]{-1, (byte)(newRef >> 24), (byte)(newRef >> 16), (byte)(newRef >> 8), (byte)newRef};
         } else {
            newRefBytes = new byte[2];
            if (newRef <= 363) {
               newRefBytes[0] = -9;
            } else if (newRef <= 619) {
               newRefBytes[0] = -8;
            } else if (newRef <= 875) {
               newRefBytes[0] = -7;
            } else {
               newRefBytes[0] = -6;
            }

            newRefBytes[1] = (byte)(newRef - 108);
         }
      } else {
         newRefBytes = new byte[]{(byte)(newRef + 139)};
      }

      return newRefBytes;
   }

   byte[] concatArray(byte[] a, byte[] b) {
      int aLen = a.length;
      int bLen = b.length;
      byte[] c = new byte[aLen + bLen];
      System.arraycopy(a, 0, c, 0, aLen);
      System.arraycopy(b, 0, c, aLen, bLen);
      return c;
   }

   protected byte[] getBinaryEntry(int[] position, byte[] decoded) {
      int start = position[0];
      int finish = position[1];
      byte[] line = new byte[finish - start];
      System.arraycopy(decoded, start, line, 0, finish - start);
      return line;
   }

   protected String getEntryPart(String entry, int part) {
      Scanner s = (new Scanner(entry)).useDelimiter(" ");

      for(int i = 1; i < part; ++i) {
         s.next();
      }

      return s.next();
   }

   protected PostscriptParser.PSElement getElement(String elementID, List elements) {
      Iterator var3 = elements.iterator();

      PostscriptParser.PSElement element;
      do {
         if (!var3.hasNext()) {
            return null;
         }

         element = (PostscriptParser.PSElement)var3.next();
      } while(!element.getOperator().equals(elementID));

      return element;
   }

   public static class BinaryCoder {
      public static byte[] decodeBytes(byte[] in, int inR, int n) {
         byte[] out = new byte[in.length - n];
         int r = inR;
         int c1 = '칭';
         int c2 = 22719;

         for(int i = 0; i < in.length; ++i) {
            int cypher = in[i] & 255;
            int plain = cypher ^ r >> 8;
            if (i >= n) {
               out[i - n] = (byte)plain;
            }

            r = (cypher + r) * c1 + c2 & '\uffff';
         }

         return out;
      }

      public static byte[] encodeBytes(byte[] in, int inR, int n) {
         byte[] buffer = new byte[in.length + n];

         int r;
         for(r = 0; r < n; ++r) {
            buffer[r] = 0;
         }

         r = inR;
         int c1 = '칭';
         int c2 = 22719;
         System.arraycopy(in, 0, buffer, n, buffer.length - n);
         byte[] out = new byte[buffer.length];

         for(int i = 0; i < buffer.length; ++i) {
            int plain = buffer[i] & 255;
            int cipher = plain ^ r >> 8;
            out[i] = (byte)cipher;
            r = (cipher + r) * c1 + c2 & '\uffff';
         }

         return out;
      }
   }

   public static final class BytesNumber {
      private int number;
      private int numBytes;
      private String name;

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

      public void setName(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }
   }
}
