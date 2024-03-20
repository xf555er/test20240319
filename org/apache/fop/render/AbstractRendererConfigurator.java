package org.apache.fop.render;

import org.apache.fop.apps.FOUserAgent;

public abstract class AbstractRendererConfigurator {
   protected final FOUserAgent userAgent;

   public AbstractRendererConfigurator(FOUserAgent userAgent) {
      this.userAgent = userAgent;
   }

   public static String getType() {
      return "renderer";
   }
}
