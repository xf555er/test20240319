package org.apache.fop.render;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;

public abstract class AbstractConfigurator {
   protected static final Log log = LogFactory.getLog(AbstractConfigurator.class);
   private static final String MIME = "mime";
   protected FOUserAgent userAgent;

   public AbstractConfigurator(FOUserAgent userAgent) {
      this.userAgent = userAgent;
   }

   public abstract String getType();
}
