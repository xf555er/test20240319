package org.apache.fop.fonts;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.truetype.OpenFont;

public class SingleByteFont extends CustomFont {
   private static Log log = LogFactory.getLog(SingleByteFont.class);
   protected SingleByteEncoding mapping;
   private boolean useNativeEncoding;
   protected int[] width;
   private Rectangle[] boundingBoxes;
   private Map alternativeCodes;
   private OpenFont.PostScriptVersion ttPostScriptVersion;
   private int usedGlyphsCount;
   private LinkedHashMap usedGlyphNames;
   private Map usedGlyphs;
   private Map usedCharsIndex;
   private Map charGIDMappings;

   public SingleByteFont(InternalResourceResolver resourceResolver) {
      super(resourceResolver);
      this.setEncoding("WinAnsiEncoding");
   }

   public SingleByteFont(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
      this(resourceResolver);
      this.setEmbeddingMode(embeddingMode);
      if (embeddingMode != EmbeddingMode.FULL) {
         this.usedGlyphNames = new LinkedHashMap();
         this.usedGlyphs = new HashMap();
         this.usedCharsIndex = new HashMap();
         this.charGIDMappings = new HashMap();
         this.usedGlyphs.put(0, 0);
         ++this.usedGlyphsCount;
      }

   }

   public boolean isEmbeddable() {
      return this.getEmbedFileURI() != null || this.getEmbedResourceName() != null;
   }

   public boolean isSubsetEmbedded() {
      return this.getEmbeddingMode() != EmbeddingMode.FULL;
   }

   public String getEncodingName() {
      return this.mapping.getName();
   }

   public SingleByteEncoding getEncoding() {
      return this.mapping;
   }

   public int getWidth(int i, int size) {
      int idx;
      if (i < 256) {
         idx = i - this.getFirstChar();
         if (idx >= 0 && idx < this.width.length) {
            return size * this.width[idx];
         }
      } else if (this.additionalEncodings != null) {
         idx = i / 256 - 1;
         SimpleSingleByteEncoding encoding = this.getAdditionalEncoding(idx);
         int codePoint = i % 256;
         NamedCharacter nc = encoding.getCharacterForIndex(codePoint);
         UnencodedCharacter uc = (UnencodedCharacter)this.unencodedCharacters.get(nc.getSingleUnicodeValue());
         return size * uc.getWidth();
      }

      return 0;
   }

   public int[] getWidths() {
      int[] arr = new int[this.width.length];
      System.arraycopy(this.width, 0, arr, 0, this.width.length);
      return arr;
   }

   public Rectangle getBoundingBox(int glyphIndex, int size) {
      Rectangle bbox = null;
      int idx;
      if (glyphIndex < 256) {
         idx = glyphIndex - this.getFirstChar();
         if (idx >= 0 && idx < this.boundingBoxes.length) {
            bbox = this.boundingBoxes[idx];
         }
      } else if (this.additionalEncodings != null) {
         idx = glyphIndex / 256 - 1;
         SimpleSingleByteEncoding encoding = this.getAdditionalEncoding(idx);
         int codePoint = glyphIndex % 256;
         NamedCharacter nc = encoding.getCharacterForIndex(codePoint);
         UnencodedCharacter uc = (UnencodedCharacter)this.unencodedCharacters.get(nc.getSingleUnicodeValue());
         bbox = uc.getBBox();
      }

      return bbox == null ? null : new Rectangle(bbox.x * size, bbox.y * size, bbox.width * size, bbox.height * size);
   }

   private char findAlternative(char c) {
      if (this.alternativeCodes == null) {
         this.alternativeCodes = new HashMap();
      } else {
         Character alternative = (Character)this.alternativeCodes.get(c);
         if (alternative != null) {
            return alternative;
         }
      }

      String charName = org.apache.xmlgraphics.fonts.Glyphs.charToGlyphName(c);
      String[] charNameAlternatives = org.apache.xmlgraphics.fonts.Glyphs.getCharNameAlternativesFor(charName);
      if (charNameAlternatives != null && charNameAlternatives.length > 0) {
         String[] var5 = charNameAlternatives;
         int var6 = charNameAlternatives.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            String charNameAlternative = var5[var7];
            if (log.isDebugEnabled()) {
               log.debug("Checking alternative for char " + c + " (charname=" + charName + "): " + charNameAlternative);
            }

            String s = org.apache.xmlgraphics.fonts.Glyphs.getUnicodeSequenceForGlyphName(charNameAlternative);
            if (s != null) {
               char d = this.lookupChar(s.charAt(0));
               if (d != 0) {
                  this.alternativeCodes.put(c, d);
                  return d;
               }
            }
         }
      }

      return '\u0000';
   }

   private char lookupChar(char c) {
      char d = this.mapping.mapChar(c);
      if (d != 0) {
         return d;
      } else {
         d = this.mapUnencodedChar(c);
         return d;
      }
   }

   private boolean isSubset() {
      return this.getEmbeddingMode() == EmbeddingMode.SUBSET;
   }

   public char mapChar(char c) {
      this.notifyMapOperation();
      char d = this.lookupChar(c);
      if (d == 0) {
         d = this.findAlternative(c);
         if (d != 0) {
            return d;
         } else {
            this.warnMissingGlyph(c);
            return '#';
         }
      } else {
         if (this.isEmbeddable() && this.isSubset()) {
            this.mapChar(d, c);
         }

         return d;
      }
   }

   private int mapChar(int glyphIndex, char unicode) {
      Integer subsetCharSelector = (Integer)this.usedGlyphs.get(glyphIndex);
      if (subsetCharSelector == null) {
         int selector = this.usedGlyphsCount;
         this.usedGlyphs.put(glyphIndex, selector);
         this.usedCharsIndex.put(selector, unicode);
         this.charGIDMappings.put(unicode, glyphIndex);
         ++this.usedGlyphsCount;
         return selector;
      } else {
         return subsetCharSelector;
      }
   }

   private char getUnicode(int index) {
      Character mapValue = (Character)this.usedCharsIndex.get(index);
      return mapValue != null ? mapValue : '\uffff';
   }

   public boolean hasChar(char c) {
      char d = this.mapping.mapChar(c);
      if (d != 0) {
         return true;
      } else {
         d = this.mapUnencodedChar(c);
         if (d != 0) {
            return true;
         } else {
            d = this.findAlternative(c);
            return d != 0;
         }
      }
   }

   protected void updateMapping(String encoding) {
      try {
         this.mapping = CodePointMapping.getMapping(encoding);
      } catch (UnsupportedOperationException var3) {
         log.error("Font '" + super.getFontName() + "': " + var3.getMessage());
      }

   }

   public void setEncoding(String encoding) {
      this.updateMapping(encoding);
   }

   public void setEncoding(CodePointMapping encoding) {
      this.mapping = encoding;
   }

   public void setUseNativeEncoding(boolean value) {
      this.useNativeEncoding = value;
   }

   public boolean isUsingNativeEncoding() {
      return this.useNativeEncoding;
   }

   public void setWidth(int index, int w) {
      if (this.width == null) {
         this.width = new int[this.getLastChar() - this.getFirstChar() + 1];
      }

      this.width[index - this.getFirstChar()] = w;
   }

   public void setBoundingBox(int index, Rectangle bbox) {
      if (this.boundingBoxes == null) {
         this.boundingBoxes = new Rectangle[this.getLastChar() - this.getFirstChar() + 1];
      }

      this.boundingBoxes[index - this.getFirstChar()] = bbox;
   }

   public void addUnencodedCharacter(NamedCharacter ch, int width, Rectangle bbox) {
      if (this.unencodedCharacters == null) {
         this.unencodedCharacters = new HashMap();
      }

      if (ch.hasSingleUnicodeValue()) {
         UnencodedCharacter uc = new UnencodedCharacter(ch, width, bbox);
         this.unencodedCharacters.put(ch.getSingleUnicodeValue(), uc);
      }

   }

   public void encodeAllUnencodedCharacters() {
      if (this.unencodedCharacters != null) {
         Set sortedKeys = new TreeSet(this.unencodedCharacters.keySet());
         Iterator var2 = sortedKeys.iterator();

         while(var2.hasNext()) {
            Character ch = (Character)var2.next();
            char mapped = this.mapChar(ch);

            assert mapped != '#';
         }
      }

   }

   public int[] getAdditionalWidths(int index) {
      SimpleSingleByteEncoding enc = this.getAdditionalEncoding(index);
      int[] arr = new int[enc.getLastChar() - enc.getFirstChar() + 1];
      int i = 0;

      for(int c = arr.length; i < c; ++i) {
         NamedCharacter nc = enc.getCharacterForIndex(enc.getFirstChar() + i);
         UnencodedCharacter uc = (UnencodedCharacter)this.unencodedCharacters.get(nc.getSingleUnicodeValue());
         arr[i] = uc.getWidth();
      }

      return arr;
   }

   public void setTrueTypePostScriptVersion(OpenFont.PostScriptVersion version) {
      this.ttPostScriptVersion = version;
   }

   public OpenFont.PostScriptVersion getTrueTypePostScriptVersion() {
      assert this.getFontType() == FontType.TRUETYPE;

      return this.ttPostScriptVersion;
   }

   public Map getUsedGlyphs() {
      return Collections.unmodifiableMap(this.usedGlyphs);
   }

   public char getUnicodeFromSelector(int selector) {
      return this.getUnicode(selector);
   }

   public int getGIDFromChar(char ch) {
      return (Integer)this.charGIDMappings.get(ch);
   }

   public char getUnicodeFromGID(int glyphIndex) {
      int selector = (Integer)this.usedGlyphs.get(glyphIndex);
      return (Character)this.usedCharsIndex.get(selector);
   }

   public void mapUsedGlyphName(int gid, String value) {
      this.usedGlyphNames.put(gid, value);
   }

   public Map getUsedGlyphNames() {
      return this.usedGlyphNames;
   }

   public String getGlyphName(int idx) {
      if (idx < this.mapping.getCharNameMap().length) {
         return this.mapping.getCharNameMap()[idx];
      } else {
         int selector = (Integer)this.usedGlyphs.get(idx);
         char theChar = (Character)this.usedCharsIndex.get(selector);
         return ((UnencodedCharacter)this.unencodedCharacters.get(theChar)).getCharacter().getName();
      }
   }

   protected static final class UnencodedCharacter {
      private final NamedCharacter character;
      private final int width;
      private final Rectangle bbox;

      public UnencodedCharacter(NamedCharacter character, int width, Rectangle bbox) {
         this.character = character;
         this.width = width;
         this.bbox = bbox;
      }

      public NamedCharacter getCharacter() {
         return this.character;
      }

      public int getWidth() {
         return this.width;
      }

      public Rectangle getBBox() {
         return this.bbox;
      }

      public String toString() {
         return this.getCharacter().toString();
      }
   }
}
