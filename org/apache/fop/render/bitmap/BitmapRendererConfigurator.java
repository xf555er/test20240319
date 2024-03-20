package org.apache.fop.render.bitmap;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.java2d.Base14FontCollection;
import org.apache.fop.render.java2d.ConfiguredFontCollection;
import org.apache.fop.render.java2d.InstalledFontCollection;
import org.apache.fop.render.java2d.Java2DFontMetrics;
import org.apache.fop.render.java2d.Java2DRendererConfigurator;

public class BitmapRendererConfigurator extends Java2DRendererConfigurator {
   public BitmapRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
      AbstractBitmapDocumentHandler bitmapHandler = (AbstractBitmapDocumentHandler)documentHandler;
      BitmapRenderingSettings settings = bitmapHandler.getSettings();
      this.configure(documentHandler, settings, new BitmapRendererConfig.BitmapRendererConfigParser("image/x-bitmap"));
   }

   void configure(IFDocumentHandler documentHandler, BitmapRenderingSettings settings, BitmapRendererConfig.BitmapRendererConfigParser parser) throws FOPException {
      BitmapRendererConfig config = (BitmapRendererConfig)this.userAgent.getRendererConfig(documentHandler.getMimeType(), parser);
      this.configure(config, settings);
   }

   private void configure(BitmapRendererConfig config, BitmapRenderingSettings settings) throws FOPException {
      if (config.hasTransparentBackround()) {
         settings.setPageBackgroundColor((Color)null);
      } else if (config.getBackgroundColor() != null) {
         settings.setPageBackgroundColor(config.getBackgroundColor());
      }

      if (config.hasAntiAliasing() != null) {
         settings.setAntiAliasing(config.hasAntiAliasing());
      }

      if (config.isRenderHighQuality() != null) {
         settings.setQualityRendering(config.isRenderHighQuality());
      }

      if (config.getColorMode() != null) {
         settings.setBufferedImageType(config.getColorMode());
      }

   }

   protected FontCollection createCollectionFromFontList(InternalResourceResolver resourceResolver, List fontList) {
      return new ConfiguredFontCollection(resourceResolver, fontList, this.userAgent.isComplexScriptFeaturesEnabled());
   }

   protected List getDefaultFontCollection() {
      Java2DFontMetrics java2DFontMetrics = new Java2DFontMetrics();
      List fontCollection = new ArrayList();
      fontCollection.add(new Base14FontCollection(java2DFontMetrics));
      fontCollection.add(new InstalledFontCollection(java2DFontMetrics));
      return fontCollection;
   }
}
