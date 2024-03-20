package org.apache.fop.fonts;

import java.awt.Rectangle;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.complexscripts.fonts.Positionable;
import org.apache.fop.complexscripts.fonts.Substitutable;
import org.xml.sax.InputSource;

public class LazyFont extends Typeface implements FontDescriptor, Substitutable, Positionable {
   private static Log log = LogFactory.getLog(LazyFont.class);
   private final FontUris fontUris;
   private final boolean useKerning;
   private final boolean useAdvanced;
   private boolean simulateStyle;
   private boolean embedAsType1;
   private boolean useSVG;
   private final EncodingMode encodingMode;
   private final EmbeddingMode embeddingMode;
   private final String subFontName;
   private final boolean embedded;
   private final InternalResourceResolver resourceResolver;
   private boolean isMetricsLoaded;
   private Typeface realFont;
   private FontDescriptor realFontDescriptor;

   public LazyFont(EmbedFontInfo fontInfo, InternalResourceResolver resourceResolver, boolean useComplexScripts) {
      this.fontUris = fontInfo.getFontUris();
      this.useKerning = fontInfo.getKerning();
      if (resourceResolver != null) {
         this.useAdvanced = useComplexScripts;
      } else {
         this.useAdvanced = fontInfo.getAdvanced();
      }

      this.simulateStyle = fontInfo.getSimulateStyle();
      this.embedAsType1 = fontInfo.getEmbedAsType1();
      this.useSVG = fontInfo.getUseSVG();
      this.encodingMode = fontInfo.getEncodingMode() != null ? fontInfo.getEncodingMode() : EncodingMode.AUTO;
      this.embeddingMode = fontInfo.getEmbeddingMode() != null ? fontInfo.getEmbeddingMode() : EmbeddingMode.AUTO;
      this.subFontName = fontInfo.getSubFontName();
      this.embedded = fontInfo.isEmbedded();
      this.resourceResolver = resourceResolver;
   }

   public String toString() {
      StringBuffer sbuf = new StringBuffer(super.toString());
      sbuf.append('{');
      sbuf.append("metrics-url=" + this.fontUris.getMetrics());
      sbuf.append(",embed-url=" + this.fontUris.getEmbed());
      sbuf.append(",kerning=" + this.useKerning);
      sbuf.append(",advanced=" + this.useAdvanced);
      sbuf.append('}');
      return sbuf.toString();
   }

   private void load(boolean fail) {
      if (!this.isMetricsLoaded) {
         String error;
         try {
            if (this.fontUris.getMetrics() != null) {
               XMLFontMetricsReader reader = null;
               InputStream in = this.resourceResolver.getResource(this.fontUris.getMetrics());
               InputSource src = new InputSource(in);
               src.setSystemId(this.fontUris.getMetrics().toASCIIString());
               reader = new XMLFontMetricsReader(src, this.resourceResolver);
               reader.setKerningEnabled(this.useKerning);
               reader.setAdvancedEnabled(this.useAdvanced);
               if (this.embedded) {
                  reader.setFontEmbedURI(this.fontUris.getEmbed());
               }

               this.realFont = reader.getFont();
            } else {
               if (this.fontUris.getEmbed() == null) {
                  throw new RuntimeException("Cannot load font. No font URIs available.");
               }

               this.realFont = FontLoader.loadFont(this.fontUris, this.subFontName, this.embedded, this.embeddingMode, this.encodingMode, this.useKerning, this.useAdvanced, this.resourceResolver, this.simulateStyle, this.embedAsType1, this.useSVG);
            }

            if (this.realFont instanceof FontDescriptor) {
               this.realFontDescriptor = (FontDescriptor)this.realFont;
            }
         } catch (RuntimeException var5) {
            error = "Failed to read font file " + this.fontUris.getEmbed() + " " + var5.getMessage();
            throw new RuntimeException(error, var5);
         } catch (Exception var6) {
            error = "Failed to read font file " + this.fontUris.getEmbed() + " " + var6.getMessage();
            log.error(error, var6);
            if (fail) {
               throw new RuntimeException(error, var6);
            }
         }

         this.realFont.setEventListener(this.eventListener);
         this.isMetricsLoaded = true;
      }

   }

   public Typeface getRealFont() {
      this.load(false);
      return this.realFont;
   }

   public String getEncodingName() {
      this.load(true);
      return this.realFont.getEncodingName();
   }

   public char mapChar(char c) {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFont.mapChar(c);
   }

   public boolean hadMappingOperations() {
      this.load(true);
      return this.realFont.hadMappingOperations();
   }

   public boolean hasChar(char c) {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFont.hasChar(c);
   }

   public boolean isMultiByte() {
      this.load(true);
      return this.realFont.isMultiByte();
   }

   public URI getFontURI() {
      this.load(true);
      return this.realFont.getFontURI();
   }

   public String getFontName() {
      this.load(true);
      return this.realFont.getFontName();
   }

   public String getEmbedFontName() {
      this.load(true);
      return this.realFont.getEmbedFontName();
   }

   public String getFullName() {
      this.load(true);
      return this.realFont.getFullName();
   }

   public Set getFamilyNames() {
      this.load(true);
      return this.realFont.getFamilyNames();
   }

   public int getMaxAscent(int size) {
      this.load(true);
      return this.realFont.getMaxAscent(size);
   }

   public int getAscender(int size) {
      this.load(true);
      return this.realFont.getAscender(size);
   }

   public int getCapHeight(int size) {
      this.load(true);
      return this.realFont.getCapHeight(size);
   }

   public int getDescender(int size) {
      this.load(true);
      return this.realFont.getDescender(size);
   }

   public int getXHeight(int size) {
      this.load(true);
      return this.realFont.getXHeight(size);
   }

   public int getUnderlinePosition(int size) {
      this.load(true);
      return this.realFont.getUnderlinePosition(size);
   }

   public int getUnderlineThickness(int size) {
      this.load(true);
      return this.realFont.getUnderlineThickness(size);
   }

   public int getStrikeoutPosition(int size) {
      this.load(true);
      return this.realFont.getStrikeoutPosition(size);
   }

   public int getStrikeoutThickness(int size) {
      this.load(true);
      return this.realFont.getStrikeoutThickness(size);
   }

   public int getWidth(int i, int size) {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFont.getWidth(i, size);
   }

   public int[] getWidths() {
      this.load(true);
      return this.realFont.getWidths();
   }

   public Rectangle getBoundingBox(int glyphIndex, int size) {
      this.load(true);
      return this.realFont.getBoundingBox(glyphIndex, size);
   }

   public boolean hasKerningInfo() {
      this.load(true);
      return this.realFont.hasKerningInfo();
   }

   public Map getKerningInfo() {
      this.load(true);
      return this.realFont.getKerningInfo();
   }

   public boolean hasFeature(int tableType, String script, String language, String feature) {
      this.load(true);
      return this.realFont.hasFeature(tableType, script, language, feature);
   }

   public int getCapHeight() {
      this.load(true);
      return this.realFontDescriptor.getCapHeight();
   }

   public int getDescender() {
      this.load(true);
      return this.realFontDescriptor.getDescender();
   }

   public int getAscender() {
      this.load(true);
      return this.realFontDescriptor.getAscender();
   }

   public int getFlags() {
      this.load(true);
      return this.realFontDescriptor.getFlags();
   }

   public boolean isSymbolicFont() {
      this.load(true);
      return this.realFontDescriptor.isSymbolicFont();
   }

   public int[] getFontBBox() {
      this.load(true);
      return this.realFontDescriptor.getFontBBox();
   }

   public int getItalicAngle() {
      this.load(true);
      return this.realFontDescriptor.getItalicAngle();
   }

   public int getStemV() {
      this.load(true);
      return this.realFontDescriptor.getStemV();
   }

   public FontType getFontType() {
      this.load(true);
      return this.realFontDescriptor.getFontType();
   }

   public boolean isEmbeddable() {
      this.load(true);
      return this.realFontDescriptor.isEmbeddable();
   }

   public boolean performsSubstitution() {
      this.load(true);
      return this.realFontDescriptor instanceof Substitutable ? ((Substitutable)this.realFontDescriptor).performsSubstitution() : false;
   }

   public CharSequence performSubstitution(CharSequence cs, String script, String language, List associations, boolean retainControls) {
      this.load(true);
      return this.realFontDescriptor instanceof Substitutable ? ((Substitutable)this.realFontDescriptor).performSubstitution(cs, script, language, associations, retainControls) : cs;
   }

   public CharSequence reorderCombiningMarks(CharSequence cs, int[][] gpa, String script, String language, List associations) {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFontDescriptor instanceof Substitutable ? ((Substitutable)this.realFontDescriptor).reorderCombiningMarks(cs, gpa, script, language, associations) : cs;
   }

   public boolean performsPositioning() {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFontDescriptor instanceof Positionable ? ((Positionable)this.realFontDescriptor).performsPositioning() : false;
   }

   public int[][] performPositioning(CharSequence cs, String script, String language, int fontSize) {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFontDescriptor instanceof Positionable ? ((Positionable)this.realFontDescriptor).performPositioning(cs, script, language, fontSize) : (int[][])null;
   }

   public int[][] performPositioning(CharSequence cs, String script, String language) {
      if (!this.isMetricsLoaded) {
         this.load(true);
      }

      return this.realFontDescriptor instanceof Positionable ? ((Positionable)this.realFontDescriptor).performPositioning(cs, script, language) : (int[][])null;
   }

   public boolean isSubsetEmbedded() {
      this.load(true);
      return this.realFont.isMultiByte() && this.embeddingMode == EmbeddingMode.FULL ? false : this.realFont.isMultiByte();
   }
}
