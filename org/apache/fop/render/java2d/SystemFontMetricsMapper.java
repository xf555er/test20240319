package org.apache.fop.render.java2d;

import java.awt.Font;
import java.awt.Rectangle;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.Typeface;

public class SystemFontMetricsMapper extends Typeface implements FontMetricsMapper {
   private final Java2DFontMetrics java2DFontMetrics;
   private final URI fontFileURI;
   private final String family;
   private final int style;

   public SystemFontMetricsMapper(String family, int style, Java2DFontMetrics java2DFontMetrics) {
      URI uri;
      try {
         uri = new URI("system:" + family.toLowerCase());
      } catch (URISyntaxException var6) {
         uri = null;
      }

      this.fontFileURI = uri;
      this.family = family;
      this.style = style;
      this.java2DFontMetrics = java2DFontMetrics;
   }

   public final URI getFontURI() {
      return null;
   }

   public String getFontName() {
      return this.family;
   }

   public String getEmbedFontName() {
      return this.getFontName();
   }

   public String getFullName() {
      return this.getFontName();
   }

   public Set getFamilyNames() {
      Set s = new HashSet();
      s.add(this.family);
      return s;
   }

   public FontType getFontType() {
      return FontType.OTHER;
   }

   public int getMaxAscent(int size) {
      return this.java2DFontMetrics.getMaxAscent(this.family, this.style, size);
   }

   public int getAscender(int size) {
      return this.java2DFontMetrics.getAscender(this.family, this.style, size);
   }

   public int getCapHeight(int size) {
      return this.java2DFontMetrics.getCapHeight(this.family, this.style, size);
   }

   public int getDescender(int size) {
      return this.java2DFontMetrics.getDescender(this.family, this.style, size);
   }

   public int getXHeight(int size) {
      return this.java2DFontMetrics.getXHeight(this.family, this.style, size);
   }

   public int getUnderlinePosition(int size) {
      return this.java2DFontMetrics.getUnderlinePosition(this.family, this.style, size);
   }

   public int getUnderlineThickness(int size) {
      return this.java2DFontMetrics.getUnderlineThickness(this.family, this.style, size);
   }

   public int getStrikeoutPosition(int size) {
      return this.java2DFontMetrics.getStrikeoutPosition(this.family, this.style, size);
   }

   public int getStrikeoutThickness(int size) {
      return this.java2DFontMetrics.getStrikeoutThickness(this.family, this.style, size);
   }

   public int getWidth(int i, int size) {
      return this.java2DFontMetrics.width(i, this.family, this.style, size);
   }

   public int[] getWidths() {
      return this.java2DFontMetrics.getWidths(this.family, this.style, 1);
   }

   public Rectangle getBoundingBox(int glyphIndex, int size) {
      throw new UnsupportedOperationException("Not implemented");
   }

   public Font getFont(int size) {
      return this.java2DFontMetrics.getFont(this.family, this.style, size);
   }

   public Map getKerningInfo() {
      return Collections.EMPTY_MAP;
   }

   public boolean hasKerningInfo() {
      return false;
   }

   public String getEncodingName() {
      return null;
   }

   public char mapChar(char c) {
      return c;
   }

   public boolean hasChar(char c) {
      return this.java2DFontMetrics.hasChar(this.family, this.style, 1, c);
   }
}
