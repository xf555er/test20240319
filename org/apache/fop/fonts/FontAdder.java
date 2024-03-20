package org.apache.fop.fonts;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.autodetect.FontInfoFinder;

public class FontAdder {
   private final FontEventListener listener;
   private final InternalResourceResolver resourceResolver;
   private final FontManager manager;

   public FontAdder(FontManager manager, InternalResourceResolver resourceResolver, FontEventListener listener) {
      this.manager = manager;
      this.resourceResolver = resourceResolver;
      this.listener = listener;
   }

   public void add(List fontURLList, List fontInfoList) throws URISyntaxException {
      FontCache cache = this.manager.getFontCache();
      FontInfoFinder finder = new FontInfoFinder();
      finder.setEventListener(this.listener);
      Iterator var5 = fontURLList.iterator();

      while(true) {
         EmbedFontInfo[] embedFontInfos;
         do {
            if (!var5.hasNext()) {
               return;
            }

            URL fontURL = (URL)var5.next();
            embedFontInfos = finder.find(fontURL.toURI(), this.resourceResolver, cache);
         } while(embedFontInfos == null);

         EmbedFontInfo[] var8 = embedFontInfos;
         int var9 = embedFontInfos.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            EmbedFontInfo fontInfo = var8[var10];
            if (fontInfo != null) {
               fontInfoList.add(fontInfo);
            }
         }
      }
   }
}
