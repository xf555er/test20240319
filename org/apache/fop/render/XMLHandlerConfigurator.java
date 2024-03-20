package org.apache.fop.render;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;

public class XMLHandlerConfigurator extends AbstractRendererConfigurator {
   protected static final Log log = LogFactory.getLog(XMLHandlerConfigurator.class);

   public XMLHandlerConfigurator(FOUserAgent userAgent) {
      super(userAgent);
   }

   private Configuration getHandlerConfig(Configuration cfg, String namespace) {
      if (cfg != null && namespace != null) {
         Configuration handlerConfig = null;
         Configuration[] children = cfg.getChildren("xml-handler");
         Configuration[] var5 = children;
         int var6 = children.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Configuration aChildren = var5[var7];

            try {
               if (aChildren.getAttribute("namespace").equals(namespace)) {
                  handlerConfig = aChildren;
                  break;
               }
            } catch (ConfigurationException var10) {
            }
         }

         if (log.isDebugEnabled()) {
            log.debug((handlerConfig == null ? "No" : "") + "XML handler configuration found for namespace " + namespace);
         }

         return handlerConfig;
      } else {
         return null;
      }
   }

   public void configure(RendererContext context, String ns) throws FOPException {
      Configuration cfg = this.userAgent.getRendererConfiguration(context.getRenderer().getMimeType());
      if (cfg != null) {
         cfg = this.getHandlerConfig(cfg, ns);
         if (cfg != null) {
            context.setProperty("cfg", cfg);
         }
      }

   }
}
