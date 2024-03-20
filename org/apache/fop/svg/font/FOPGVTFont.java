package org.apache.fop.svg.font;

import java.awt.font.FontRenderContext;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import org.apache.batik.gvt.font.GVTFont;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.GVTGlyphVector;
import org.apache.batik.gvt.font.GVTLineMetrics;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.FontTriplet;

public class FOPGVTFont implements GVTFont {
   private final Font font;
   private final GVTFontFamily fontFamily;

   public FOPGVTFont(Font font, GVTFontFamily fontFamily) {
      this.font = font;
      this.fontFamily = fontFamily;
   }

   public Font getFont() {
      return this.font;
   }

   public boolean canDisplay(char c) {
      return this.font.hasChar(c);
   }

   public int canDisplayUpTo(char[] text, int start, int limit) {
      for(int i = start; i < limit; ++i) {
         if (!this.canDisplay(text[i])) {
            return i;
         }
      }

      return -1;
   }

   public int canDisplayUpTo(CharacterIterator iter, int start, int limit) {
      for(char c = iter.setIndex(start); iter.getIndex() < limit; c = iter.next()) {
         if (!this.canDisplay(c)) {
            return iter.getIndex();
         }
      }

      return -1;
   }

   public int canDisplayUpTo(String str) {
      for(int i = 0; i < str.length(); ++i) {
         if (!this.canDisplay(str.charAt(i))) {
            return i;
         }
      }

      return -1;
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, char[] chars) {
      return this.createGlyphVector(frc, new String(chars));
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, CharacterIterator ci) {
      return (GVTGlyphVector)(!this.font.performsSubstitution() && !this.font.performsPositioning() ? new FOPGVTGlyphVector(this, ci, frc) : new ComplexGlyphVector(this, ci, frc));
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, int[] glyphCodes, CharacterIterator ci) {
      throw new UnsupportedOperationException("Not implemented");
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, String text) {
      StringCharacterIterator sci = new StringCharacterIterator(text);
      return this.createGlyphVector(frc, (CharacterIterator)sci);
   }

   public GVTGlyphVector createGlyphVector(FontRenderContext frc, String text, String script, String language) {
      if (script == null && language == null) {
         return this.createGlyphVector(frc, text);
      } else {
         AttributedString as = new AttributedString(text);
         if (script != null) {
            as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.SCRIPT, script);
         }

         if (language != null) {
            as.addAttribute(GVTAttributedCharacterIterator.TextAttribute.LANGUAGE, language);
         }

         return this.createGlyphVector(frc, (CharacterIterator)as.getIterator());
      }
   }

   public FOPGVTFont deriveFont(float size) {
      throw new UnsupportedOperationException("Not implemented");
   }

   public FontInfo getFontInfo() {
      return ((FOPGVTFontFamily)this.fontFamily).getFontInfo();
   }

   public String getFontKey() {
      return this.font.getFontName();
   }

   public FontTriplet getFontTriplet() {
      return this.font.getFontTriplet();
   }

   public String getFamilyName() {
      return this.fontFamily.getFamilyName();
   }

   public GVTLineMetrics getLineMetrics(char[] chars, int beginIndex, int limit, FontRenderContext frc) {
      return this.getLineMetrics(limit - beginIndex);
   }

   GVTLineMetrics getLineMetrics(int numChars) {
      numChars = numChars < 0 ? 0 : numChars;
      FontMetrics metrics = this.font.getFontMetrics();
      int size = this.font.getFontSize();
      return new GVTLineMetrics((float)metrics.getCapHeight(size) / 1000000.0F, 0, (float[])null, (float)(-metrics.getDescender(size)) / 1000000.0F, 0.0F, 0.0F, numChars, (float)(-metrics.getStrikeoutPosition(size)) / 1000000.0F, (float)metrics.getStrikeoutThickness(size) / 1000000.0F, (float)(-metrics.getUnderlinePosition(size)) / 1000000.0F, (float)metrics.getUnderlineThickness(size) / 1000000.0F, (float)(-metrics.getCapHeight(size)) / 1000000.0F, (float)metrics.getUnderlineThickness(size) / 1000000.0F);
   }

   public GVTLineMetrics getLineMetrics(CharacterIterator ci, int beginIndex, int limit, FontRenderContext frc) {
      return this.getLineMetrics(limit - beginIndex);
   }

   public GVTLineMetrics getLineMetrics(String str, FontRenderContext frc) {
      return this.getLineMetrics(str.length());
   }

   public GVTLineMetrics getLineMetrics(String str, int beginIndex, int limit, FontRenderContext frc) {
      return this.getLineMetrics(limit - beginIndex);
   }

   public float getSize() {
      return (float)this.font.getFontSize() / 1000.0F;
   }

   public float getVKern(int glyphCode1, int glyphCode2) {
      return 0.0F;
   }

   public float getHKern(int glyphCode1, int glyphCode2) {
      return 0.0F;
   }
}
