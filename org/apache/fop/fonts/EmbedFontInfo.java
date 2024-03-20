package org.apache.fop.fonts;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

public class EmbedFontInfo implements Serializable {
   private static final long serialVersionUID = 8755432068669997369L;
   protected final boolean kerning;
   protected final boolean advanced;
   private final EncodingMode encodingMode;
   private final EmbeddingMode embeddingMode;
   private final boolean simulateStyle;
   private final boolean embedAsType1;
   private final boolean useSVG;
   protected String postScriptName;
   protected String subFontName;
   private List fontTriplets;
   private transient boolean embedded;
   private FontUris fontUris;

   public EmbedFontInfo(FontUris fontUris, boolean kerning, boolean advanced, List fontTriplets, String subFontName, EncodingMode encodingMode, EmbeddingMode embeddingMode, boolean simulateStyle, boolean embedAsType1, boolean useSVG) {
      this.embedded = true;
      this.kerning = kerning;
      this.advanced = advanced;
      this.fontTriplets = fontTriplets;
      this.subFontName = subFontName;
      this.encodingMode = encodingMode;
      this.embeddingMode = embeddingMode;
      this.fontUris = fontUris;
      this.simulateStyle = simulateStyle;
      this.embedAsType1 = embedAsType1;
      this.useSVG = useSVG;
   }

   public EmbedFontInfo(FontUris fontUris, boolean kerning, boolean advanced, List fontTriplets, String subFontName) {
      this(fontUris, kerning, advanced, fontTriplets, subFontName, EncodingMode.AUTO, EmbeddingMode.AUTO, false, false, true);
   }

   public URI getMetricsURI() {
      return this.fontUris.getMetrics();
   }

   public URI getEmbedURI() {
      return this.fontUris.getEmbed();
   }

   public boolean getKerning() {
      return this.kerning;
   }

   public boolean getAdvanced() {
      return this.advanced;
   }

   public String getSubFontName() {
      return this.subFontName;
   }

   public String getPostScriptName() {
      return this.postScriptName;
   }

   public void setPostScriptName(String postScriptName) {
      this.postScriptName = postScriptName;
   }

   public List getFontTriplets() {
      return this.fontTriplets;
   }

   public boolean isEmbedded() {
      return this.fontUris.getEmbed() == null ? false : this.embedded;
   }

   public EmbeddingMode getEmbeddingMode() {
      return this.embeddingMode;
   }

   public void setEmbedded(boolean value) {
      this.embedded = value;
   }

   public EncodingMode getEncodingMode() {
      return this.encodingMode;
   }

   public boolean getSimulateStyle() {
      return this.simulateStyle;
   }

   public boolean getEmbedAsType1() {
      return this.embedAsType1;
   }

   public boolean getUseSVG() {
      return this.useSVG;
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      this.embedded = true;
   }

   public String toString() {
      return "metrics-uri=" + this.fontUris.getMetrics() + ", embed-uri=" + this.fontUris.getEmbed() + ", kerning=" + this.kerning + ", advanced=" + this.advanced + ", enc-mode=" + this.encodingMode + ", font-triplet=" + this.fontTriplets + (this.getSubFontName() != null ? ", sub-font=" + this.getSubFontName() : "") + (this.isEmbedded() ? "" : ", NOT embedded");
   }

   public FontUris getFontUris() {
      return this.fontUris;
   }
}
