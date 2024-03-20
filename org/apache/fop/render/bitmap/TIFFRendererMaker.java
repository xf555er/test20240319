package org.apache.fop.render.bitmap;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractRendererMaker;
import org.apache.fop.render.Renderer;

public class TIFFRendererMaker extends AbstractRendererMaker {
   private static final String[] MIMES = new String[]{"image/tiff"};

   public Renderer makeRenderer(FOUserAgent userAgent) {
      return new TIFFRenderer(userAgent);
   }

   public boolean needsOutputStream() {
      return true;
   }

   public String[] getSupportedMimeTypes() {
      return MIMES;
   }

   public void configureRenderer(FOUserAgent userAgent, Renderer renderer) throws FOPException {
      (new TIFFRendererConfigurator(userAgent, new TIFFRendererConfig.TIFFRendererConfigParser())).configure(renderer);
   }
}
