package org.apache.fop.render;

import java.util.ArrayList;
import java.util.List;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.base14.Base14FontCollection;

public class DefaultRendererConfigurator extends PrintRendererConfigurator {
   public DefaultRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   protected List getDefaultFontCollection() {
      FontManager fontManager = this.userAgent.getFontManager();
      List fontCollection = new ArrayList();
      fontCollection.add(new Base14FontCollection(fontManager.isBase14KerningEnabled()));
      return fontCollection;
   }
}
