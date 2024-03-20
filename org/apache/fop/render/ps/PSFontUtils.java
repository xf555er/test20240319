package org.apache.fop.render.ps;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFStandardString;
import org.apache.fop.fonts.Base14Font;
import org.apache.fop.fonts.CFFToType1Font;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.CIDSet;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.cff.CFFDataReader;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.OTFFile;
import org.apache.fop.fonts.truetype.OTFSubSetFile;
import org.apache.fop.fonts.truetype.OpenFont;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.fonts.truetype.TTFOutputStream;
import org.apache.fop.fonts.truetype.TTFSubSetFile;
import org.apache.fop.fonts.type1.Type1SubsetFile;
import org.apache.fop.render.ps.fonts.PSTTFOutputStream;
import org.apache.fop.util.HexEncoder;
import org.apache.xmlgraphics.fonts.Glyphs;
import org.apache.xmlgraphics.java2d.GeneralGraphics2DImagePainter;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

public class PSFontUtils extends org.apache.xmlgraphics.ps.PSFontUtils {
   protected static final Log log = LogFactory.getLog(PSFontUtils.class);

   public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo) throws IOException {
      return writeFontDict(gen, fontInfo, (PSEventProducer)null);
   }

   public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, PSEventProducer eventProducer) throws IOException {
      return writeFontDict(gen, fontInfo, fontInfo.getFonts(), true, eventProducer);
   }

   public static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, Map fonts, PSEventProducer eventProducer) throws IOException {
      return writeFontDict(gen, fontInfo, fonts, false, eventProducer);
   }

   private static Map writeFontDict(PSGenerator gen, FontInfo fontInfo, Map fonts, boolean encodeAllCharacters, PSEventProducer eventProducer) throws IOException {
      gen.commentln("%FOPBeginFontDict");
      Map fontResources = new HashMap();
      Iterator var6 = fonts.keySet().iterator();

      while(true) {
         String key;
         Typeface tf;
         do {
            if (!var6.hasNext()) {
               gen.commentln("%FOPEndFontDict");
               reencodeFonts(gen, fonts);
               return fontResources;
            }

            key = (String)var6.next();
            tf = getTypeFace(fontInfo, fonts, key);
            PSFontResource fontResource = embedFont(gen, tf, eventProducer);
            fontResources.put(key, fontResource);
         } while(!(tf instanceof SingleByteFont));

         SingleByteFont sbf = (SingleByteFont)tf;
         if (encodeAllCharacters) {
            sbf.encodeAllUnencodedCharacters();
         }

         int i = 0;

         for(int c = sbf.getAdditionalEncodingCount(); i < c; ++i) {
            SingleByteEncoding encoding = sbf.getAdditionalEncoding(i);
            defineEncoding(gen, encoding);
            String postFix = "_" + (i + 1);
            PSResource derivedFontRes;
            if (tf.getFontType() == FontType.TRUETYPE && sbf.getTrueTypePostScriptVersion() != OpenFont.PostScriptVersion.V2) {
               derivedFontRes = defineDerivedTrueTypeFont(gen, eventProducer, tf.getEmbedFontName(), tf.getEmbedFontName() + postFix, encoding, sbf.getCMap());
            } else {
               derivedFontRes = defineDerivedFont(gen, tf.getEmbedFontName(), tf.getEmbedFontName() + postFix, encoding.getName());
            }

            fontResources.put(key + postFix, PSFontResource.createFontResource(derivedFontRes));
         }
      }
   }

   private static void reencodeFonts(PSGenerator gen, Map fonts) throws IOException {
      ResourceTracker tracker = gen.getResourceTracker();
      Iterator var3;
      if (!tracker.isResourceSupplied(WINANSI_ENCODING_RESOURCE)) {
         var3 = fonts.values().iterator();

         while(var3.hasNext()) {
            Typeface tf = (Typeface)var3.next();
            if (tf instanceof LazyFont) {
               tf = ((LazyFont)tf).getRealFont();
               if (tf instanceof SingleByteFont && ((SingleByteFont)tf).getEncoding().getName().equals("custom")) {
                  defineEncoding(gen, ((SingleByteFont)tf).getEncoding());
               }
            }
         }

         defineWinAnsiEncoding(gen);
      }

      gen.commentln("%FOPBeginFontReencode");
      var3 = fonts.entrySet().iterator();

      while(true) {
         Typeface tf;
         do {
            if (!var3.hasNext()) {
               gen.commentln("%FOPEndFontReencode");
               return;
            }

            Map.Entry e = (Map.Entry)var3.next();
            String key = (String)e.getKey();
            tf = (Typeface)e.getValue();
            if (!(tf instanceof LazyFont)) {
               break;
            }

            tf = ((LazyFont)tf).getRealFont();
         } while(tf == null);

         if (null != tf.getEncodingName() && !"SymbolEncoding".equals(tf.getEncodingName()) && !"ZapfDingbatsEncoding".equals(tf.getEncodingName())) {
            if (tf instanceof Base14Font) {
               redefineFontEncoding(gen, tf.getEmbedFontName(), tf.getEncodingName());
            } else if (tf instanceof SingleByteFont) {
               SingleByteFont sbf = (SingleByteFont)tf;
               if (!sbf.isUsingNativeEncoding()) {
                  redefineFontEncoding(gen, tf.getEmbedFontName(), tf.getEncodingName());
               }
            }
         }
      }
   }

   private static Typeface getTypeFace(FontInfo fontInfo, Map fonts, String key) {
      Typeface tf = (Typeface)fonts.get(key);
      if (tf instanceof LazyFont) {
         tf = ((LazyFont)tf).getRealFont();
      }

      if (tf == null) {
         String fallbackKey = fontInfo.getInternalFontKey(Font.DEFAULT_FONT);
         tf = (Typeface)fonts.get(fallbackKey);
      }

      return tf;
   }

   private static PSFontResource embedFont(PSGenerator gen, Typeface tf, PSEventProducer eventProducer) throws IOException {
      boolean embeddedFont = false;
      FontType fontType = tf.getFontType();
      PSFontResource fontResource = null;
      PSResource fontRes = new PSResource("font", tf.getEmbedFontName());
      if ((fontType == FontType.TYPE1 || fontType == FontType.TRUETYPE || fontType == FontType.TYPE0 || fontType == FontType.TYPE1C) && tf instanceof CustomFont) {
         CustomFont cf = (CustomFont)tf;
         if (isEmbeddable(cf)) {
            List ins = getInputStreamOnFont(gen, cf);
            if (ins != null) {
               int i = 0;

               for(Iterator var10 = ins.iterator(); var10.hasNext(); ++i) {
                  InputStream in = (InputStream)var10.next();
                  if (i > 0) {
                     fontRes = new PSResource("font", tf.getEmbedFontName() + "." + i);
                  }

                  if (fontType == FontType.TYPE0 || fontType == FontType.TYPE1C) {
                     if (((MultiByteFont)tf).isOTFFile()) {
                        checkPostScriptLevel3(gen, eventProducer, "OpenType CFF");
                        embedType2CFF(gen, (MultiByteFont)tf, in);
                     } else {
                        if (gen.embedIdentityH()) {
                           checkPostScriptLevel3(gen, eventProducer, "TrueType");
                           gen.includeProcsetCIDInitResource();
                        }

                        PSResource cidFontResource = embedType2CIDFont(gen, (MultiByteFont)tf, in);
                        fontResource = PSFontResource.createFontResource(fontRes, gen.getProcsetCIDInitResource(), gen.getIdentityHCMapResource(), cidFontResource);
                     }
                  }

                  gen.writeDSCComment("BeginResource", (Object)fontRes);
                  if (fontType == FontType.TYPE1) {
                     embedType1Font(gen, (CustomFont)tf, in);
                     if (fontResource == null) {
                        fontResource = PSFontResource.createFontResource(fontRes);
                     }
                  } else if (fontType == FontType.TRUETYPE) {
                     embedTrueTypeFont(gen, (SingleByteFont)tf, in);
                     fontResource = PSFontResource.createFontResource(fontRes);
                  } else if (!((MultiByteFont)tf).isOTFFile()) {
                     composeType0Font(gen, (MultiByteFont)tf);
                  }

                  gen.writeDSCComment("EndResource");
                  gen.getResourceTracker().registerSuppliedResource(fontRes);
                  embeddedFont = true;
               }
            } else {
               gen.commentln("%WARNING: Could not embed font: " + cf.getEmbedFontName());
               log.warn("Font " + cf.getEmbedFontName() + " is marked as supplied in the PostScript file but could not be embedded!");
            }
         }

         if (!embeddedFont) {
            gen.writeDSCComment("IncludeResource", (Object)fontRes);
            fontResource = PSFontResource.createFontResource(fontRes);
         }

         return fontResource;
      } else {
         gen.writeDSCComment("IncludeResource", (Object)fontRes);
         fontResource = PSFontResource.createFontResource(fontRes);
         return fontResource;
      }
   }

   private static void checkPostScriptLevel3(PSGenerator gen, PSEventProducer eventProducer, String fontType) {
      if (gen.getPSLevel() < 3) {
         if (eventProducer == null) {
            throw new IllegalStateException("PostScript Level 3 is required to use " + fontType + " fonts, configured level is " + gen.getPSLevel());
         }

         eventProducer.postscriptLevel3Needed(gen);
      }

   }

   private static void embedType1Font(PSGenerator gen, CustomFont font, InputStream fontStream) throws IOException {
      if (font.getEmbeddingMode() == EmbeddingMode.AUTO) {
         font.setEmbeddingMode(EmbeddingMode.FULL);
      }

      byte[] fullFont = IOUtils.toByteArray(fontStream);
      InputStream fontStream = new ByteArrayInputStream(fullFont);
      boolean embed = true;
      if (font.getEmbeddingMode() == EmbeddingMode.SUBSET) {
         Type1SubsetFile subset = new Type1SubsetFile();
         byte[] byteSubset = subset.createSubset(fontStream, (SingleByteFont)font);
         fontStream = new ByteArrayInputStream(byteSubset);
      }

      embedType1Font(gen, fontStream);
      if (font.getEmbeddingMode() == EmbeddingMode.SUBSET) {
         writeEncoding(gen, (SingleByteFont)font);
      }

   }

   private static void writeEncoding(PSGenerator gen, SingleByteFont font) throws IOException {
      String psName = font.getEmbedFontName();
      gen.writeln("/" + psName + ".0.enc [ ");
      int lengthCount = 0;
      int charCount = 1;
      int encodingCount = 0;
      StringBuilder line = new StringBuilder();
      int lastGid = 0;
      Set keySet = font.getUsedGlyphNames().keySet();
      Iterator var9 = keySet.iterator();

      while(var9.hasNext()) {
         int gid = (Integer)var9.next();

         for(int i = lastGid; i < gid - 1; ++i) {
            line.append("/.notdef ");
            ++lengthCount;
            if (lengthCount == 8) {
               gen.writeln(line.toString());
               line = new StringBuilder();
               lengthCount = 0;
            }
         }

         lastGid = gid;
         line.append((String)font.getUsedGlyphNames().get(gid) + " ");
         ++lengthCount;
         ++charCount;
         if (lengthCount == 8) {
            gen.writeln(line.toString());
            line = new StringBuilder();
            lengthCount = 0;
         }

         if (charCount > 256) {
            ++encodingCount;
            charCount = 1;
            gen.writeln(line.toString());
            line = new StringBuilder();
            lengthCount = 0;
            gen.writeln("] def");
            gen.writeln(String.format("/%s.%d %s.%d.enc /%s RE", psName, encodingCount - 1, psName, encodingCount - 1, psName));
            gen.writeln("/" + psName + "." + encodingCount + ".enc [ ");
         }
      }

      gen.writeln(line.toString());
      gen.writeln("] def");
      gen.writeln(String.format("/%s.%d %s.%d.enc /%s RE", psName, encodingCount, psName, encodingCount, psName));
   }

   private static void embedTrueTypeFont(PSGenerator gen, SingleByteFont font, InputStream fontStream) throws IOException {
      gen.commentln("%!PS-TrueTypeFont-65536-65536-1");
      gen.writeln("11 dict begin");
      if (font.getEmbeddingMode() == EmbeddingMode.AUTO) {
         font.setEmbeddingMode(EmbeddingMode.SUBSET);
      }

      FontFileReader reader = new FontFileReader(fontStream);
      TTFFile ttfFile = new TTFFile();
      ttfFile.readFont(reader, font.getFullName());
      createType42DictionaryEntries(gen, font, font.getCMap(), ttfFile);
      gen.writeln("FontName currentdict end definefont pop");
   }

   private static void createType42DictionaryEntries(PSGenerator gen, CustomFont font, CMapSegment[] cmap, TTFFile ttfFile) throws IOException {
      gen.write("/FontName /");
      gen.write(font.getEmbedFontName());
      gen.writeln(" def");
      gen.writeln("/PaintType 0 def");
      gen.writeln("/FontMatrix [1 0 0 1 0 0] def");
      writeFontBBox(gen, font);
      gen.writeln("/FontType 42 def");
      gen.writeln("/Encoding 256 array");
      gen.writeln("0 1 255{1 index exch/.notdef put}for");
      Set glyphNames = new HashSet();
      boolean buildCharStrings;
      if (font.getFontType() == FontType.TYPE0 && font.getEmbeddingMode() != EmbeddingMode.FULL) {
         buildCharStrings = false;
      } else {
         buildCharStrings = true;

         for(int i = 0; i < Glyphs.WINANSI_ENCODING.length; ++i) {
            gen.write("dup ");
            gen.write(i);
            gen.write(" /");
            String glyphName = Glyphs.charToGlyphName(Glyphs.WINANSI_ENCODING[i]);
            if (glyphName.equals("")) {
               gen.write(".notdef");
            } else {
               gen.write(glyphName);
               glyphNames.add(glyphName);
            }

            gen.writeln(" put");
         }
      }

      gen.writeln("readonly def");
      TTFOutputStream ttfOut = new PSTTFOutputStream(gen);
      ttfFile.stream(ttfOut);
      buildCharStrings(gen, buildCharStrings, cmap, glyphNames, font);
   }

   private static void buildCharStrings(PSGenerator gen, boolean buildCharStrings, CMapSegment[] cmap, Set glyphNames, CustomFont font) throws IOException {
      gen.write("/CharStrings ");
      int var7;
      if (!buildCharStrings) {
         gen.write(1);
      } else if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
         int charCount = 1;
         CMapSegment[] var6 = cmap;
         var7 = cmap.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            CMapSegment segment = var6[var8];
            charCount += segment.getUnicodeEnd() - segment.getUnicodeStart() + 1;
         }

         gen.write(charCount);
      } else {
         gen.write(font.getCMap().length);
      }

      gen.writeln(" dict dup begin");
      gen.write("/");
      gen.write(".notdef");
      gen.writeln(" 0 def");
      if (!buildCharStrings) {
         gen.writeln("end readonly def");
      } else {
         if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
            CMapSegment[] var13 = cmap;
            int var15 = cmap.length;

            for(var7 = 0; var7 < var15; ++var7) {
               CMapSegment segment = var13[var7];
               int glyphIndex = segment.getGlyphStartIndex();

               for(int ch = segment.getUnicodeStart(); ch <= segment.getUnicodeEnd(); ++ch) {
                  char ch16 = (char)ch;
                  String glyphName = Glyphs.charToGlyphName(ch16);
                  if ("".equals(glyphName)) {
                     glyphName = "u" + Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
                  }

                  writeGlyphDefs(gen, glyphName, glyphIndex);
                  ++glyphIndex;
               }
            }
         } else {
            Iterator var14 = glyphNames.iterator();

            while(var14.hasNext()) {
               String name = (String)var14.next();
               writeGlyphDefs(gen, name, getGlyphIndex(Glyphs.getUnicodeSequenceForGlyphName(name).charAt(0), font.getCMap()));
            }
         }

         gen.writeln("end readonly def");
      }
   }

   private static void writeGlyphDefs(PSGenerator gen, String glyphName, int glyphIndex) throws IOException {
      gen.write("/");
      gen.write(glyphName);
      gen.write(" ");
      gen.write(glyphIndex);
      gen.writeln(" def");
   }

   private static int getGlyphIndex(char c, CMapSegment[] cmap) {
      CMapSegment[] var2 = cmap;
      int var3 = cmap.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         CMapSegment segment = var2[var4];
         if (segment.getUnicodeStart() <= c && c <= segment.getUnicodeEnd()) {
            return segment.getGlyphStartIndex() + c - segment.getUnicodeStart();
         }
      }

      return 0;
   }

   private static void composeType0Font(PSGenerator gen, MultiByteFont font) throws IOException {
      String psName = font.getEmbedFontName();
      gen.write("/");
      gen.write(psName);
      gen.write(" /Identity-H [/");
      gen.write(psName);
      gen.writeln("] composefont pop");
   }

   private static void embedType2CFF(PSGenerator gen, MultiByteFont font, InputStream fontStream) throws IOException {
      FontFileReader reader = new FontFileReader(fontStream);
      CFFDataReader cffReader = new CFFDataReader(reader);
      if (cffReader.getFDSelect() != null) {
         throw new UnsupportedOperationException("CID-Keyed OTF CFF fonts are not supported for PostScript output.");
      } else {
         String psName;
         byte[] bytes;
         int lengthCount;
         int gid;
         int encodingCount;
         if (font.getEmbeddingMode() == EmbeddingMode.FULL) {
            font.setFontName(new String(cffReader.getNameIndex().getValue(0)));
            psName = font.getEmbedFontName();
            Map topDICT = cffReader.getTopDictEntries();
            lengthCount = ((Number)((CFFDataReader.DICTEntry)topDICT.get("charset")).getOperands().get(0)).intValue();

            for(gid = 0; gid < cffReader.getCharStringIndex().getNumObjects(); ++gid) {
               encodingCount = cffReader.getSIDFromGID(lengthCount, gid);
               if (encodingCount < 391) {
                  font.mapUsedGlyphName(gid, CFFStandardString.getName(encodingCount));
               } else {
                  int index = encodingCount - 391;
                  if (index < cffReader.getStringIndex().getNumObjects()) {
                     font.mapUsedGlyphName(gid, new String(cffReader.getStringIndex().getValue(index)));
                  } else {
                     font.mapUsedGlyphName(gid, ".notdef");
                  }
               }
            }

            bytes = OTFFile.getCFFData(reader);
         } else {
            psName = font.getEmbedFontName();
            OTFSubSetFile otfFile = new OTFSubSetFile();
            otfFile.readFont(reader, psName, font);
            bytes = otfFile.getFontSubset();
         }

         gen.writeln("%!PS-Adobe-3.0 Resource-FontSet");
         gen.writeln("%%DocumentNeedResources:ProcSet(FontSetInit)");
         gen.writeln("%%Title:(FontSet/" + psName + ")");
         gen.writeln("%%Version: 1.000");
         gen.writeln("%%EndComments");
         gen.writeln("%%IncludeResource:ProcSet(FontSetInit)");
         gen.writeln("%%BeginResource: FontSet (" + psName + ")");
         gen.writeln("/FontSetInit /ProcSet findresource begin");
         String fontDeclaration = "/" + psName + " " + bytes.length + " StartData";
         gen.writeln("%%BeginData: " + (fontDeclaration.length() + 1 + bytes.length) + " Binary Bytes");
         gen.writeln(fontDeclaration);
         gen.writeByteArr(bytes);
         gen.writeln("%%EndData");
         gen.writeln("%%EndResource");
         gen.writeln("/" + psName + ".0.enc [ ");
         lengthCount = 0;
         gid = 1;
         encodingCount = 0;
         String line = "";
         Iterator var12 = font.getUsedGlyphNames().keySet().iterator();

         while(var12.hasNext()) {
            int gid = (Integer)var12.next();
            line = line + "/" + (String)font.getUsedGlyphNames().get(gid) + " ";
            ++lengthCount;
            ++gid;
            if (lengthCount == 8) {
               gen.writeln(line);
               line = "";
               lengthCount = 0;
            }

            if (gid > 256) {
               ++encodingCount;
               gid = 1;
               gen.writeln(line);
               line = "";
               lengthCount = 0;
               gen.writeln("] def");
               gen.writeln(String.format("/%s.%d %s.%d.enc /%s RE", psName, encodingCount - 1, psName, encodingCount - 1, psName));
               gen.writeln("/" + psName + "." + encodingCount + ".enc [ ");
            }
         }

         gen.writeln(line);
         gen.writeln("] def");
         gen.writeln(String.format("/%s.%d %s.%d.enc /%s RE", psName, encodingCount, psName, encodingCount, psName));
      }
   }

   private static PSResource embedType2CIDFont(PSGenerator gen, MultiByteFont font, InputStream fontStream) throws IOException {
      assert font.getCIDType() == CIDFontType.CIDTYPE2;

      String psName = font.getEmbedFontName();
      gen.write("%%BeginResource: CIDFont ");
      gen.writeln(psName);
      gen.write("%%Title: (");
      gen.write(psName);
      gen.writeln(" Adobe Identity 0)");
      gen.writeln("%%Version: 1");
      gen.writeln("/CIDInit /ProcSet findresource begin");
      gen.writeln("20 dict begin");
      gen.write("/CIDFontName /");
      gen.write(psName);
      gen.writeln(" def");
      gen.writeln("/CIDFontVersion 1 def");
      gen.write("/CIDFontType ");
      gen.write(font.getCIDType().getValue());
      gen.writeln(" def");
      gen.writeln("/CIDSystemInfo 3 dict dup begin");
      gen.writeln("  /Registry (Adobe) def");
      gen.writeln("  /Ordering (Identity) def");
      gen.writeln("  /Supplement 0 def");
      gen.writeln("end def");
      gen.write("/CIDCount ");
      CIDSet cidSet = font.getCIDSet();
      int numberOfGlyphs = cidSet.getNumberOfGlyphs();
      gen.write(numberOfGlyphs);
      gen.writeln(" def");
      gen.writeln("/GDBytes 2 def");
      gen.writeln("/CIDMap [<");
      int colCount = 0;
      int lineCount = 1;
      int nextBitSet = 0;
      int previousBitSet = false;

      String gid;
      for(int cid = 0; cid < numberOfGlyphs; ++cid) {
         if (colCount++ == 20) {
            gen.newLine();
            colCount = 1;
            if (lineCount++ == 800) {
               gen.writeln("> <");
               lineCount = 1;
            }
         }

         if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
            gid = HexEncoder.encode(cid, 4);
         } else {
            int previousBitSet = nextBitSet;
            nextBitSet = cidSet.getGlyphIndices().nextSetBit(nextBitSet);

            while(previousBitSet++ < nextBitSet) {
               gen.write("0000");
               ++cid;
               if (colCount++ == 20) {
                  gen.newLine();
                  colCount = 1;
                  if (lineCount++ == 800) {
                     gen.writeln("> <");
                     lineCount = 1;
                  }
               }
            }

            gid = HexEncoder.encode(nextBitSet, 4);
            ++nextBitSet;
         }

         gen.write(gid);
      }

      gen.writeln(">] def");
      FontFileReader reader = new FontFileReader(fontStream);
      gid = OFFontLoader.readHeader(reader);
      Object ttfFile;
      if (font.getEmbeddingMode() != EmbeddingMode.FULL) {
         ttfFile = new TTFSubSetFile();
         ((TTFSubSetFile)ttfFile).readFont(reader, font.getTTCName(), gid, font.getUsedGlyphs());
      } else {
         ttfFile = new TTFFile();
         ((TTFFile)ttfFile).readFont(reader, font.getTTCName());
      }

      createType42DictionaryEntries(gen, font, new CMapSegment[0], (TTFFile)ttfFile);
      gen.writeln("CIDFontName currentdict end /CIDFont defineresource pop");
      gen.writeln("end");
      gen.writeln("%%EndResource");
      PSResource cidFontResource = new PSResource("cidfont", psName);
      gen.getResourceTracker().registerSuppliedResource(cidFontResource);
      return cidFontResource;
   }

   private static void writeFontBBox(PSGenerator gen, CustomFont font) throws IOException {
      int[] bbox = font.getFontBBox();
      gen.write("/FontBBox[");

      for(int i = 0; i < 4; ++i) {
         gen.write(" ");
         gen.write(bbox[i]);
      }

      gen.writeln(" ] def");
   }

   private static boolean isEmbeddable(CustomFont font) {
      return font.isEmbeddable();
   }

   private static List getInputStreamOnFont(PSGenerator gen, CustomFont font) throws IOException {
      if (isEmbeddable(font)) {
         List fonts = new ArrayList();
         InputStream in = font.getInputStream();
         if (in == null) {
            return font instanceof CFFToType1Font ? ((CFFToType1Font)font).getInputStreams() : null;
         } else {
            if (!(in instanceof BufferedInputStream)) {
               in = new BufferedInputStream((InputStream)in);
            }

            fonts.add(in);
            return fonts;
         }
      } else {
         return null;
      }
   }

   public static Map determineSuppliedFonts(ResourceTracker resTracker, FontInfo fontInfo, Map fonts) {
      Map fontResources = new HashMap();
      Iterator var4 = fonts.keySet().iterator();

      while(true) {
         Typeface tf;
         do {
            PSResource fontRes;
            FontType fontType;
            do {
               do {
                  if (!var4.hasNext()) {
                     return fontResources;
                  }

                  String key = (String)var4.next();
                  tf = getTypeFace(fontInfo, fonts, key);
                  fontRes = new PSResource("font", tf.getEmbedFontName());
                  fontResources.put(key, fontRes);
                  fontType = tf.getFontType();
               } while(fontType != FontType.TYPE1 && fontType != FontType.TRUETYPE && fontType != FontType.TYPE0);
            } while(!(tf instanceof CustomFont));

            CustomFont cf = (CustomFont)tf;
            if (isEmbeddable(cf)) {
               if (fontType == FontType.TYPE0) {
                  resTracker.registerSuppliedResource(new PSResource("cidfont", tf.getEmbedFontName()));
                  resTracker.registerSuppliedResource(new PSResource("cmap", "Identity-H"));
               }

               resTracker.registerSuppliedResource(fontRes);
            }
         } while(!(tf instanceof SingleByteFont));

         SingleByteFont sbf = (SingleByteFont)tf;
         int i = 0;

         for(int c = sbf.getAdditionalEncodingCount(); i < c; ++i) {
            SingleByteEncoding encoding = sbf.getAdditionalEncoding(i);
            PSResource encodingRes = new PSResource("encoding", encoding.getName());
            resTracker.registerSuppliedResource(encodingRes);
            PSResource derivedFontRes = new PSResource("font", tf.getEmbedFontName() + "_" + (i + 1));
            resTracker.registerSuppliedResource(derivedFontRes);
         }
      }
   }

   public static PSResource defineEncoding(PSGenerator gen, SingleByteEncoding encoding) throws IOException {
      PSResource res = new PSResource("encoding", encoding.getName());
      gen.writeDSCComment("BeginResource", (Object)res);
      gen.writeln("/" + encoding.getName() + " [");
      String[] charNames = encoding.getCharNameMap();

      for(int i = 0; i < 256; ++i) {
         if (i > 0) {
            if (i % 5 == 0) {
               gen.newLine();
            } else {
               gen.write(" ");
            }
         }

         String glyphname = null;
         if (i < charNames.length) {
            glyphname = charNames[i];
         }

         if (glyphname == null || "".equals(glyphname)) {
            glyphname = ".notdef";
         }

         gen.write("/");
         gen.write(glyphname);
      }

      gen.newLine();
      gen.writeln("] def");
      gen.writeDSCComment("EndResource");
      gen.getResourceTracker().registerSuppliedResource(res);
      return res;
   }

   public static PSResource defineDerivedFont(PSGenerator gen, String baseFontName, String fontName, String encoding) throws IOException {
      PSResource res = new PSResource("font", fontName);
      gen.writeDSCComment("BeginResource", (Object)res);
      gen.commentln("%XGCDependencies: font " + baseFontName);
      gen.commentln("%XGC+ encoding " + encoding);
      gen.writeln("/" + baseFontName + " findfont");
      gen.writeln("dup length dict begin");
      gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
      gen.writeln("  /Encoding " + encoding + " def");
      gen.writeln("  currentdict");
      gen.writeln("end");
      gen.writeln("/" + fontName + " exch definefont pop");
      gen.writeDSCComment("EndResource");
      gen.getResourceTracker().registerSuppliedResource(res);
      return res;
   }

   private static PSResource defineDerivedTrueTypeFont(PSGenerator gen, PSEventProducer eventProducer, String baseFontName, String fontName, SingleByteEncoding encoding, CMapSegment[] cmap) throws IOException {
      checkPostScriptLevel3(gen, eventProducer, "TrueType");
      PSResource res = new PSResource("font", fontName);
      gen.writeDSCComment("BeginResource", (Object)res);
      gen.commentln("%XGCDependencies: font " + baseFontName);
      gen.commentln("%XGC+ encoding " + encoding.getName());
      gen.writeln("/" + baseFontName + " findfont");
      gen.writeln("dup length dict begin");
      gen.writeln("  {1 index /FID ne {def} {pop pop} ifelse} forall");
      gen.writeln("  /Encoding " + encoding.getName() + " def");
      gen.writeln("  /CharStrings 256 dict dup begin");
      String[] charNameMap = encoding.getCharNameMap();
      char[] unicodeCharMap = encoding.getUnicodeCharMap();

      assert charNameMap.length == unicodeCharMap.length;

      for(int i = 0; i < charNameMap.length; ++i) {
         String glyphName = charNameMap[i];
         gen.write("    /");
         gen.write(glyphName);
         gen.write(" ");
         if (glyphName.equals(".notdef")) {
            gen.write(0);
         } else {
            gen.write(getGlyphIndex(unicodeCharMap[i], cmap));
         }

         gen.writeln(" def");
      }

      gen.writeln("  end readonly def");
      gen.writeln("  currentdict");
      gen.writeln("end");
      gen.writeln("/" + fontName + " exch definefont pop");
      gen.writeDSCComment("EndResource");
      gen.getResourceTracker().registerSuppliedResource(res);
      return res;
   }

   public static void addFallbackFonts(FontInfo fontInfo, GeneralGraphics2DImagePainter painter) throws IOException {
      Iterator var2 = fontInfo.getFontTriplets().entrySet().iterator();

      while(var2.hasNext()) {
         Map.Entry x = (Map.Entry)var2.next();
         String name = ((FontTriplet)x.getKey()).getName();
         Typeface typeface = (Typeface)fontInfo.getFonts().get(x.getValue());
         painter.addFallbackFont(name, typeface);
      }

   }
}
