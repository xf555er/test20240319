package org.apache.fop.render.xml;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractRendererMaker;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;

public class XMLRendererMaker extends AbstractRendererMaker {
   private static final String[] MIMES = new String[]{"application/X-fop-areatree"};

   public Renderer makeRenderer(FOUserAgent userAgent) {
      return new XMLRenderer(userAgent);
   }

   public void configureRenderer(FOUserAgent userAgent, Renderer renderer) throws FOPException {
      PrintRendererConfigurator.createDefaultInstance(userAgent).configure(renderer);
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
