package org.apache.fop.render.pcl.fonts;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OpenFont;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;

public class PCLSoftFont {
   private int fontID;
   private Typeface font;
   private Map charOffsets;
   private OpenFont openFont;
   private InputStream fontStream;
   private FontFileReader reader;
   private Map charsWritten;
   private Map mappedChars;
   private Map charMtxPositions;
   private boolean multiByteFont;
   private int charCount = 32;

   public PCLSoftFont(int fontID, Typeface font, boolean multiByteFont) {
      this.fontID = fontID;
      this.font = font;
      this.charsWritten = new HashMap();
      this.mappedChars = new HashMap();
      this.multiByteFont = multiByteFont;
   }

   public Typeface getTypeface() {
      return this.font;
   }

   public int getFontID() {
      return this.fontID;
   }

   public void setCharacterOffsets(Map charOffsets) {
      this.charOffsets = charOffsets;
   }

   public Map getCharacterOffsets() {
      return this.charOffsets;
   }

   public OpenFont getOpenFont() {
      return this.openFont;
   }

   public void setOpenFont(OpenFont openFont) {
      this.openFont = openFont;
   }

   public InputStream getFontStream() {
      return this.fontStream;
   }

   public void setFontStream(InputStream fontStream) {
      this.fontStream = fontStream;
   }

   public FontFileReader getReader() {
      return this.reader;
   }

   public void setReader(FontFileReader reader) {
      this.reader = reader;
   }

   public void writeCharacter(int unicode) {
      this.charsWritten.put(unicode, this.charCount++);
   }

   public int getUnicodeCodePoint(int unicode) {
      return this.charsWritten.containsKey(unicode) ? (Integer)this.charsWritten.get(unicode) : -1;
   }

   public boolean hasPreviouslyWritten(int unicode) {
      return this.charsWritten.containsKey(unicode);
   }

   public int getMtxCharIndex(int unicode) {
      return this.charMtxPositions.get(unicode) != null ? (Integer)this.charMtxPositions.get(unicode) : 0;
   }

   public int getCmapGlyphIndex(int unicode) {
      if (this.font instanceof CustomFontMetricsMapper) {
         CustomFontMetricsMapper customFont = (CustomFontMetricsMapper)this.font;
         Typeface realFont = customFont.getRealFont();
         if (realFont instanceof MultiByteFont) {
            MultiByteFont mbFont = (MultiByteFont)realFont;
            return mbFont.findGlyphIndex(unicode);
         }
      }

      return 0;
   }

   public void setMtxCharIndexes(Map charMtxPositions) {
      this.charMtxPositions = charMtxPositions;
   }

   public int getCharCount() {
      return this.charCount;
   }

   public void setMappedChars(Map mappedChars) {
      this.mappedChars = mappedChars;
   }

   public Map getMappedChars() {
      return this.mappedChars;
   }

   public int getCharIndex(char ch) {
      return this.mappedChars.containsKey(ch) ? (Integer)this.mappedChars.get(ch) : -1;
   }

   public int getCharCode(char ch) {
      return this.multiByteFont ? this.getCharIndex(ch) : this.getUnicodeCodePoint(ch);
   }
}
