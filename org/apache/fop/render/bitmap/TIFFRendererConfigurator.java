package org.apache.fop.render.bitmap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.xmlgraphics.image.writer.Endianness;

public class TIFFRendererConfigurator extends BitmapRendererConfigurator {
   private static final Log LOG = LogFactory.getLog(TIFFRendererConfigurator.class);

   public TIFFRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      super(userAgent, rendererConfigParser);
   }

   public void configure(Renderer renderer) throws FOPException {
      TIFFRendererConfig config = (TIFFRendererConfig)this.getRendererConfig(renderer);
      if (config != null) {
         TIFFRenderer tiffRenderer = (TIFFRenderer)renderer;
         this.setCompressionMethod(config.getCompressionType(), tiffRenderer.getRenderingSettings());
      }

      super.configure(renderer);
   }

   private void setCompressionMethod(TIFFCompressionValue compression, BitmapRenderingSettings settings) throws FOPException {
      if (compression != null) {
         if (compression != TIFFCompressionValue.NONE) {
            settings.setCompressionMethod(compression.getName());
         }

         if (LOG.isInfoEnabled()) {
            LOG.info("TIFF compression set to " + compression.getName());
         }

         if (compression.hasCCITTCompression()) {
            settings.setBufferedImageType(compression.getImageType());
         }
      }

   }

   private boolean isSingleStrip(TIFFRendererConfig config) {
      Boolean singleRowPerStrip = config.isSingleStrip();
      return singleRowPerStrip == null ? false : singleRowPerStrip;
   }

   private Endianness getEndianness(TIFFRendererConfig config) {
      Endianness endianMode = config.getEndianness();
      return endianMode == null ? Endianness.DEFAULT : endianMode;
   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
      TIFFRendererConfig config = (TIFFRendererConfig)this.getRendererConfig(documentHandler);
      if (config != null) {
         TIFFDocumentHandler tiffHandler = (TIFFDocumentHandler)documentHandler;
         BitmapRenderingSettings settings = tiffHandler.getSettings();
         this.configure(documentHandler, settings, new TIFFRendererConfig.TIFFRendererConfigParser());
         this.setCompressionMethod(config.getCompressionType(), settings);
         settings.getWriterParams().setSingleStrip(this.isSingleStrip(config));
         settings.getWriterParams().setEndianness(this.getEndianness(config));
      }

   }
}
