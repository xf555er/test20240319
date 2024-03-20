package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.Typeface;

public abstract class AFPFont extends Typeface {
   private static final double STRIKEOUT_POSITION_FACTOR = 0.45;
   protected final String name;
   private final boolean embeddable;

   public AFPFont(String name, boolean embeddable) {
      this.name = name;
      this.embeddable = embeddable;
   }

   public URI getFontURI() {
      return null;
   }

   public String getFontName() {
      return this.name;
   }

   public String getEmbedFontName() {
      return this.name;
   }

   public String getFullName() {
      return this.getFontName();
   }

   public Set getFamilyNames() {
      Set s = new HashSet();
      s.add(this.name);
      return s;
   }

   public FontType getFontType() {
      return FontType.OTHER;
   }

   public boolean hasKerningInfo() {
      return false;
   }

   public Map getKerningInfo() {
      return null;
   }

   public abstract CharacterSet getCharacterSet(int var1);

   public boolean isEmbeddable() {
      return this.embeddable;
   }

   protected static final char toUnicodeCodepoint(int character) {
      return (char)character;
   }

   public int getUnderlineThickness(int size) {
      return this.getBoundingBox(45, size).height;
   }

   public int getStrikeoutPosition(int size) {
      return (int)(0.45 * (double)this.getCapHeight(size));
   }

   public int getStrikeoutThickness(int size) {
      return this.getBoundingBox(45, size).height;
   }

   public abstract Rectangle getBoundingBox(int var1, int var2);

   public int[] getWidths() {
      throw new UnsupportedOperationException();
   }

   public String toString() {
      return "name=" + this.name;
   }
}
