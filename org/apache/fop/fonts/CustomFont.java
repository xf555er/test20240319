package org.apache.fop.fonts;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fop.apps.io.InternalResourceResolver;

public abstract class CustomFont extends Typeface implements FontDescriptor, MutableFont {
   private static final int DEFAULT_LINE_THICKNESS = 50;
   private URI fontFileURI;
   private String fontName;
   private String fullName;
   private Set familyNames;
   private String fontSubName;
   private URI embedFileURI;
   private String embedResourceName;
   private final InternalResourceResolver resourceResolver;
   private EmbeddingMode embeddingMode;
   private int capHeight;
   private int xHeight;
   private int ascender;
   private int descender;
   private int[] fontBBox;
   private int flags;
   private int weight;
   private int stemV;
   private int italicAngle;
   private int missingWidth;
   private FontType fontType;
   private int firstChar;
   private int lastChar;
   private int underlinePosition;
   private int underlineThickness;
   private int strikeoutPosition;
   private int strikeoutThickness;
   private Map kerning;
   private boolean useKerning;
   protected List cmap;
   protected Map svgs;
   private boolean useAdvanced;
   private boolean simulateStyle;
   protected List additionalEncodings;
   protected Map unencodedCharacters;

   public CustomFont(InternalResourceResolver resourceResolver) {
      this.embeddingMode = EmbeddingMode.AUTO;
      this.fontBBox = new int[]{0, 0, 0, 0};
      this.flags = 4;
      this.fontType = FontType.TYPE1;
      this.lastChar = 255;
      this.useKerning = true;
      this.cmap = new ArrayList();
      this.useAdvanced = true;
      this.resourceResolver = resourceResolver;
   }

   public URI getFontURI() {
      return this.fontFileURI;
   }

   public String getFontName() {
      return this.fontName;
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return this.fullName;
   }

   public Set getFamilyNames() {
      return Collections.unmodifiableSet(this.familyNames);
   }

   public String getStrippedFontName() {
      return FontUtil.stripWhiteSpace(this.getFontName());
   }

   public String getFontSubName() {
      return this.fontSubName;
   }

   public URI getEmbedFileURI() {
      return this.embedFileURI;
   }

   public EmbeddingMode getEmbeddingMode() {
      return this.embeddingMode;
   }

   public InputStream getInputStream() throws IOException {
      return this.resourceResolver.getResource(this.embedFileURI);
   }

   public String getEmbedResourceName() {
      return this.embedResourceName;
   }

   public int getAscender() {
      return this.ascender;
   }

   public int getDescender() {
      return this.descender;
   }

   public int getCapHeight() {
      return this.capHeight;
   }

   public int getAscender(int size) {
      return size * this.ascender;
   }

   public int getDescender(int size) {
      return size * this.descender;
   }

   public int getCapHeight(int size) {
      return size * this.capHeight;
   }

   public int getXHeight(int size) {
      return size * this.xHeight;
   }

   public int[] getFontBBox() {
      return this.fontBBox;
   }

   public int getFlags() {
      return this.flags;
   }

   public boolean isSymbolicFont() {
      return (this.getFlags() & 4) != 0 || "ZapfDingbatsEncoding".equals(this.getEncodingName());
   }

   public int getWeight() {
      return this.weight;
   }

   public int getStemV() {
      return this.stemV;
   }

   public int getItalicAngle() {
      return this.italicAngle;
   }

   public int getMissingWidth() {
      return this.missingWidth;
   }

   public FontType getFontType() {
      return this.fontType;
   }

   public int getFirstChar() {
      return this.firstChar;
   }

   public int getLastChar() {
      return this.lastChar;
   }

   public boolean isKerningEnabled() {
      return this.useKerning;
   }

   public final boolean hasKerningInfo() {
      return this.isKerningEnabled() && this.kerning != null && !this.kerning.isEmpty();
   }

   public final Map getKerningInfo() {
      return this.hasKerningInfo() ? this.kerning : Collections.emptyMap();
   }

   public boolean isAdvancedEnabled() {
      return this.useAdvanced;
   }

   public void setFontURI(URI uri) {
      this.fontFileURI = uri;
   }

   public void setFontName(String name) {
      this.fontName = name;
   }

   public void setFullName(String name) {
      this.fullName = name;
   }

   public void setFamilyNames(Set names) {
      this.familyNames = new HashSet(names);
   }

   public void setFontSubFamilyName(String subFamilyName) {
      this.fontSubName = subFamilyName;
   }

   public void setEmbedURI(URI path) {
      this.embedFileURI = path;
   }

   public void setEmbedResourceName(String name) {
      this.embedResourceName = name;
   }

   public void setEmbeddingMode(EmbeddingMode embeddingMode) {
      this.embeddingMode = embeddingMode;
   }

   public void setCapHeight(int capHeight) {
      this.capHeight = capHeight;
   }

   public void setXHeight(int xHeight) {
      this.xHeight = xHeight;
   }

   public void setAscender(int ascender) {
      this.ascender = ascender;
   }

   public void setDescender(int descender) {
      this.descender = descender;
   }

   public void setFontBBox(int[] bbox) {
      this.fontBBox = bbox;
   }

   public void setFlags(int flags) {
      this.flags = flags;
   }

   public void setWeight(int weight) {
      weight = weight / 100 * 100;
      weight = Math.max(100, weight);
      weight = Math.min(900, weight);
      this.weight = weight;
   }

   public void setStemV(int stemV) {
      this.stemV = stemV;
   }

   public void setItalicAngle(int italicAngle) {
      this.italicAngle = italicAngle;
   }

   public void setMissingWidth(int width) {
      this.missingWidth = width;
   }

   public void setFontType(FontType fontType) {
      this.fontType = fontType;
   }

   public void setFirstChar(int index) {
      this.firstChar = index;
   }

   public void setLastChar(int index) {
      this.lastChar = index;
   }

   public void setKerningEnabled(boolean enabled) {
      this.useKerning = enabled;
   }

   public void setAdvancedEnabled(boolean enabled) {
      this.useAdvanced = enabled;
   }

   public void setSimulateStyle(boolean enabled) {
      this.simulateStyle = enabled;
   }

   public boolean getSimulateStyle() {
      return this.simulateStyle;
   }

   public void putKerningEntry(Integer key, Map value) {
      if (this.kerning == null) {
         this.kerning = new HashMap();
      }

      this.kerning.put(key, value);
   }

   public void replaceKerningMap(Map kerningMap) {
      if (kerningMap == null) {
         this.kerning = Collections.emptyMap();
      } else {
         this.kerning = kerningMap;
      }

   }

   public void setCMap(CMapSegment[] cmap) {
      this.cmap.clear();
      Collections.addAll(this.cmap, cmap);
   }

   public CMapSegment[] getCMap() {
      return (CMapSegment[])this.cmap.toArray(new CMapSegment[this.cmap.size()]);
   }

   public int getUnderlinePosition(int size) {
      return this.underlinePosition == 0 ? this.getDescender(size) / 2 : size * this.underlinePosition;
   }

   public void setUnderlinePosition(int underlinePosition) {
      this.underlinePosition = underlinePosition;
   }

   public int getUnderlineThickness(int size) {
      return size * (this.underlineThickness == 0 ? 50 : this.underlineThickness);
   }

   public void setUnderlineThickness(int underlineThickness) {
      this.underlineThickness = underlineThickness;
   }

   public int getStrikeoutPosition(int size) {
      return this.strikeoutPosition == 0 ? this.getXHeight(size) / 2 : size * this.strikeoutPosition;
   }

   public void setStrikeoutPosition(int strikeoutPosition) {
      this.strikeoutPosition = strikeoutPosition;
   }

   public int getStrikeoutThickness(int size) {
      return this.strikeoutThickness == 0 ? this.getUnderlineThickness(size) : size * this.strikeoutThickness;
   }

   public void setStrikeoutThickness(int strikeoutThickness) {
      this.strikeoutThickness = strikeoutThickness;
   }

   public abstract Map getUsedGlyphs();

   public abstract char getUnicodeFromGID(int var1);

   public boolean hasAdditionalEncodings() {
      return this.additionalEncodings != null && this.additionalEncodings.size() > 0;
   }

   public int getAdditionalEncodingCount() {
      return this.hasAdditionalEncodings() ? this.additionalEncodings.size() : 0;
   }

   public SimpleSingleByteEncoding getAdditionalEncoding(int index) throws IndexOutOfBoundsException {
      if (this.hasAdditionalEncodings()) {
         return (SimpleSingleByteEncoding)this.additionalEncodings.get(index);
      } else {
         throw new IndexOutOfBoundsException("No additional encodings available");
      }
   }

   public void addUnencodedCharacter(NamedCharacter ch, int width, Rectangle bbox) {
      if (this.unencodedCharacters == null) {
         this.unencodedCharacters = new HashMap();
      }

      if (ch.hasSingleUnicodeValue()) {
         SingleByteFont.UnencodedCharacter uc = new SingleByteFont.UnencodedCharacter(ch, width, bbox);
         this.unencodedCharacters.put(ch.getSingleUnicodeValue(), uc);
      }

   }

   protected char mapUnencodedChar(char ch) {
      if (this.unencodedCharacters != null) {
         SingleByteFont.UnencodedCharacter unencoded = (SingleByteFont.UnencodedCharacter)this.unencodedCharacters.get(ch);
         if (unencoded != null) {
            if (this.additionalEncodings == null) {
               this.additionalEncodings = new ArrayList();
            }

            SimpleSingleByteEncoding encoding = null;
            char mappedStart = 0;
            int additionalsCount = this.additionalEncodings.size();

            for(int i = 0; i < additionalsCount; ++i) {
               mappedStart = (char)(mappedStart + 256);
               encoding = this.getAdditionalEncoding(i);
               char alt = encoding.mapChar(ch);
               if (alt != 0) {
                  return (char)(mappedStart + alt);
               }
            }

            if (encoding != null && encoding.isFull()) {
               encoding = null;
            }

            if (encoding == null) {
               encoding = new SimpleSingleByteEncoding(this.getFontName() + "EncodingSupp" + (additionalsCount + 1));
               this.additionalEncodings.add(encoding);
               mappedStart = (char)(mappedStart + 256);
            }

            return (char)(mappedStart + encoding.addCharacter(unencoded.getCharacter()));
         }
      }

      return '\u0000';
   }

   public boolean hasSVG() {
      return this.svgs != null;
   }

   public void setSVG(Map svgs) {
      this.svgs = svgs;
   }
}
