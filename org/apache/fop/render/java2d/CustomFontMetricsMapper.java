package org.apache.fop.render.java2d;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fop.complexscripts.fonts.Positionable;
import org.apache.fop.complexscripts.fonts.Substitutable;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

public class CustomFontMetricsMapper extends Typeface implements FontMetricsMapper, Substitutable, Positionable {
   private Typeface typeface;
   private Font font;
   private float size = 1.0F;
   private static final int TYPE1_FONT = 1;

   public CustomFontMetricsMapper(CustomFont fontMetrics) throws FontFormatException, IOException {
      this.typeface = fontMetrics;
      this.initialize(fontMetrics.getInputStream());
   }

   public CustomFontMetricsMapper(LazyFont fontMetrics, InputStream fontSource) throws FontFormatException, IOException {
      this.typeface = fontMetrics;
      this.initialize(fontSource);
   }

   private void initialize(InputStream inStream) throws FontFormatException, IOException {
      int type = 0;
      if (FontType.TYPE1.equals(this.typeface.getFontType())) {
         type = 1;
      }

      this.font = Font.createFont(type, inStream);
      inStream.close();
   }

   public final String getEncodingName() {
      return null;
   }

   public final boolean hasChar(char c) {
      return this.font.canDisplay(c);
   }

   public final char mapChar(char c) {
      return this.typeface.mapChar(c);
   }

   public final Font getFont(int size) {
      if (this.size == (float)size) {
         return this.font;
      } else {
         this.size = (float)size / 1000.0F;
         this.font = this.font.deriveFont(this.size);
         return this.font;
      }
   }

   public final int getAscender(int size) {
      return this.typeface.getAscender(size);
   }

   public final int getCapHeight(int size) {
      return this.typeface.getCapHeight(size);
   }

   public final int getDescender(int size) {
      return this.typeface.getDescender(size);
   }

   public final String getEmbedFontName() {
      return this.typeface.getEmbedFontName();
   }

   public final Set getFamilyNames() {
      return this.typeface.getFamilyNames();
   }

   public final String getFontName() {
      return this.typeface.getFontName();
   }

   public final URI getFontURI() {
      return this.typeface.getFontURI();
   }

   public final FontType getFontType() {
      return this.typeface.getFontType();
   }

   public final String getFullName() {
      return this.typeface.getFullName();
   }

   public final Map getKerningInfo() {
      return this.typeface.getKerningInfo();
   }

   public final int getWidth(int i, int size) {
      return this.typeface.getWidth(i, size);
   }

   public final int[] getWidths() {
      return this.typeface.getWidths();
   }

   public Rectangle getBoundingBox(int glyphIndex, int size) {
      return this.typeface.getBoundingBox(glyphIndex, size);
   }

   public final int getXHeight(int size) {
      return this.typeface.getXHeight(size);
   }

   public int getUnderlinePosition(int size) {
      return this.typeface.getUnderlinePosition(size);
   }

   public int getUnderlineThickness(int size) {
      return this.typeface.getUnderlineThickness(size);
   }

   public int getStrikeoutPosition(int size) {
      return this.typeface.getStrikeoutPosition(size);
   }

   public int getStrikeoutThickness(int size) {
      return this.typeface.getStrikeoutThickness(size);
   }

   public final boolean hasKerningInfo() {
      return this.typeface.hasKerningInfo();
   }

   public boolean isMultiByte() {
      return this.typeface.isMultiByte();
   }

   public boolean performsPositioning() {
      return this.typeface instanceof Positionable ? ((Positionable)this.typeface).performsPositioning() : false;
   }

   public int[][] performPositioning(CharSequence cs, String script, String language, int fontSize) {
      return this.typeface instanceof Positionable ? ((Positionable)this.typeface).performPositioning(cs, script, language, fontSize) : (int[][])null;
   }

   public int[][] performPositioning(CharSequence cs, String script, String language) {
      return this.typeface instanceof Positionable ? ((Positionable)this.typeface).performPositioning(cs, script, language) : (int[][])null;
   }

   public boolean performsSubstitution() {
      return this.typeface instanceof Substitutable ? ((Substitutable)this.typeface).performsSubstitution() : false;
   }

   public CharSequence performSubstitution(CharSequence cs, String script, String language, List associations, boolean retainControls) {
      return this.typeface instanceof Substitutable ? ((Substitutable)this.typeface).performSubstitution(cs, script, language, associations, retainControls) : cs;
   }

   public CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa, String script, String language, List associations) {
      return this.typeface instanceof Substitutable ? ((Substitutable)this.typeface).reorderCombiningMarks(cs, gpa, script, language, associations) : cs;
   }

   public Typeface getRealFont() {
      return this.typeface;
   }
}
