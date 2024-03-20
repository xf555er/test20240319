package org.apache.fop.fonts;

import java.io.IOException;
import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.type1.Type1FontLoader;

public abstract class FontLoader {
   protected static final Log log = LogFactory.getLog(FontLoader.class);
   protected final URI fontFileURI;
   protected final InternalResourceResolver resourceResolver;
   protected CustomFont returnFont;
   protected boolean loaded;
   protected boolean embedded;
   protected boolean useKerning;
   protected boolean useAdvanced;

   public FontLoader(URI fontFileURI, boolean embedded, boolean useKerning, boolean useAdvanced, InternalResourceResolver resourceResolver) {
      this.fontFileURI = fontFileURI;
      this.embedded = embedded;
      this.useKerning = useKerning;
      this.useAdvanced = useAdvanced;
      this.resourceResolver = resourceResolver;
   }

   private static boolean isType1(FontUris fontUris) {
      return fontUris.getEmbed().toASCIIString().toLowerCase().endsWith(".pfb") || fontUris.getAfm() != null || fontUris.getPfm() != null;
   }

   public static CustomFont loadFont(FontUris fontUris, String subFontName, boolean embedded, EmbeddingMode embeddingMode, EncodingMode encodingMode, boolean useKerning, boolean useAdvanced, InternalResourceResolver resourceResolver, boolean simulateStyle, boolean embedAsType1, boolean useSVG) throws IOException {
      boolean type1 = isType1(fontUris);
      Object loader;
      if (type1) {
         if (encodingMode == EncodingMode.CID) {
            throw new IllegalArgumentException("CID encoding mode not supported for Type 1 fonts");
         }

         loader = new Type1FontLoader(fontUris, embedded, embeddingMode, useKerning, resourceResolver);
      } else {
         loader = new OFFontLoader(fontUris.getEmbed(), subFontName, embedded, embeddingMode, encodingMode, useKerning, useAdvanced, resourceResolver, simulateStyle, embedAsType1, useSVG);
      }

      return ((FontLoader)loader).getFont();
   }

   protected abstract void read() throws IOException;

   public CustomFont getFont() throws IOException {
      if (!this.loaded) {
         this.read();
      }

      return this.returnFont;
   }
}
