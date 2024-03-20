package org.apache.fop.fonts;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.complexscripts.fonts.Positionable;
import org.apache.fop.complexscripts.fonts.Substitutable;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.util.CharUtilities;

public class Font implements Substitutable, Positionable {
   public static final int WEIGHT_EXTRA_BOLD = 800;
   public static final int WEIGHT_BOLD = 700;
   public static final int WEIGHT_NORMAL = 400;
   public static final int WEIGHT_LIGHT = 200;
   public static final String STYLE_NORMAL = "normal";
   public static final String STYLE_ITALIC = "italic";
   public static final String STYLE_OBLIQUE = "oblique";
   public static final String STYLE_INCLINED = "inclined";
   public static final int PRIORITY_DEFAULT = 0;
   public static final FontTriplet DEFAULT_FONT = new FontTriplet("any", "normal", 400, 0);
   private static Log log = LogFactory.getLog(Font.class);
   private final String fontName;
   private final FontTriplet triplet;
   private final int fontSize;
   private final FontMetrics metric;

   public Font(String key, FontTriplet triplet, FontMetrics met, int fontSize) {
      this.fontName = key;
      this.triplet = triplet;
      this.metric = met;
      this.fontSize = fontSize;
   }

   public FontMetrics getFontMetrics() {
      return this.metric;
   }

   public boolean isMultiByte() {
      return this.getFontMetrics().isMultiByte();
   }

   public int getAscender() {
      return this.metric.getAscender(this.fontSize) / 1000;
   }

   public int getCapHeight() {
      return this.metric.getCapHeight(this.fontSize) / 1000;
   }

   public int getDescender() {
      return this.metric.getDescender(this.fontSize) / 1000;
   }

   public String getFontName() {
      return this.fontName;
   }

   public FontTriplet getFontTriplet() {
      return this.triplet;
   }

   public int getFontSize() {
      return this.fontSize;
   }

   public int getXHeight() {
      return this.metric.getXHeight(this.fontSize) / 1000;
   }

   public boolean hasKerning() {
      return this.metric.hasKerningInfo();
   }

   public boolean hasFeature(int tableType, String script, String language, String feature) {
      return this.metric.hasFeature(tableType, script, language, feature);
   }

   public Map getKerning() {
      return this.metric.hasKerningInfo() ? this.metric.getKerningInfo() : Collections.emptyMap();
   }

   public int getKernValue(int ch1, int ch2) {
      if (ch1 >= 55296 && ch1 <= 57344) {
         return 0;
      } else if (ch2 >= 55296 && ch2 <= 57344) {
         return 0;
      } else {
         Map kernPair = (Map)this.getKerning().get(ch1);
         if (kernPair != null) {
            Integer width = (Integer)kernPair.get(ch2);
            if (width != null) {
               return width * this.getFontSize() / 1000;
            }
         }

         return 0;
      }
   }

   public int getWidth(int charnum) {
      return this.metric.getWidth(charnum, this.fontSize) / 1000;
   }

   public char mapChar(char c) {
      if (this.metric instanceof Typeface) {
         return ((Typeface)this.metric).mapChar(c);
      } else {
         char d = CodePointMapping.getMapping("WinAnsiEncoding").mapChar(c);
         if (d != 0) {
            c = d;
         } else {
            log.warn("Glyph " + c + " not available in font " + this.fontName);
            c = '#';
         }

         return c;
      }
   }

   public int mapCodePoint(int cp) {
      FontMetrics fontMetrics = this.getRealFontMetrics();
      if (fontMetrics instanceof CIDFont) {
         return ((CIDFont)fontMetrics).mapCodePoint(cp);
      } else {
         return CharUtilities.isBmpCodePoint(cp) ? this.mapChar((char)cp) : 35;
      }
   }

   public boolean hasChar(char c) {
      if (this.metric instanceof Typeface) {
         return ((Typeface)this.metric).hasChar(c);
      } else {
         return CodePointMapping.getMapping("WinAnsiEncoding").mapChar(c) > 0;
      }
   }

   public boolean hasCodePoint(int cp) {
      FontMetrics realFont = this.getRealFontMetrics();
      if (realFont instanceof CIDFont) {
         return ((CIDFont)realFont).hasCodePoint(cp);
      } else {
         return CharUtilities.isBmpCodePoint(cp) ? this.hasChar((char)cp) : false;
      }
   }

   private FontMetrics getRealFontMetrics() {
      FontMetrics realFontMetrics = this.metric;
      if (realFontMetrics instanceof CustomFontMetricsMapper) {
         realFontMetrics = ((CustomFontMetricsMapper)realFontMetrics).getRealFont();
      }

      return (FontMetrics)(realFontMetrics instanceof LazyFont ? ((LazyFont)realFontMetrics).getRealFont() : realFontMetrics);
   }

   public String toString() {
      StringBuffer sbuf = new StringBuffer(super.toString());
      sbuf.append('{');
      sbuf.append(this.fontName);
      sbuf.append(',');
      sbuf.append(this.fontSize);
      sbuf.append('}');
      return sbuf.toString();
   }

   public int getCharWidth(char c) {
      int width;
      if (c != '\n' && c != '\r' && c != '\t' && c != 160) {
         int em;
         if (this.hasChar(c)) {
            em = this.mapChar(c);
            width = this.getWidth(em);
         } else {
            width = -1;
         }

         if (width <= 0) {
            em = this.getFontSize();
            int en = em / 2;
            if (c == ' ') {
               width = em;
            } else if (c == 8192) {
               width = en;
            } else if (c == 8193) {
               width = em;
            } else if (c == 8194) {
               width = em / 2;
            } else if (c == 8195) {
               width = this.getFontSize();
            } else if (c == 8196) {
               width = em / 3;
            } else if (c == 8197) {
               width = em / 4;
            } else if (c == 8198) {
               width = em / 6;
            } else if (c == 8199) {
               width = this.getCharWidth('0');
            } else if (c == 8200) {
               width = this.getCharWidth('.');
            } else if (c == 8201) {
               width = em / 5;
            } else if (c == 8202) {
               width = em / 10;
            } else if (c == 8203) {
               width = 0;
            } else if (c == 8239) {
               width = this.getCharWidth(' ') / 2;
            } else if (c == 8288) {
               width = 0;
            } else if (c == 12288) {
               width = this.getCharWidth(' ') * 2;
            } else if (c == '\ufeff') {
               width = 0;
            } else {
               width = this.getWidth(this.mapChar(c));
            }
         }
      } else {
         width = this.getCharWidth(' ');
      }

      return width;
   }

   public int getCharWidth(int c) {
      if (c < 65536) {
         return this.getCharWidth((char)c);
      } else if (this.hasCodePoint(c)) {
         int mappedChar = this.mapCodePoint(c);
         return this.getWidth(mappedChar);
      } else {
         return -1;
      }
   }

   public int getWordWidth(String word) {
      if (word == null) {
         return 0;
      } else {
         int wordLength = word.length();
         int width = 0;
         char[] characters = new char[wordLength];
         word.getChars(0, wordLength, characters, 0);

         for(int i = 0; i < wordLength; ++i) {
            width += this.getCharWidth(characters[i]);
         }

         return width;
      }
   }

   public boolean performsSubstitution() {
      if (this.metric instanceof Substitutable) {
         Substitutable s = (Substitutable)this.metric;
         return s.performsSubstitution();
      } else {
         return false;
      }
   }

   public CharSequence performSubstitution(CharSequence cs, String script, String language, List associations, boolean retainControls) {
      if (this.metric instanceof Substitutable) {
         Substitutable s = (Substitutable)this.metric;
         return s.performSubstitution(cs, script, language, associations, retainControls);
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa, String script, String language, List associations) {
      if (this.metric instanceof Substitutable) {
         Substitutable s = (Substitutable)this.metric;
         return s.reorderCombiningMarks(cs, gpa, script, language, associations);
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public boolean performsPositioning() {
      if (this.metric instanceof Positionable) {
         Positionable p = (Positionable)this.metric;
         return p.performsPositioning();
      } else {
         return false;
      }
   }

   public int[][] performPositioning(CharSequence cs, String script, String language, int fontSize) {
      if (this.metric instanceof Positionable) {
         Positionable p = (Positionable)this.metric;
         return p.performPositioning(cs, script, language, fontSize);
      } else {
         throw new UnsupportedOperationException();
      }
   }

   public int[][] performPositioning(CharSequence cs, String script, String language) {
      return this.performPositioning(cs, script, language, this.fontSize);
   }
}
