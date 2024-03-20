package org.apache.fop.fonts.truetype;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.CFFToType1Font;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.NamedCharacter;
import org.apache.fop.fonts.SingleByteFont;
import org.apache.fop.util.HexEncoder;

public class OFFontLoader extends FontLoader {
   private MultiByteFont multiFont;
   private SingleByteFont singleFont;
   private final String subFontName;
   private EncodingMode encodingMode;
   private EmbeddingMode embeddingMode;
   private boolean simulateStyle;
   private boolean embedAsType1;
   private boolean useSVG;

   public OFFontLoader(URI fontFileURI, InternalResourceResolver resourceResolver) {
      this(fontFileURI, (String)null, true, EmbeddingMode.AUTO, EncodingMode.AUTO, true, true, resourceResolver, false, false, true);
   }

   public OFFontLoader(URI fontFileURI, String subFontName, boolean embedded, EmbeddingMode embeddingMode, EncodingMode encodingMode, boolean useKerning, boolean useAdvanced, InternalResourceResolver resolver, boolean simulateStyle, boolean embedAsType1, boolean useSVG) {
      super(fontFileURI, embedded, useKerning, useAdvanced, resolver);
      this.subFontName = subFontName;
      this.encodingMode = encodingMode;
      this.embeddingMode = embeddingMode;
      this.simulateStyle = simulateStyle;
      this.embedAsType1 = embedAsType1;
      this.useSVG = useSVG;
      if (this.encodingMode == EncodingMode.AUTO) {
         this.encodingMode = EncodingMode.CID;
      }

      if (this.embeddingMode == EmbeddingMode.AUTO) {
         this.embeddingMode = EmbeddingMode.SUBSET;
      }

   }

   protected void read() throws IOException {
      this.read(this.subFontName);
   }

   private void read(String ttcFontName) throws IOException {
      InputStream in = this.resourceResolver.getResource(this.fontFileURI);

      try {
         FontFileReader reader = new FontFileReader(in);
         String header = readHeader(reader);
         boolean isCFF = header.equals("OTTO");
         OpenFont otf = isCFF ? new OTFFile(this.useKerning, this.useAdvanced) : new TTFFile(this.useKerning, this.useAdvanced);
         boolean supported = ((OpenFont)otf).readFont(reader, header, ttcFontName);
         if (!supported) {
            throw new IOException("The font does not have a Unicode cmap table: " + this.fontFileURI);
         }

         this.buildFont((OpenFont)otf, ttcFontName);
         this.loaded = true;
      } finally {
         IOUtils.closeQuietly((InputStream)in);
      }

   }

   public static String readHeader(FontFileReader fontFile) throws IOException {
      if (fontFile != null) {
         fontFile.seekSet(0L);
         return fontFile.readTTFString(4);
      } else {
         return null;
      }
   }

   private void buildFont(OpenFont otf, String ttcFontName) {
      boolean isCid = this.embedded;
      if (this.encodingMode == EncodingMode.SINGLE_BYTE) {
         isCid = false;
      }

      Object font;
      if (isCid) {
         if (otf instanceof OTFFile && this.embedAsType1) {
            this.multiFont = new CFFToType1Font(this.resourceResolver, this.embeddingMode);
         } else {
            this.multiFont = new MultiByteFont(this.resourceResolver, this.embeddingMode);
         }

         this.multiFont.setIsOTFFile(otf instanceof OTFFile);
         this.returnFont = this.multiFont;
         this.multiFont.setTTCName(ttcFontName);
         font = this.multiFont;
      } else {
         this.singleFont = new SingleByteFont(this.resourceResolver, this.embeddingMode);
         this.returnFont = this.singleFont;
         font = this.singleFont;
      }

      ((CustomFont)font).setSimulateStyle(this.simulateStyle);
      this.returnFont.setFontURI(this.fontFileURI);
      if (!otf.getEmbedFontName().equals("")) {
         this.returnFont.setFontName(otf.getEmbedFontName());
      } else {
         this.returnFont.setFontName(otf.getPostScriptName());
      }

      this.returnFont.setFullName(otf.getFullName());
      this.returnFont.setFamilyNames(otf.getFamilyNames());
      this.returnFont.setFontSubFamilyName(otf.getSubFamilyName());
      this.returnFont.setCapHeight(otf.getCapHeight());
      this.returnFont.setXHeight(otf.getXHeight());
      this.returnFont.setAscender(otf.getLowerCaseAscent());
      this.returnFont.setDescender(otf.getLowerCaseDescent());
      this.returnFont.setFontBBox(otf.getFontBBox());
      this.returnFont.setUnderlinePosition(otf.getUnderlinePosition() - otf.getUnderlineThickness() / 2);
      this.returnFont.setUnderlineThickness(otf.getUnderlineThickness());
      this.returnFont.setStrikeoutPosition(otf.getStrikeoutPosition() - otf.getStrikeoutThickness() / 2);
      this.returnFont.setStrikeoutThickness(otf.getStrikeoutThickness());
      this.returnFont.setFlags(otf.getFlags());
      this.returnFont.setStemV(Integer.parseInt(otf.getStemV()));
      this.returnFont.setItalicAngle(Integer.parseInt(otf.getItalicAngle()));
      this.returnFont.setMissingWidth(0);
      this.returnFont.setWeight(otf.getWeightClass());
      if (isCid) {
         if (otf instanceof OTFFile) {
            if (((OTFFile)otf).isType1() && this.embeddingMode == EmbeddingMode.SUBSET && !this.embedAsType1) {
               this.multiFont.setFontType(FontType.TYPE1C);
               this.copyGlyphMetricsSingleByte(otf);
            }

            this.multiFont.setCIDType(CIDFontType.CIDTYPE0);
         } else {
            this.multiFont.setCIDType(CIDFontType.CIDTYPE2);
         }

         this.multiFont.setWidthArray(otf.getWidths());
         this.multiFont.setBBoxArray(otf.getBoundingBoxes());
      } else {
         this.singleFont.setFontType(FontType.TRUETYPE);
         this.singleFont.setEncoding(otf.getCharSetName());
         this.returnFont.setFirstChar(otf.getFirstChar());
         this.returnFont.setLastChar(otf.getLastChar());
         this.singleFont.setTrueTypePostScriptVersion(otf.getPostScriptVersion());
         this.copyGlyphMetricsSingleByte(otf);
      }

      this.returnFont.setCMap(this.getCMap(otf));
      if (this.useSVG) {
         this.returnFont.setSVG(otf.svgs);
      }

      if (otf.getKerning() != null && this.useKerning) {
         this.copyKerning(otf, isCid);
      }

      if (this.useAdvanced) {
         this.copyAdvanced(otf);
      }

      if (this.embedded) {
         if (!otf.isEmbeddable()) {
            String msg = "The font " + this.fontFileURI + " is not embeddable due to a licensing restriction.";
            throw new RuntimeException(msg);
         }

         this.returnFont.setEmbedURI(this.fontFileURI);
      }

   }

   private CMapSegment[] getCMap(OpenFont otf) {
      CMapSegment[] array = new CMapSegment[otf.getCMaps().size()];
      return (CMapSegment[])otf.getCMaps().toArray(array);
   }

   private void copyGlyphMetricsSingleByte(OpenFont otf) {
      int[] wx = otf.getWidths();
      Rectangle[] bboxes = otf.getBoundingBoxes();
      if (this.singleFont != null) {
         for(int i = this.singleFont.getFirstChar(); i <= this.singleFont.getLastChar(); ++i) {
            this.singleFont.setWidth(i, otf.getCharWidth(i));
            int[] bbox = otf.getBBox(i);
            this.singleFont.setBoundingBox(i, new Rectangle(bbox[0], bbox[1], bbox[2] - bbox[0], bbox[3] - bbox[1]));
         }
      }

      Iterator var12 = otf.getCMaps().iterator();

      while(true) {
         CMapSegment segment;
         do {
            if (!var12.hasNext()) {
               return;
            }

            segment = (CMapSegment)var12.next();
         } while(segment.getUnicodeStart() >= 65534);

         for(char u = (char)segment.getUnicodeStart(); u <= segment.getUnicodeEnd(); ++u) {
            int codePoint = 0;
            if (this.singleFont != null) {
               codePoint = this.singleFont.getEncoding().mapChar(u);
            }

            if (codePoint <= 0) {
               int glyphIndex = segment.getGlyphStartIndex() + u - segment.getUnicodeStart();
               String glyphName = otf.getGlyphName(glyphIndex);
               if (glyphName.length() == 0 && otf.getPostScriptVersion() != OpenFont.PostScriptVersion.V2) {
                  glyphName = "u" + HexEncoder.encode(u);
               }

               if (glyphName.length() > 0) {
                  String unicode = Character.toString(u);
                  NamedCharacter nc = new NamedCharacter(glyphName, unicode);
                  this.returnFont.addUnencodedCharacter(nc, wx[glyphIndex], bboxes[glyphIndex]);
               }
            }
         }
      }
   }

   private void copyKerning(OpenFont otf, boolean isCid) {
      Set kerningSet;
      if (isCid) {
         kerningSet = otf.getKerning().keySet();
      } else {
         kerningSet = otf.getAnsiKerning().keySet();
      }

      Integer kpx1;
      Map h2;
      for(Iterator var4 = kerningSet.iterator(); var4.hasNext(); this.returnFont.putKerningEntry(kpx1, h2)) {
         kpx1 = (Integer)var4.next();
         if (isCid) {
            h2 = (Map)otf.getKerning().get(kpx1);
         } else {
            h2 = (Map)otf.getAnsiKerning().get(kpx1);
         }
      }

   }

   private void copyAdvanced(OpenFont otf) {
      if (this.returnFont instanceof MultiByteFont) {
         MultiByteFont mbf = (MultiByteFont)this.returnFont;
         mbf.setGDEF(otf.getGDEF());
         mbf.setGSUB(otf.getGSUB());
         mbf.setGPOS(otf.getGPOS());
      }

   }
}
