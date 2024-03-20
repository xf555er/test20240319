package org.apache.fop.fonts.truetype;

import java.awt.Rectangle;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.fonts.AdvancedTypographicTableFormatException;
import org.apache.fop.complexscripts.fonts.GlyphDefinitionTable;
import org.apache.fop.complexscripts.fonts.GlyphPositioningTable;
import org.apache.fop.complexscripts.fonts.GlyphSubstitutionTable;
import org.apache.fop.complexscripts.fonts.OTFAdvancedTypographicTableReader;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.xmlgraphics.fonts.Glyphs;

public abstract class OpenFont {
   static final byte NTABS = 24;
   static final int MAX_CHAR_CODE = 255;
   static final int ENC_BUF_SIZE = 1024;
   private static final String[] MAC_GLYPH_ORDERING = new String[]{".notdef", ".null", "nonmarkingreturn", "space", "exclam", "quotedbl", "numbersign", "dollar", "percent", "ampersand", "quotesingle", "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen", "period", "slash", "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "colon", "semicolon", "less", "equal", "greater", "question", "at", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash", "bracketright", "asciicircum", "underscore", "grave", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft", "bar", "braceright", "asciitilde", "Adieresis", "Aring", "Ccedilla", "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute", "agrave", "acircumflex", "adieresis", "atilde", "aring", "ccedilla", "eacute", "egrave", "ecircumflex", "edieresis", "iacute", "igrave", "icircumflex", "idieresis", "ntilde", "oacute", "ograve", "ocircumflex", "odieresis", "otilde", "uacute", "ugrave", "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling", "section", "bullet", "paragraph", "germandbls", "registered", "copyright", "trademark", "acute", "dieresis", "notequal", "AE", "Oslash", "infinity", "plusminus", "lessequal", "greaterequal", "yen", "mu", "partialdiff", "summation", "product", "pi", "integral", "ordfeminine", "ordmasculine", "Omega", "ae", "oslash", "questiondown", "exclamdown", "logicalnot", "radical", "florin", "approxequal", "Delta", "guillemotleft", "guillemotright", "ellipsis", "nonbreakingspace", "Agrave", "Atilde", "Otilde", "OE", "oe", "endash", "emdash", "quotedblleft", "quotedblright", "quoteleft", "quoteright", "divide", "lozenge", "ydieresis", "Ydieresis", "fraction", "currency", "guilsinglleft", "guilsinglright", "fi", "fl", "daggerdbl", "periodcentered", "quotesinglbase", "quotedblbase", "perthousand", "Acircumflex", "Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis", "Igrave", "Oacute", "Ocircumflex", "apple", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi", "circumflex", "tilde", "macron", "breve", "dotaccent", "ring", "cedilla", "hungarumlaut", "ogonek", "caron", "Lslash", "lslash", "Scaron", "scaron", "Zcaron", "zcaron", "brokenbar", "Eth", "eth", "Yacute", "yacute", "Thorn", "thorn", "minus", "multiply", "onesuperior", "twosuperior", "threesuperior", "onehalf", "onequarter", "threequarters", "franc", "Gbreve", "gbreve", "Idotaccent", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron", "ccaron", "dcroat"};
   protected FontFileReader fontFile;
   public static final boolean TRACE_ENABLED = false;
   private static final String ENCODING = "WinAnsiEncoding";
   private static final short FIRST_CHAR = 0;
   protected boolean useKerning;
   private boolean isEmbeddable;
   private boolean hasSerifs;
   protected Map dirTabs;
   private Map kerningTab;
   private Map ansiKerningTab;
   private List cmaps;
   protected List unicodeMappings;
   private int upem;
   protected int nhmtx;
   private PostScriptVersion postScriptVersion;
   protected int locaFormat;
   protected long lastLoca;
   protected int numberOfGlyphs;
   protected OFMtxEntry[] mtxTab;
   protected String postScriptName;
   protected String fullName;
   protected String embedFontName;
   protected String notice;
   protected final Set familyNames;
   protected String subFamilyName;
   protected boolean cid;
   private long italicAngle;
   private long isFixedPitch;
   private int fontBBox1;
   private int fontBBox2;
   private int fontBBox3;
   private int fontBBox4;
   private int capHeight;
   private int os2CapHeight;
   private int underlinePosition;
   private int underlineThickness;
   private int strikeoutPosition;
   private int strikeoutThickness;
   private int xHeight;
   private int os2xHeight;
   private int ascender;
   private int descender;
   private int hheaAscender;
   private int hheaDescender;
   private int os2Ascender;
   private int os2Descender;
   private int usWeightClass;
   private short lastChar;
   private int[] ansiWidth;
   private Map ansiIndex;
   protected Map svgs;
   private final Map glyphToUnicodeMap;
   private final Map unicodeToGlyphMap;
   private boolean isCFF;
   protected boolean useAdvanced;
   protected OTFAdvancedTypographicTableReader advancedTableReader;
   protected Log log;

   public OpenFont() {
      this(true, false);
   }

   public OpenFont(boolean useKerning, boolean useAdvanced) {
      this.isEmbeddable = true;
      this.hasSerifs = true;
      this.postScriptName = "";
      this.fullName = "";
      this.embedFontName = "";
      this.notice = "";
      this.familyNames = new HashSet();
      this.subFamilyName = "";
      this.cid = true;
      this.glyphToUnicodeMap = new HashMap();
      this.unicodeToGlyphMap = new HashMap();
      this.log = LogFactory.getLog(TTFFile.class);
      this.useKerning = useKerning;
      this.useAdvanced = useAdvanced;
   }

   public OFDirTabEntry getDirectoryEntry(OFTableName name) {
      return (OFDirTabEntry)this.dirTabs.get(name);
   }

   public boolean seekTab(FontFileReader in, OFTableName tableName, long offset) throws IOException {
      OFDirTabEntry dt = (OFDirTabEntry)this.dirTabs.get(tableName);
      if (dt == null) {
         this.log.info("Dirtab " + tableName.getName() + " not found.");
         return false;
      } else {
         in.seekSet(dt.getOffset() + offset);
         return true;
      }
   }

   public int convertTTFUnit2PDFUnit(int n) {
      int ret;
      if (n < 0) {
         long rest1 = (long)(n % this.upem);
         long storrest = 1000L * rest1;
         long ledd2 = storrest != 0L ? rest1 / storrest : 0L;
         ret = -(-1000 * n / this.upem - (int)ledd2);
      } else {
         ret = n / this.upem * 1000 + n % this.upem * 1000 / this.upem;
      }

      return ret;
   }

   protected boolean readCMAP() throws IOException {
      this.unicodeMappings = new ArrayList();
      if (!this.seekTab(this.fontFile, OFTableName.CMAP, 2L)) {
         return true;
      } else {
         int numCMap = this.fontFile.readTTFUShort();
         long cmapUniOffset = 0L;
         long symbolMapOffset = 0L;
         long surrogateMapOffset = 0L;
         if (this.log.isDebugEnabled()) {
            this.log.debug(numCMap + " cmap tables");
         }

         for(int i = 0; i < numCMap; ++i) {
            int cmapPID = this.fontFile.readTTFUShort();
            int cmapEID = this.fontFile.readTTFUShort();
            long cmapOffset = (long)this.fontFile.readTTFLong();
            if (this.log.isDebugEnabled()) {
               this.log.debug("Platform ID: " + cmapPID + " Encoding: " + cmapEID);
            }

            if (cmapPID == 3 && cmapEID == 1) {
               cmapUniOffset = cmapOffset;
            }

            if (cmapPID == 3 && cmapEID == 0) {
               symbolMapOffset = cmapOffset;
            }

            if (cmapPID == 3 && cmapEID == 10) {
               surrogateMapOffset = cmapOffset;
            }
         }

         if (surrogateMapOffset > 0L) {
            return this.readUnicodeCmap(surrogateMapOffset, 10);
         } else if (cmapUniOffset > 0L) {
            return this.readUnicodeCmap(cmapUniOffset, 1);
         } else if (symbolMapOffset > 0L) {
            return this.readUnicodeCmap(symbolMapOffset, 0);
         } else {
            this.log.fatal("Unsupported TrueType font: No Unicode or Symbol cmap table not present. Aborting");
            return false;
         }
      }
   }

   private boolean readUnicodeCmap(long cmapUniOffset, int encodingID) throws IOException {
      int mtxPtr = 0;
      this.seekTab(this.fontFile, OFTableName.CMAP, cmapUniOffset);
      int cmapFormat = this.fontFile.readTTFUShort();
      if (cmapFormat < 8) {
         this.fontFile.readTTFUShort();
         this.fontFile.readTTFUShort();
      } else {
         this.fontFile.readTTFUShort();
         this.fontFile.readTTFULong();
         this.fontFile.readTTFULong();
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("CMAP format: " + cmapFormat);
      }

      if (cmapFormat == 4) {
         int cmapSegCountX2 = this.fontFile.readTTFUShort();
         int cmapSearchRange = this.fontFile.readTTFUShort();
         int cmapEntrySelector = this.fontFile.readTTFUShort();
         int cmapRangeShift = this.fontFile.readTTFUShort();
         if (this.log.isDebugEnabled()) {
            this.log.debug("segCountX2   : " + cmapSegCountX2);
            this.log.debug("searchRange  : " + cmapSearchRange);
            this.log.debug("entrySelector: " + cmapEntrySelector);
            this.log.debug("rangeShift   : " + cmapRangeShift);
         }

         int[] cmapEndCounts = new int[cmapSegCountX2 / 2];
         int[] cmapStartCounts = new int[cmapSegCountX2 / 2];
         int[] cmapDeltas = new int[cmapSegCountX2 / 2];
         int[] cmapRangeOffsets = new int[cmapSegCountX2 / 2];

         int glyphIdArrayOffset;
         for(glyphIdArrayOffset = 0; glyphIdArrayOffset < cmapSegCountX2 / 2; ++glyphIdArrayOffset) {
            cmapEndCounts[glyphIdArrayOffset] = this.fontFile.readTTFUShort();
         }

         this.fontFile.skip(2L);

         for(glyphIdArrayOffset = 0; glyphIdArrayOffset < cmapSegCountX2 / 2; ++glyphIdArrayOffset) {
            cmapStartCounts[glyphIdArrayOffset] = this.fontFile.readTTFUShort();
         }

         for(glyphIdArrayOffset = 0; glyphIdArrayOffset < cmapSegCountX2 / 2; ++glyphIdArrayOffset) {
            cmapDeltas[glyphIdArrayOffset] = this.fontFile.readTTFShort();
         }

         for(glyphIdArrayOffset = 0; glyphIdArrayOffset < cmapSegCountX2 / 2; ++glyphIdArrayOffset) {
            cmapRangeOffsets[glyphIdArrayOffset] = this.fontFile.readTTFUShort();
         }

         glyphIdArrayOffset = this.fontFile.getCurrentPos();
         BitSet eightBitGlyphs = new BitSet(256);

         for(int i = 0; i < cmapStartCounts.length; ++i) {
            if (this.log.isTraceEnabled()) {
               this.log.trace(i + ": " + cmapStartCounts[i] + " - " + cmapEndCounts[i]);
            }

            if (this.log.isDebugEnabled() && this.isInPrivateUseArea(cmapStartCounts[i], cmapEndCounts[i])) {
               this.log.debug("Font contains glyphs in the Unicode private use area: " + Integer.toHexString(cmapStartCounts[i]) + " - " + Integer.toHexString(cmapEndCounts[i]));
            }

            for(int j = cmapStartCounts[i]; j <= cmapEndCounts[i]; ++j) {
               if (j < 256 && j > this.lastChar) {
                  this.lastChar = (short)j;
               }

               if (j < 256) {
                  eightBitGlyphs.set(j);
               }

               if (mtxPtr < this.mtxTab.length) {
                  int glyphIdx;
                  if (cmapRangeOffsets[i] != 0 && j != 65535) {
                     int glyphOffset = glyphIdArrayOffset + (cmapRangeOffsets[i] / 2 + (j - cmapStartCounts[i]) + i - cmapSegCountX2 / 2) * 2;
                     this.fontFile.seekSet((long)glyphOffset);
                     glyphIdx = this.fontFile.readTTFUShort() + cmapDeltas[i] & '\uffff';
                     this.unicodeMappings.add(new UnicodeMapping(this, glyphIdx, j));
                     this.mtxTab[glyphIdx].getUnicodeIndex().add(j);
                     if (encodingID == 0 && j >= 61472 && j <= 61695) {
                        int mapped = j - '\uf000';
                        if (!eightBitGlyphs.get(mapped)) {
                           this.unicodeMappings.add(new UnicodeMapping(this, glyphIdx, mapped));
                           this.mtxTab[glyphIdx].getUnicodeIndex().add(mapped);
                        }
                     }

                     List v = (List)this.ansiIndex.get(j);
                     if (v != null) {
                        Iterator var38 = v.iterator();

                        while(var38.hasNext()) {
                           Integer aIdx = (Integer)var38.next();
                           this.ansiWidth[aIdx] = this.mtxTab[glyphIdx].getWx();
                           if (this.log.isTraceEnabled()) {
                              this.log.trace("Added width " + this.mtxTab[glyphIdx].getWx() + " uni: " + j + " ansi: " + aIdx);
                           }
                        }
                     }

                     if (this.log.isTraceEnabled()) {
                        this.log.trace("Idx: " + glyphIdx + " Delta: " + cmapDeltas[i] + " Unicode: " + j + " name: " + this.mtxTab[glyphIdx].getName());
                     }
                  } else {
                     glyphIdx = j + cmapDeltas[i] & '\uffff';
                     if (glyphIdx < this.mtxTab.length) {
                        this.mtxTab[glyphIdx].getUnicodeIndex().add(j);
                     } else {
                        this.log.debug("Glyph " + glyphIdx + " out of range: " + this.mtxTab.length);
                     }

                     this.unicodeMappings.add(new UnicodeMapping(this, glyphIdx, j));
                     if (glyphIdx < this.mtxTab.length) {
                        this.mtxTab[glyphIdx].getUnicodeIndex().add(j);
                     } else {
                        this.log.debug("Glyph " + glyphIdx + " out of range: " + this.mtxTab.length);
                     }

                     List v = (List)this.ansiIndex.get(j);
                     Integer aIdx;
                     if (v != null) {
                        for(Iterator var20 = v.iterator(); var20.hasNext(); this.ansiWidth[aIdx] = this.mtxTab[glyphIdx].getWx()) {
                           aIdx = (Integer)var20.next();
                        }
                     }
                  }

                  if (glyphIdx < this.mtxTab.length && this.mtxTab[glyphIdx].getUnicodeIndex().size() < 2) {
                     ++mtxPtr;
                  }
               }
            }
         }
      } else {
         if (cmapFormat != 12) {
            this.log.error("Cmap format not supported: " + cmapFormat);
            return false;
         }

         long nGroups = this.fontFile.readTTFULong();

         for(long i = 0L; i < nGroups; ++i) {
            long startCharCode = this.fontFile.readTTFULong();
            long endCharCode = this.fontFile.readTTFULong();
            long startGlyphCode = this.fontFile.readTTFULong();
            if (startCharCode >= 0L && startCharCode <= 1114111L) {
               if (startCharCode >= 55296L && startCharCode <= 57343L) {
                  this.log.warn("startCharCode is a surrogate pair: " + startCharCode);
               }

               if ((endCharCode <= 0L || endCharCode >= startCharCode) && endCharCode <= 1114111L) {
                  if (endCharCode >= 55296L && endCharCode <= 57343L) {
                     this.log.warn("endCharCode is a surrogate pair: " + startCharCode);
                  }

                  for(long offset = 0L; offset <= endCharCode - startCharCode; ++offset) {
                     long glyphIndexL = startGlyphCode + offset;
                     long charCodeL = startCharCode + offset;
                     if (glyphIndexL >= (long)this.numberOfGlyphs) {
                        this.log.warn("Format 12 cmap contains an invalid glyph index");
                        break;
                     }

                     if (charCodeL > 1114111L) {
                        this.log.warn("Format 12 cmap contains character beyond UCS-4");
                     }

                     if (glyphIndexL > 2147483647L) {
                        this.log.error("glyphIndex > Integer.MAX_VALUE");
                     } else if (charCodeL > 2147483647L) {
                        this.log.error("startCharCode + j > Integer.MAX_VALUE");
                     } else {
                        if (charCodeL < 255L && charCodeL > (long)this.lastChar) {
                           this.lastChar = (short)((int)charCodeL);
                        }

                        int charCode = (int)charCodeL;
                        int glyphIndex = (int)glyphIndexL;
                        List ansiIndexes = null;
                        if (charCodeL <= 65535L) {
                           ansiIndexes = (List)this.ansiIndex.get((int)charCodeL);
                        }

                        this.unicodeMappings.add(new UnicodeMapping(this, glyphIndex, charCode));
                        this.mtxTab[glyphIndex].getUnicodeIndex().add(charCode);
                        if (ansiIndexes != null) {
                           Iterator var25 = ansiIndexes.iterator();

                           while(var25.hasNext()) {
                              Integer aIdx = (Integer)var25.next();
                              this.ansiWidth[aIdx] = this.mtxTab[glyphIndex].getWx();
                              if (this.log.isTraceEnabled()) {
                                 this.log.trace("Added width " + this.mtxTab[glyphIndex].getWx() + " uni: " + offset + " ansi: " + aIdx);
                              }
                           }
                        }
                     }
                  }
               } else {
                  this.log.warn("startCharCode outside Unicode range");
               }
            } else {
               this.log.warn("startCharCode outside Unicode range");
            }
         }
      }

      return true;
   }

   private boolean isInPrivateUseArea(int start, int end) {
      return this.isInPrivateUseArea(start) || this.isInPrivateUseArea(end);
   }

   private boolean isInPrivateUseArea(int unicode) {
      return unicode >= 57344 && unicode <= 63743;
   }

   public List getMtx() {
      return Collections.unmodifiableList(Arrays.asList(this.mtxTab));
   }

   public void readFont(FontFileReader in, String header) throws IOException {
      this.readFont(in, header, (String)null);
   }

   protected void initAnsiWidths() {
      this.ansiWidth = new int[256];

      int i;
      for(i = 0; i < 256; ++i) {
         this.ansiWidth[i] = this.mtxTab[0].getWx();
      }

      this.ansiIndex = new HashMap();

      for(i = 32; i < Glyphs.WINANSI_ENCODING.length; ++i) {
         Integer ansi = i;
         Integer uni = Integer.valueOf(Glyphs.WINANSI_ENCODING[i]);
         List v = (List)this.ansiIndex.get(uni);
         if (v == null) {
            v = new ArrayList();
            this.ansiIndex.put(uni, v);
         }

         ((List)v).add(ansi);
      }

   }

   public boolean readFont(FontFileReader in, String header, String name) throws IOException {
      this.initializeFont(in);
      if (!this.checkTTC(header, name)) {
         if (name == null) {
            throw new IllegalArgumentException("For TrueType collection you must specify which font to select (-ttcname)");
         } else {
            throw new IOException("Name does not exist in the TrueType collection: " + name);
         }
      } else {
         this.readDirTabs();
         this.readFontHeader();
         this.getNumGlyphs();
         if (this.log.isDebugEnabled()) {
            this.log.debug("Number of glyphs in font: " + this.numberOfGlyphs);
         }

         this.readHorizontalHeader();
         this.readHorizontalMetrics();
         this.initAnsiWidths();
         this.readPostScript();
         this.readOS2();
         this.determineAscDesc();
         this.readSVG();
         this.readName();
         boolean pcltFound = this.readPCLT();
         boolean valid = this.readCMAP();
         if (!valid) {
            return false;
         } else {
            this.createCMaps();
            this.updateBBoxAndOffset();
            if (this.useKerning) {
               this.readKerning();
            }

            this.handleCharacterSpacing(in);
            this.guessVerticalMetricsFromGlyphBBox();
            return true;
         }
      }
   }

   public void readFont(FontFileReader in, String header, MultiByteFont mbfont) throws IOException {
      this.readFont(in, header, mbfont.getTTCName());
   }

   protected abstract void updateBBoxAndOffset() throws IOException;

   protected abstract void readName() throws IOException;

   protected abstract void initializeFont(FontFileReader var1) throws IOException;

   protected void handleCharacterSpacing(FontFileReader in) throws IOException {
      if (this.useAdvanced) {
         try {
            OTFAdvancedTypographicTableReader atr = new OTFAdvancedTypographicTableReader(this, in);
            atr.readAll();
            this.advancedTableReader = atr;
         } catch (AdvancedTypographicTableFormatException var3) {
            this.log.warn("Encountered format constraint violation in advanced (typographic) table (AT) in font '" + this.getFullName() + "', ignoring AT data: " + var3.getMessage());
         }
      }

   }

   protected void createCMaps() {
      this.cmaps = new ArrayList();
      if (!this.unicodeMappings.isEmpty()) {
         Iterator e = this.unicodeMappings.iterator();
         UnicodeMapping um = (UnicodeMapping)e.next();
         UnicodeMapping lastMapping = um;
         int unicodeStart = um.getUnicodeIndex();

         int glyphStart;
         int unicodeEnd;
         for(glyphStart = um.getGlyphIndex(); e.hasNext(); lastMapping = um) {
            um = (UnicodeMapping)e.next();
            if (lastMapping.getUnicodeIndex() + 1 != um.getUnicodeIndex() || lastMapping.getGlyphIndex() + 1 != um.getGlyphIndex()) {
               unicodeEnd = lastMapping.getUnicodeIndex();
               this.cmaps.add(new CMapSegment(unicodeStart, unicodeEnd, glyphStart));
               unicodeStart = um.getUnicodeIndex();
               glyphStart = um.getGlyphIndex();
            }
         }

         unicodeEnd = lastMapping.getUnicodeIndex();
         this.cmaps.add(new CMapSegment(unicodeStart, unicodeEnd, glyphStart));
      }
   }

   public String getPostScriptName() {
      return this.postScriptName.length() == 0 ? FontUtil.stripWhiteSpace(this.getFullName()) : this.postScriptName;
   }

   PostScriptVersion getPostScriptVersion() {
      return this.postScriptVersion;
   }

   public Set getFamilyNames() {
      return this.familyNames;
   }

   public String getSubFamilyName() {
      return this.subFamilyName;
   }

   public String getFullName() {
      return this.fullName;
   }

   public String getCharSetName() {
      return "WinAnsiEncoding";
   }

   public int getCapHeight() {
      return this.convertTTFUnit2PDFUnit(this.capHeight);
   }

   public int getXHeight() {
      return this.convertTTFUnit2PDFUnit(this.xHeight);
   }

   protected int getPadSize(int currentPosition) {
      int padSize = 4 - currentPosition % 4;
      return padSize < 4 ? padSize : 0;
   }

   public int getFlags() {
      int flags = 32;
      if (this.italicAngle != 0L) {
         flags |= 64;
      }

      if (this.isFixedPitch != 0L) {
         flags |= 2;
      }

      if (this.hasSerifs) {
         flags |= 1;
      }

      return flags;
   }

   public int getWeightClass() {
      return this.usWeightClass;
   }

   public String getStemV() {
      return "0";
   }

   public String getItalicAngle() {
      String ia = Short.toString((short)((int)(this.italicAngle / 65536L)));
      return ia;
   }

   public int[] getFontBBox() {
      int[] fbb = new int[]{this.convertTTFUnit2PDFUnit(this.fontBBox1), this.convertTTFUnit2PDFUnit(this.fontBBox2), this.convertTTFUnit2PDFUnit(this.fontBBox3), this.convertTTFUnit2PDFUnit(this.fontBBox4)};
      return fbb;
   }

   public int[] getBBoxRaw() {
      int[] bbox = new int[]{this.fontBBox1, this.fontBBox2, this.fontBBox3, this.fontBBox4};
      return bbox;
   }

   public int getLowerCaseAscent() {
      return this.convertTTFUnit2PDFUnit(this.ascender);
   }

   public int getLowerCaseDescent() {
      return this.convertTTFUnit2PDFUnit(this.descender);
   }

   public short getLastChar() {
      return this.lastChar;
   }

   public short getFirstChar() {
      return 0;
   }

   public int[] getWidths() {
      int[] wx = new int[this.mtxTab.length];

      for(int i = 0; i < wx.length; ++i) {
         wx[i] = this.convertTTFUnit2PDFUnit(this.mtxTab[i].getWx());
      }

      return wx;
   }

   public Rectangle[] getBoundingBoxes() {
      Rectangle[] boundingBoxes = new Rectangle[this.mtxTab.length];

      for(int i = 0; i < boundingBoxes.length; ++i) {
         int[] boundingBox = this.mtxTab[i].getBoundingBox();
         boundingBoxes[i] = new Rectangle(this.convertTTFUnit2PDFUnit(boundingBox[0]), this.convertTTFUnit2PDFUnit(boundingBox[1]), this.convertTTFUnit2PDFUnit(boundingBox[2] - boundingBox[0]), this.convertTTFUnit2PDFUnit(boundingBox[3] - boundingBox[1]));
      }

      return boundingBoxes;
   }

   public int[] getBBox(int glyphIndex) {
      int[] bbox = new int[4];
      if (glyphIndex < this.mtxTab.length) {
         int[] bboxInTTFUnits = this.mtxTab[glyphIndex].getBoundingBox();

         for(int i = 0; i < 4; ++i) {
            bbox[i] = this.convertTTFUnit2PDFUnit(bboxInTTFUnits[i]);
         }
      }

      return bbox;
   }

   public int getCharWidth(int idx) {
      return this.convertTTFUnit2PDFUnit(this.ansiWidth[idx]);
   }

   public int getCharWidthRaw(int idx) {
      return this.ansiWidth != null ? this.ansiWidth[idx] : -1;
   }

   public Map getKerning() {
      return this.kerningTab;
   }

   public Map getAnsiKerning() {
      return this.ansiKerningTab;
   }

   public int getUnderlinePosition() {
      return this.convertTTFUnit2PDFUnit(this.underlinePosition);
   }

   public int getUnderlineThickness() {
      return this.convertTTFUnit2PDFUnit(this.underlineThickness);
   }

   public int getStrikeoutPosition() {
      return this.convertTTFUnit2PDFUnit(this.strikeoutPosition);
   }

   public int getStrikeoutThickness() {
      return this.convertTTFUnit2PDFUnit(this.strikeoutThickness);
   }

   public boolean isEmbeddable() {
      return this.isEmbeddable;
   }

   public boolean isCFF() {
      return this.isCFF;
   }

   protected void readDirTabs() throws IOException {
      int sfntVersion = this.fontFile.readTTFLong();
      switch (sfntVersion) {
         case 65536:
            this.log.debug("sfnt version: OpenType 1.0");
            break;
         case 1330926671:
            this.isCFF = true;
            this.log.debug("sfnt version: OpenType with CFF data");
            break;
         case 1953658213:
            this.log.debug("sfnt version: Apple TrueType");
            break;
         case 1954115633:
            this.log.debug("sfnt version: Apple Type 1 housed in sfnt wrapper");
            break;
         default:
            this.log.debug("Unknown sfnt version: " + Integer.toHexString(sfntVersion));
      }

      int ntabs = this.fontFile.readTTFUShort();
      this.fontFile.skip(6L);
      this.dirTabs = new HashMap();
      OFDirTabEntry[] pd = new OFDirTabEntry[ntabs];
      this.log.debug("Reading " + ntabs + " dir tables");

      for(int i = 0; i < ntabs; ++i) {
         pd[i] = new OFDirTabEntry();
         String tableName = pd[i].read(this.fontFile);
         this.dirTabs.put(OFTableName.getValue(tableName), pd[i]);
      }

      this.dirTabs.put(OFTableName.TABLE_DIRECTORY, new OFDirTabEntry(0L, (long)this.fontFile.getCurrentPos()));
      this.log.debug("dir tables: " + this.dirTabs.keySet());
   }

   protected void readFontHeader() throws IOException {
      this.seekTab(this.fontFile, OFTableName.HEAD, 16L);
      int flags = this.fontFile.readTTFUShort();
      if (this.log.isDebugEnabled()) {
         this.log.debug("flags: " + flags + " - " + Integer.toString(flags, 2));
      }

      this.upem = this.fontFile.readTTFUShort();
      if (this.log.isDebugEnabled()) {
         this.log.debug("unit per em: " + this.upem);
      }

      this.fontFile.skip(16L);
      this.fontBBox1 = this.fontFile.readTTFShort();
      this.fontBBox2 = this.fontFile.readTTFShort();
      this.fontBBox3 = this.fontFile.readTTFShort();
      this.fontBBox4 = this.fontFile.readTTFShort();
      if (this.log.isDebugEnabled()) {
         this.log.debug("font bbox: xMin=" + this.fontBBox1 + " yMin=" + this.fontBBox2 + " xMax=" + this.fontBBox3 + " yMax=" + this.fontBBox4);
      }

      this.fontFile.skip(6L);
      this.locaFormat = this.fontFile.readTTFShort();
   }

   protected void getNumGlyphs() throws IOException {
      this.seekTab(this.fontFile, OFTableName.MAXP, 4L);
      this.numberOfGlyphs = this.fontFile.readTTFUShort();
   }

   protected void readHorizontalHeader() throws IOException {
      this.seekTab(this.fontFile, OFTableName.HHEA, 4L);
      this.hheaAscender = this.fontFile.readTTFShort();
      this.hheaDescender = this.fontFile.readTTFShort();
      this.fontFile.skip(26L);
      this.nhmtx = this.fontFile.readTTFUShort();
      if (this.log.isDebugEnabled()) {
         this.log.debug("hhea.Ascender: " + this.formatUnitsForDebug(this.hheaAscender));
         this.log.debug("hhea.Descender: " + this.formatUnitsForDebug(this.hheaDescender));
         this.log.debug("Number of horizontal metrics: " + this.nhmtx);
      }

   }

   private void readSVG() throws IOException {
      OFDirTabEntry dirTab = (OFDirTabEntry)this.dirTabs.get(OFTableName.SVG);
      if (dirTab != null) {
         this.svgs = new LinkedHashMap();
         this.fontFile.seekSet(dirTab.getOffset());
         this.fontFile.readTTFUShort();
         this.fontFile.readTTFULong();
         this.fontFile.readTTFULong();
         int numEntries = this.fontFile.readTTFUShort();

         for(int i = 0; i < numEntries; ++i) {
            int startGlyphID = this.fontFile.readTTFUShort();
            this.fontFile.readTTFUShort();
            SVGGlyphData svgGlyph = new SVGGlyphData();
            svgGlyph.svgDocOffset = this.fontFile.readTTFULong();
            svgGlyph.svgDocLength = this.fontFile.readTTFULong();
            this.svgs.put(startGlyphID, svgGlyph);
         }

         Iterator var6 = this.svgs.values().iterator();

         while(var6.hasNext()) {
            SVGGlyphData entry = (SVGGlyphData)var6.next();
            this.seekTab(this.fontFile, OFTableName.SVG, entry.svgDocOffset);
            this.fontFile.readTTFUShort();
            this.fontFile.readTTFULong();
            this.fontFile.readTTFULong();
            entry.setSVG(this.fontFile.readTTFString((int)entry.svgDocLength));
         }
      }

   }

   protected void readHorizontalMetrics() throws IOException {
      this.seekTab(this.fontFile, OFTableName.HMTX, 0L);
      int mtxSize = Math.max(this.numberOfGlyphs, this.nhmtx);
      this.mtxTab = new OFMtxEntry[mtxSize];
      if (this.log.isTraceEnabled()) {
         this.log.trace("*** Widths array: \n");
      }

      int lastWidth;
      for(lastWidth = 0; lastWidth < mtxSize; ++lastWidth) {
         this.mtxTab[lastWidth] = new OFMtxEntry();
      }

      for(lastWidth = 0; lastWidth < this.nhmtx; ++lastWidth) {
         this.mtxTab[lastWidth].setWx(this.fontFile.readTTFUShort());
         this.mtxTab[lastWidth].setLsb(this.fontFile.readTTFUShort());
         if (this.log.isTraceEnabled()) {
            this.log.trace("   width[" + lastWidth + "] = " + this.convertTTFUnit2PDFUnit(this.mtxTab[lastWidth].getWx()) + ";");
         }
      }

      if (this.cid && this.nhmtx < mtxSize) {
         lastWidth = this.mtxTab[this.nhmtx - 1].getWx();

         for(int i = this.nhmtx; i < mtxSize; ++i) {
            this.mtxTab[i].setWx(lastWidth);
            this.mtxTab[i].setLsb(this.fontFile.readTTFUShort());
         }
      }

   }

   protected void readPostScript() throws IOException {
      this.seekTab(this.fontFile, OFTableName.POST, 0L);
      int postFormat = this.fontFile.readTTFLong();
      this.italicAngle = this.fontFile.readTTFULong();
      this.underlinePosition = this.fontFile.readTTFShort();
      this.underlineThickness = this.fontFile.readTTFShort();
      this.isFixedPitch = this.fontFile.readTTFULong();
      this.fontFile.skip(16L);
      this.log.debug("PostScript format: 0x" + Integer.toHexString(postFormat));
      int numGlyphStrings;
      switch (postFormat) {
         case 65536:
            this.log.debug("PostScript format 1");
            this.postScriptVersion = OpenFont.PostScriptVersion.V1;

            for(numGlyphStrings = 0; numGlyphStrings < MAC_GLYPH_ORDERING.length; ++numGlyphStrings) {
               this.mtxTab[numGlyphStrings].setName(MAC_GLYPH_ORDERING[numGlyphStrings]);
            }

            return;
         case 131072:
            this.log.debug("PostScript format 2");
            this.postScriptVersion = OpenFont.PostScriptVersion.V2;
            numGlyphStrings = 257;
            int l = this.fontFile.readTTFUShort();

            for(int i = 0; i < l; ++i) {
               this.mtxTab[i].setIndex(this.fontFile.readTTFUShort());
               if (this.mtxTab[i].getIndex() > numGlyphStrings && this.mtxTab[i].getIndex() <= 32767) {
                  numGlyphStrings = this.mtxTab[i].getIndex();
               }

               if (this.log.isTraceEnabled()) {
                  this.log.trace("PostScript index: " + this.mtxTab[i].getIndexAsString());
               }
            }

            String[] psGlyphsBuffer = new String[numGlyphStrings - 257];
            if (this.log.isDebugEnabled()) {
               this.log.debug("Reading " + numGlyphStrings + " glyphnames, that are not in the standard Macintosh set. Total number of glyphs=" + l);
            }

            int i;
            for(i = 0; i < psGlyphsBuffer.length; ++i) {
               psGlyphsBuffer[i] = this.fontFile.readTTFString(this.fontFile.readTTFUByte());
            }

            for(i = 0; i < l; ++i) {
               if (this.mtxTab[i].getIndex() < MAC_GLYPH_ORDERING.length) {
                  this.mtxTab[i].setName(MAC_GLYPH_ORDERING[this.mtxTab[i].getIndex()]);
               } else if (!this.mtxTab[i].isIndexReserved()) {
                  int k = this.mtxTab[i].getIndex() - MAC_GLYPH_ORDERING.length;
                  if (this.log.isTraceEnabled()) {
                     this.log.trace(k + " i=" + i + " mtx=" + this.mtxTab.length + " ps=" + psGlyphsBuffer.length);
                  }

                  this.mtxTab[i].setName(psGlyphsBuffer[k]);
               }
            }

            return;
         case 196608:
            this.log.debug("PostScript format 3");
            this.postScriptVersion = OpenFont.PostScriptVersion.V3;
            break;
         default:
            this.log.error("Unknown PostScript format: " + postFormat);
            this.postScriptVersion = OpenFont.PostScriptVersion.UNKNOWN;
      }

   }

   protected void readOS2() throws IOException {
      OFDirTabEntry os2Entry = (OFDirTabEntry)this.dirTabs.get(OFTableName.OS2);
      if (os2Entry != null) {
         this.seekTab(this.fontFile, OFTableName.OS2, 0L);
         int version = this.fontFile.readTTFUShort();
         if (this.log.isDebugEnabled()) {
            this.log.debug("OS/2 table: version=" + version + ", offset=" + os2Entry.getOffset() + ", len=" + os2Entry.getLength());
         }

         this.fontFile.skip(2L);
         this.usWeightClass = this.fontFile.readTTFUShort();
         this.fontFile.skip(2L);
         int fsType = this.fontFile.readTTFUShort();
         if (fsType == 2) {
            this.isEmbeddable = false;
         } else {
            this.isEmbeddable = true;
         }

         this.fontFile.skip(16L);
         this.strikeoutThickness = this.fontFile.readTTFShort();
         this.strikeoutPosition = this.fontFile.readTTFShort();
         this.fontFile.skip(2L);
         this.fontFile.skip(10L);
         this.fontFile.skip(16L);
         this.fontFile.skip(4L);
         this.fontFile.skip(6L);
         this.os2Ascender = this.fontFile.readTTFShort();
         this.os2Descender = this.fontFile.readTTFShort();
         if (this.log.isDebugEnabled()) {
            this.log.debug("sTypoAscender: " + this.os2Ascender + " -> internal " + this.convertTTFUnit2PDFUnit(this.os2Ascender));
            this.log.debug("sTypoDescender: " + this.os2Descender + " -> internal " + this.convertTTFUnit2PDFUnit(this.os2Descender));
         }

         int v = this.fontFile.readTTFShort();
         if (this.log.isDebugEnabled()) {
            this.log.debug("sTypoLineGap: " + v);
         }

         v = this.fontFile.readTTFUShort();
         if (this.log.isDebugEnabled()) {
            this.log.debug("usWinAscent: " + this.formatUnitsForDebug(v));
         }

         v = this.fontFile.readTTFUShort();
         if (this.log.isDebugEnabled()) {
            this.log.debug("usWinDescent: " + this.formatUnitsForDebug(v));
         }

         if (os2Entry.getLength() >= 90L) {
            this.fontFile.skip(8L);
            this.os2xHeight = this.fontFile.readTTFShort();
            this.os2CapHeight = this.fontFile.readTTFShort();
            if (this.log.isDebugEnabled()) {
               this.log.debug("sxHeight: " + this.os2xHeight);
               this.log.debug("sCapHeight: " + this.os2CapHeight);
            }
         }
      } else {
         this.isEmbeddable = true;
      }

   }

   protected boolean readPCLT() throws IOException {
      OFDirTabEntry dirTab = (OFDirTabEntry)this.dirTabs.get(OFTableName.PCLT);
      if (dirTab != null) {
         this.fontFile.seekSet(dirTab.getOffset() + 4L + 4L + 2L);
         this.xHeight = this.fontFile.readTTFUShort();
         this.log.debug("xHeight from PCLT: " + this.formatUnitsForDebug(this.xHeight));
         this.fontFile.skip(4L);
         this.capHeight = this.fontFile.readTTFUShort();
         this.log.debug("capHeight from PCLT: " + this.formatUnitsForDebug(this.capHeight));
         this.fontFile.skip(34L);
         int serifStyle = this.fontFile.readTTFUByte();
         serifStyle >>= 6;
         serifStyle &= 3;
         if (serifStyle == 1) {
            this.hasSerifs = false;
         } else {
            this.hasSerifs = true;
         }

         return true;
      } else {
         return false;
      }
   }

   protected void determineAscDesc() {
      int hheaBoxHeight = this.hheaAscender - this.hheaDescender;
      int os2BoxHeight = this.os2Ascender - this.os2Descender;
      if (this.os2Ascender > 0 && os2BoxHeight <= this.upem) {
         this.ascender = this.os2Ascender;
         this.descender = this.os2Descender;
      } else if (this.hheaAscender > 0 && hheaBoxHeight <= this.upem) {
         this.ascender = this.hheaAscender;
         this.descender = this.hheaDescender;
      } else if (this.os2Ascender > 0) {
         this.ascender = this.os2Ascender;
         this.descender = this.os2Descender;
      } else {
         this.ascender = this.hheaAscender;
         this.descender = this.hheaDescender;
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("Font box height: " + (this.ascender - this.descender));
         if (this.ascender - this.descender > this.upem) {
            this.log.debug("Ascender and descender together are larger than the em box.");
         }
      }

   }

   protected void guessVerticalMetricsFromGlyphBBox() {
      int localCapHeight = 0;
      int localXHeight = 0;
      int localAscender = 0;
      int localDescender = 0;
      OFMtxEntry[] var5 = this.mtxTab;
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         OFMtxEntry aMtxTab = var5[var7];
         if ("H".equals(aMtxTab.getName())) {
            localCapHeight = aMtxTab.getBoundingBox()[3];
         } else if ("x".equals(aMtxTab.getName())) {
            localXHeight = aMtxTab.getBoundingBox()[3];
         } else if ("d".equals(aMtxTab.getName())) {
            localAscender = aMtxTab.getBoundingBox()[3];
         } else if ("p".equals(aMtxTab.getName())) {
            localDescender = aMtxTab.getBoundingBox()[1];
         } else {
            List unicodeIndex = aMtxTab.getUnicodeIndex();
            if (unicodeIndex.size() > 0) {
               char ch = (char)(Integer)unicodeIndex.get(0);
               if (ch == 'H') {
                  localCapHeight = aMtxTab.getBoundingBox()[3];
               } else if (ch == 'x') {
                  localXHeight = aMtxTab.getBoundingBox()[3];
               } else if (ch == 'd') {
                  localAscender = aMtxTab.getBoundingBox()[3];
               } else if (ch == 'p') {
                  localDescender = aMtxTab.getBoundingBox()[1];
               }
            }
         }
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("Ascender from glyph 'd': " + this.formatUnitsForDebug(localAscender));
         this.log.debug("Descender from glyph 'p': " + this.formatUnitsForDebug(localDescender));
      }

      if (this.ascender - this.descender > this.upem) {
         this.log.debug("Replacing specified ascender/descender with derived values to get values which fit in the em box.");
         this.ascender = localAscender;
         this.descender = localDescender;
      }

      if (this.log.isDebugEnabled()) {
         this.log.debug("xHeight from glyph 'x': " + this.formatUnitsForDebug(localXHeight));
         this.log.debug("CapHeight from glyph 'H': " + this.formatUnitsForDebug(localCapHeight));
      }

      if (this.capHeight == 0) {
         this.capHeight = localCapHeight;
         if (this.capHeight == 0) {
            this.capHeight = this.os2CapHeight;
         }

         if (this.capHeight == 0) {
            this.log.debug("capHeight value could not be determined. The font may not work as expected.");
         }
      }

      if (this.xHeight == 0) {
         this.xHeight = localXHeight;
         if (this.xHeight == 0) {
            this.xHeight = this.os2xHeight;
         }

         if (this.xHeight == 0) {
            this.log.debug("xHeight value could not be determined. The font may not work as expected.");
         }
      }

   }

   protected void readKerning() throws IOException {
      this.kerningTab = new HashMap();
      this.ansiKerningTab = new HashMap();
      OFDirTabEntry dirTab = (OFDirTabEntry)this.dirTabs.get(OFTableName.KERN);
      if (dirTab != null) {
         this.seekTab(this.fontFile, OFTableName.KERN, 2L);

         Object adjTab;
         for(int n = this.fontFile.readTTFUShort(); n > 0; --n) {
            this.fontFile.skip(4L);
            int k = this.fontFile.readTTFUShort();
            if ((k & 1) == 0 || (k & 2) != 0 || (k & 4) != 0) {
               return;
            }

            if (k >> 8 == 0) {
               k = this.fontFile.readTTFUShort();
               this.fontFile.skip(6L);

               while(k-- > 0) {
                  int i = this.fontFile.readTTFUShort();
                  int j = this.fontFile.readTTFUShort();
                  int kpx = this.fontFile.readTTFShort();
                  if (kpx != 0) {
                     Integer iObj = this.glyphToUnicode(i);
                     Integer u2 = this.glyphToUnicode(j);
                     if (iObj == null) {
                        this.log.debug("Ignoring kerning pair because no Unicode index was found for the first glyph " + i);
                     } else if (u2 == null) {
                        this.log.debug("Ignoring kerning pair because Unicode index was found for the second glyph " + i);
                     } else {
                        adjTab = (Map)this.kerningTab.get(iObj);
                        if (adjTab == null) {
                           adjTab = new HashMap();
                        }

                        ((Map)adjTab).put(u2, this.convertTTFUnit2PDFUnit(kpx));
                        this.kerningTab.put(iObj, adjTab);
                     }
                  }
               }
            }
         }

         Iterator var21 = this.kerningTab.entrySet().iterator();

         while(true) {
            Integer unicodeKey;
            Integer ansiKey;
            Integer cidKey1;
            HashMap akpx;
            Iterator var27;
            do {
               if (!var21.hasNext()) {
                  return;
               }

               Map.Entry e1 = (Map.Entry)var21.next();
               Integer unicodeKey1 = (Integer)e1.getKey();
               cidKey1 = this.unicodeToGlyph(unicodeKey1);
               akpx = new HashMap();
               Map ckpx = (Map)e1.getValue();
               var27 = ckpx.entrySet().iterator();

               while(var27.hasNext()) {
                  Map.Entry e = (Map.Entry)var27.next();
                  unicodeKey = (Integer)e.getKey();
                  Integer cidKey2 = this.unicodeToGlyph(unicodeKey);
                  Integer kern = (Integer)e.getValue();
                  Iterator var13 = this.mtxTab[cidKey2].getUnicodeIndex().iterator();

                  while(var13.hasNext()) {
                     Object o = var13.next();
                     ansiKey = (Integer)o;
                     Integer[] ansiKeys = this.unicodeToWinAnsi(ansiKey);
                     Integer[] var17 = ansiKeys;
                     int var18 = ansiKeys.length;

                     for(int var19 = 0; var19 < var18; ++var19) {
                        Integer ansiKey = var17[var19];
                        akpx.put(ansiKey, kern);
                     }
                  }
               }
            } while(akpx.size() <= 0);

            var27 = this.mtxTab[cidKey1].getUnicodeIndex().iterator();

            while(var27.hasNext()) {
               adjTab = var27.next();
               unicodeKey = (Integer)adjTab;
               Integer[] ansiKeys = this.unicodeToWinAnsi(unicodeKey);
               Integer[] var30 = ansiKeys;
               int var31 = ansiKeys.length;

               for(int var32 = 0; var32 < var31; ++var32) {
                  ansiKey = var30[var32];
                  this.ansiKerningTab.put(ansiKey, akpx);
               }
            }
         }
      }
   }

   public void stream(TTFOutputStream ttfOut) throws IOException {
      SortedSet sortedDirTabs = this.sortDirTabMap(this.dirTabs);
      byte[] file = this.fontFile.getAllBytes();
      TTFTableOutputStream tableOut = ttfOut.getTableOutputStream();
      TTFGlyphOutputStream glyphOut = ttfOut.getGlyphOutputStream();
      ttfOut.startFontStream();
      Iterator var6 = sortedDirTabs.iterator();

      while(var6.hasNext()) {
         Map.Entry entry = (Map.Entry)var6.next();
         int offset = (int)((OFDirTabEntry)entry.getValue()).getOffset();
         int paddedLength = (int)((OFDirTabEntry)entry.getValue()).getLength();
         paddedLength += this.getPadSize(offset + paddedLength);
         if (((OFTableName)entry.getKey()).equals(OFTableName.GLYF)) {
            this.streamGlyf(glyphOut, file, offset, paddedLength);
         } else {
            tableOut.streamTable(file, offset, paddedLength);
         }
      }

      ttfOut.endFontStream();
   }

   private void streamGlyf(TTFGlyphOutputStream glyphOut, byte[] fontFile, int tableOffset, int tableLength) throws IOException {
      int glyphStart = false;
      int glyphEnd = 0;
      glyphOut.startGlyphStream();

      for(int i = 0; i < this.mtxTab.length - 1; ++i) {
         int glyphStart = (int)this.mtxTab[i].getOffset() + tableOffset;
         glyphEnd = (int)this.mtxTab[i + 1].getOffset() + tableOffset;
         glyphOut.streamGlyph(fontFile, glyphStart, glyphEnd - glyphStart);
      }

      glyphOut.streamGlyph(fontFile, glyphEnd, tableOffset + tableLength - glyphEnd);
      glyphOut.endGlyphStream();
   }

   SortedSet sortDirTabMap(Map directoryTabs) {
      SortedSet sortedSet = new TreeSet(new Comparator() {
         public int compare(Map.Entry o1, Map.Entry o2) {
            return (int)(((OFDirTabEntry)o1.getValue()).getOffset() - ((OFDirTabEntry)o2.getValue()).getOffset());
         }
      });
      sortedSet.addAll(directoryTabs.entrySet());
      return sortedSet;
   }

   public List getCMaps() {
      return this.cmaps;
   }

   protected final boolean checkTTC(String tag, String name) throws IOException {
      if (!"ttcf".equals(tag)) {
         this.fontFile.seekSet(0L);
         return true;
      } else {
         this.fontFile.skip(4L);
         int numDirectories = (int)this.fontFile.readTTFULong();
         long[] dirOffsets = new long[numDirectories];

         for(int i = 0; i < numDirectories; ++i) {
            dirOffsets[i] = this.fontFile.readTTFULong();
         }

         this.log.info("This is a TrueType collection file with " + numDirectories + " fonts");
         this.log.info("Containing the following fonts: ");
         boolean found = false;
         long dirTabOffset = 0L;

         for(int i = 0; i < numDirectories; ++i) {
            this.fontFile.seekSet(dirOffsets[i]);
            this.readDirTabs();
            this.readName();
            if (this.fullName.equals(name)) {
               found = true;
               dirTabOffset = dirOffsets[i];
               this.log.info(this.fullName + " <-- selected");
            } else {
               this.log.info(this.fullName);
            }

            this.notice = "";
            this.fullName = "";
            this.familyNames.clear();
            this.postScriptName = "";
            this.subFamilyName = "";
         }

         this.fontFile.seekSet(dirTabOffset);
         return found;
      }
   }

   public final List getTTCnames(FontFileReader in) throws IOException {
      this.fontFile = in;
      List fontNames = new ArrayList();
      String tag = in.readTTFString(4);
      if (!"ttcf".equals(tag)) {
         this.log.error("Not a TTC!");
         return null;
      } else {
         in.skip(4L);
         int numDirectories = (int)in.readTTFULong();
         long[] dirOffsets = new long[numDirectories];

         int i;
         for(i = 0; i < numDirectories; ++i) {
            dirOffsets[i] = in.readTTFULong();
         }

         this.log.info("This is a TrueType collection file with " + numDirectories + " fonts");
         this.log.info("Containing the following fonts: ");

         for(i = 0; i < numDirectories; ++i) {
            in.seekSet(dirOffsets[i]);
            this.readDirTabs();
            this.readName();
            this.log.info(this.fullName);
            fontNames.add(this.fullName);
            this.notice = "";
            this.fullName = "";
            this.familyNames.clear();
            this.postScriptName = "";
            this.subFamilyName = "";
         }

         in.seekSet(0L);
         return fontNames;
      }
   }

   private Integer[] unicodeToWinAnsi(int unicode) {
      List ret = new ArrayList();

      for(int i = 32; i < Glyphs.WINANSI_ENCODING.length; ++i) {
         if (unicode == Glyphs.WINANSI_ENCODING[i]) {
            ret.add(i);
         }
      }

      return (Integer[])ret.toArray(new Integer[ret.size()]);
   }

   public void printStuff() {
      System.out.println("Font name:   " + this.postScriptName);
      System.out.println("Full name:   " + this.fullName);
      System.out.println("Family name: " + this.familyNames);
      System.out.println("Subfamily name: " + this.subFamilyName);
      System.out.println("Notice:      " + this.notice);
      System.out.println("xHeight:     " + this.convertTTFUnit2PDFUnit(this.xHeight));
      System.out.println("capheight:   " + this.convertTTFUnit2PDFUnit(this.capHeight));
      int italic = (int)(this.italicAngle >> 16);
      System.out.println("Italic:      " + italic);
      System.out.print("ItalicAngle: " + (short)((int)(this.italicAngle / 65536L)));
      if (this.italicAngle % 65536L > 0L) {
         System.out.print("." + (short)((int)(this.italicAngle % 65536L * 1000L)) / 65536);
      }

      System.out.println();
      System.out.println("Ascender:    " + this.convertTTFUnit2PDFUnit(this.ascender));
      System.out.println("Descender:   " + this.convertTTFUnit2PDFUnit(this.descender));
      System.out.println("FontBBox:    [" + this.convertTTFUnit2PDFUnit(this.fontBBox1) + " " + this.convertTTFUnit2PDFUnit(this.fontBBox2) + " " + this.convertTTFUnit2PDFUnit(this.fontBBox3) + " " + this.convertTTFUnit2PDFUnit(this.fontBBox4) + "]");
   }

   private String formatUnitsForDebug(int units) {
      return units + " -> " + this.convertTTFUnit2PDFUnit(units) + " internal units";
   }

   private Integer glyphToUnicode(int glyphIndex) {
      return (Integer)this.glyphToUnicodeMap.get(glyphIndex);
   }

   private Integer unicodeToGlyph(int unicodeIndex) throws IOException {
      Integer result = (Integer)this.unicodeToGlyphMap.get(unicodeIndex);
      if (result == null) {
         throw new IOException("Glyph index not found for unicode value " + unicodeIndex);
      } else {
         return result;
      }
   }

   String getGlyphName(int glyphIndex) {
      return this.mtxTab[glyphIndex].getName();
   }

   public boolean hasAdvancedTable() {
      return this.advancedTableReader != null ? this.advancedTableReader.hasAdvancedTable() : false;
   }

   public GlyphDefinitionTable getGDEF() {
      return this.advancedTableReader != null ? this.advancedTableReader.getGDEF() : null;
   }

   public GlyphSubstitutionTable getGSUB() {
      return this.advancedTableReader != null ? this.advancedTableReader.getGSUB() : null;
   }

   public GlyphPositioningTable getGPOS() {
      return this.advancedTableReader != null ? this.advancedTableReader.getGPOS() : null;
   }

   public static void main(String[] args) {
      InputStream stream = null;

      try {
         boolean useKerning = true;
         boolean useAdvanced = true;
         stream = new FileInputStream(args[0]);
         FontFileReader reader = new FontFileReader(stream);
         String name = null;
         if (args.length >= 2) {
            name = args[1];
         }

         String header = OFFontLoader.readHeader(reader);
         boolean isCFF = header.equals("OTTO");
         OpenFont otfFile = isCFF ? new OTFFile() : new TTFFile(useKerning, useAdvanced);
         ((OpenFont)otfFile).readFont(reader, header, name);
         ((OpenFont)otfFile).printStuff();
      } catch (IOException var12) {
         System.err.println("Problem reading font: " + var12.toString());
         var12.printStackTrace(System.err);
      } finally {
         IOUtils.closeQuietly((InputStream)stream);
      }

   }

   public String getEmbedFontName() {
      return this.embedFontName;
   }

   public String getCopyrightNotice() {
      return this.notice;
   }

   static final class UnicodeMapping implements Comparable {
      private final int unicodeIndex;
      private final int glyphIndex;

      UnicodeMapping(OpenFont font, int glyphIndex, int unicodeIndex) {
         this.unicodeIndex = unicodeIndex;
         this.glyphIndex = glyphIndex;
         font.glyphToUnicodeMap.put(glyphIndex, unicodeIndex);
         font.unicodeToGlyphMap.put(unicodeIndex, glyphIndex);
      }

      public int getGlyphIndex() {
         return this.glyphIndex;
      }

      public int getUnicodeIndex() {
         return this.unicodeIndex;
      }

      public int hashCode() {
         int hc = this.unicodeIndex;
         hc = 19 * hc + (hc ^ this.glyphIndex);
         return hc;
      }

      public boolean equals(Object o) {
         if (o instanceof UnicodeMapping) {
            UnicodeMapping m = (UnicodeMapping)o;
            if (this.unicodeIndex != m.unicodeIndex) {
               return false;
            } else {
               return this.glyphIndex == m.glyphIndex;
            }
         } else {
            return false;
         }
      }

      public int compareTo(Object o) {
         if (o instanceof UnicodeMapping) {
            UnicodeMapping m = (UnicodeMapping)o;
            if (this.unicodeIndex > m.unicodeIndex) {
               return 1;
            } else {
               return this.unicodeIndex < m.unicodeIndex ? -1 : 0;
            }
         } else {
            return -1;
         }
      }
   }

   public static enum PostScriptVersion {
      V1,
      V2,
      V3,
      UNKNOWN;
   }
}
