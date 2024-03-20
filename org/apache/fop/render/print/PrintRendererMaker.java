package org.apache.fop.render.print;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractRendererMaker;
import org.apache.fop.render.PrintRendererConfigurator;
import org.apache.fop.render.Renderer;

public class PrintRendererMaker extends AbstractRendererMaker {
   private static final String[] MIMES = new String[]{"application/X-fop-print"};

   public Renderer makeRenderer(FOUserAgent userAgent) {
      return new PrintRenderer(userAgent);
   }

   public void configureRenderer(FOUserAgent userAgent, Renderer renderer) throws FOPException {
      PrintRendererConfigurator.createDefaultInstance(userAgent).configure(renderer);
   }

   public boolean needsOutputStream() {
      return false;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }
}
