package org.apache.fop.render.txt;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractRendererMaker;
import org.apache.fop.render.Renderer;

public class TXTRendererMaker extends AbstractRendererMaker {
   private static final String[] MIMES = new String[]{"text/plain"};

   public Renderer makeRenderer(FOUserAgent userAgent) {
      return new TXTRenderer(userAgent);
   }

   public void configureRenderer(FOUserAgent userAgent, Renderer renderer) throws FOPException {
      (new TXTRendererConfigurator(userAgent, new TxtRendererConfig.TxtRendererConfigParser())).configure(renderer);
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
