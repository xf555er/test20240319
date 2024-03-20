package org.apache.fop.render.ps;

import java.util.HashMap;
import java.util.Map;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;
import org.apache.xmlgraphics.ps.PSResource;

class FontResourceCache {
   private final FontInfo fontInfo;
   private Map fontResources = new HashMap();

   public FontResourceCache(FontInfo fontInfo) {
      this.fontInfo = fontInfo;
   }

   public PSFontResource getFontResourceForFontKey(String key) {
      PSFontResource res = null;
      if (this.fontResources != null) {
         res = (PSFontResource)this.fontResources.get(key);
      } else {
         this.fontResources = new HashMap();
      }

      if (res == null) {
         res = PSFontResource.createFontResource(new PSResource("font", this.getPostScriptNameForFontKey(key)));
         this.fontResources.put(key, res);
      }

      return res;
   }

   private String getPostScriptNameForFontKey(String key) {
      int pos = key.indexOf(95);
      String postFix = null;
      if (pos > 0) {
         postFix = key.substring(pos);
         key = key.substring(0, pos);
      }

      Map fonts = this.fontInfo.getFonts();
      Typeface tf = (Typeface)fonts.get(key);
      if (tf instanceof LazyFont) {
         tf = ((LazyFont)tf).getRealFont();
      }

      if (tf == null) {
         throw new IllegalStateException("Font not available: " + key);
      } else {
         return postFix == null ? tf.getEmbedFontName() : tf.getEmbedFontName() + postFix;
      }
   }

   public void addAll(Map fontMap) {
      this.fontResources.putAll(fontMap);
   }
}
