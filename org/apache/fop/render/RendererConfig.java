package org.apache.fop.render;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.FontConfig;

public interface RendererConfig {
   FontConfig getFontInfoConfig();

   public interface RendererConfigParser {
      RendererConfig build(FOUserAgent var1, Configuration var2) throws FOPException;

      String getMimeType();
   }
}
