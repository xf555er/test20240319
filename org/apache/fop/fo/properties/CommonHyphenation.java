package org.apache.fop.fo.properties;

import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.fonts.Typeface;

public final class CommonHyphenation {
   private static final Log LOG = LogFactory.getLog(CommonHyphenation.class);
   private static final PropertyCache CACHE = new PropertyCache();
   private int hash;
   public final StringProperty language;
   public final StringProperty country;
   public final StringProperty script;
   public final EnumProperty hyphenate;
   public final OptionalCharacterProperty hyphenationCharacter;
   public final NumberProperty hyphenationPushCharacterCount;
   public final NumberProperty hyphenationRemainCharacterCount;
   private static final char HYPHEN_MINUS = '-';
   private static final char MINUS_SIGN = '−';

   private CommonHyphenation(StringProperty language, StringProperty country, StringProperty script, EnumProperty hyphenate, OptionalCharacterProperty hyphenationCharacter, NumberProperty hyphenationPushCharacterCount, NumberProperty hyphenationRemainCharacterCount) {
      this.language = language;
      this.country = country;
      this.script = script;
      this.hyphenate = hyphenate;
      this.hyphenationCharacter = hyphenationCharacter;
      this.hyphenationPushCharacterCount = hyphenationPushCharacterCount;
      this.hyphenationRemainCharacterCount = hyphenationRemainCharacterCount;
   }

   public static CommonHyphenation getInstance(PropertyList propertyList) throws PropertyException {
      StringProperty language = (StringProperty)propertyList.get(134);
      StringProperty country = (StringProperty)propertyList.get(81);
      StringProperty script = (StringProperty)propertyList.get(218);
      EnumProperty hyphenate = (EnumProperty)propertyList.get(116);
      OptionalCharacterProperty hyphenationCharacter = (OptionalCharacterProperty)propertyList.get(117);
      NumberProperty hyphenationPushCharacterCount = (NumberProperty)propertyList.get(120);
      NumberProperty hyphenationRemainCharacterCount = (NumberProperty)propertyList.get(121);
      CommonHyphenation instance = new CommonHyphenation(language, country, script, hyphenate, hyphenationCharacter, hyphenationPushCharacterCount, hyphenationRemainCharacterCount);
      return (CommonHyphenation)CACHE.fetch(instance);
   }

   public Character getHyphChar(Font font) {
      if (this.hyphenationCharacter.getObject() == null) {
         return null;
      } else {
         char hyphChar = this.hyphenationCharacter.getCharacter();
         if (font.hasChar(hyphChar)) {
            return hyphChar;
         } else {
            boolean warn = false;
            char effHyphChar;
            if (font.hasChar('-')) {
               effHyphChar = '-';
               warn = true;
            } else {
               FontMetrics metrics;
               Typeface typeface;
               if (font.hasChar('−')) {
                  effHyphChar = 8722;
                  metrics = font.getFontMetrics();
                  if (metrics instanceof Typeface) {
                     typeface = (Typeface)metrics;
                     if (!"SymbolEncoding".equals(typeface.getEncodingName())) {
                        warn = true;
                     }
                  }
               } else {
                  effHyphChar = ' ';
                  metrics = font.getFontMetrics();
                  if (metrics instanceof Typeface) {
                     typeface = (Typeface)metrics;
                     if (!"ZapfDingbatsEncoding".equals(typeface.getEncodingName())) {
                        warn = true;
                     }
                  }
               }
            }

            if (warn) {
               LOG.warn("Substituted specified hyphenation character (0x" + Integer.toHexString(hyphChar) + ") with 0x" + Integer.toHexString(effHyphChar) + " because the font doesn't have the specified hyphenation character: " + font.getFontTriplet());
            }

            return effHyphChar;
         }
      }
   }

   public int getHyphIPD(Font font) {
      Character hyphChar = this.getHyphChar(font);
      return hyphChar == null ? 0 : font.getCharWidth(hyphChar);
   }

   public Locale getLocale() {
      return toLocale(this.language.getString(), this.country.getString());
   }

   public static Locale toLocale(String language, String country) {
      Locale locale = null;
      if (isDefined(language)) {
         if (isDefined(country)) {
            locale = new Locale(language, country);
         } else {
            locale = new Locale(language);
         }
      }

      return locale;
   }

   private static boolean isDefined(String property) {
      return property != null && !property.equalsIgnoreCase("none");
   }

   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof CommonHyphenation)) {
         return false;
      } else {
         CommonHyphenation ch = (CommonHyphenation)obj;
         return ch.language == this.language && ch.country == this.country && ch.script == this.script && ch.hyphenate == this.hyphenate && ch.hyphenationCharacter == this.hyphenationCharacter && ch.hyphenationPushCharacterCount == this.hyphenationPushCharacterCount && ch.hyphenationRemainCharacterCount == this.hyphenationRemainCharacterCount;
      }
   }

   public int hashCode() {
      if (this.hash == 0) {
         int hash = 17;
         hash = 37 * hash + (this.language == null ? 0 : this.language.hashCode());
         hash = 37 * hash + (this.script == null ? 0 : this.script.hashCode());
         hash = 37 * hash + (this.country == null ? 0 : this.country.hashCode());
         hash = 37 * hash + (this.hyphenate == null ? 0 : this.hyphenate.hashCode());
         hash = 37 * hash + (this.hyphenationCharacter == null ? 0 : this.hyphenationCharacter.hashCode());
         hash = 37 * hash + (this.hyphenationPushCharacterCount == null ? 0 : this.hyphenationPushCharacterCount.hashCode());
         hash = 37 * hash + (this.hyphenationRemainCharacterCount == null ? 0 : this.hyphenationRemainCharacterCount.hashCode());
         this.hash = hash;
      }

      return this.hash;
   }
}
