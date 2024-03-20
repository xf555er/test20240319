package org.apache.fop.render.bitmap;

import java.awt.image.BufferedImage;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.java2d.Java2DRenderer;
import org.apache.xmlgraphics.image.GraphicsUtil;
import org.apache.xmlgraphics.image.rendered.FormatRed;
import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

public class TIFFRenderer extends Java2DRenderer {
   private BitmapRenderingSettings imageSettings = new BitmapRenderingSettings();
   private OutputStream outputStream;

   public String getMimeType() {
      return "image/tiff";
   }

   public TIFFRenderer(FOUserAgent userAgent) {
      super(userAgent);
      this.imageSettings.setCompressionMethod(TIFFCompressionValue.PACKBITS.getName());
      this.imageSettings.setBufferedImageType(2);
      int dpi = Math.round(userAgent.getTargetResolution());
      this.imageSettings.setResolution(dpi);
   }

   public void startRenderer(OutputStream outputStream) throws IOException {
      this.outputStream = outputStream;
      super.startRenderer(outputStream);
   }

   public void stopRenderer() throws IOException {
      super.stopRenderer();
      log.debug("Starting TIFF encoding ...");
      Iterator pageImagesItr = new LazyPageImagesIterator(this.getNumberOfPages(), log);
      ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(this.getMimeType());
      if (writer == null) {
         BitmapRendererEventProducer eventProducer = BitmapRendererEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
         eventProducer.noImageWriterFound(this, this.getMimeType());
      } else {
         if (writer.supportsMultiImageWriter()) {
            MultiImageWriter multiWriter = writer.createMultiImageWriter(this.outputStream);

            try {
               while(pageImagesItr.hasNext()) {
                  RenderedImage img = (RenderedImage)pageImagesItr.next();
                  multiWriter.writeImage(img, this.imageSettings.getWriterParams());
               }
            } finally {
               multiWriter.close();
            }
         } else {
            RenderedImage renderedImage = null;
            if (pageImagesItr.hasNext()) {
               renderedImage = (RenderedImage)pageImagesItr.next();
            }

            writer.writeImage(renderedImage, this.outputStream, this.imageSettings.getWriterParams());
            if (pageImagesItr.hasNext()) {
               BitmapRendererEventProducer eventProducer = BitmapRendererEventProducer.Provider.get(this.getUserAgent().getEventBroadcaster());
               eventProducer.stoppingAfterFirstPageNoFilename(this);
            }
         }

         this.outputStream.flush();
         this.clearViewportList();
      }

      log.debug("TIFF encoding done.");
   }

   protected BufferedImage getBufferedImage(int bitmapWidth, int bitmapHeight) {
      return new BufferedImage(bitmapWidth, bitmapHeight, this.imageSettings.getBufferedImageType());
   }

   public void setBufferedImageType(int bufferedImageType) {
      this.imageSettings.setBufferedImageType(bufferedImageType);
   }

   public BitmapRenderingSettings getRenderingSettings() {
      return this.imageSettings;
   }

   private class LazyPageImagesIterator implements Iterator {
      private Log log;
      private int count;
      private int current;

      public LazyPageImagesIterator(int c, Log log) {
         this.count = c;
         this.log = log;
      }

      public boolean hasNext() {
         return this.current < this.count;
      }

      public Object next() {
         if (this.log.isDebugEnabled()) {
            this.log.debug("[" + (this.current + 1) + "]");
         }

         BufferedImage pageImage = null;

         try {
            pageImage = TIFFRenderer.this.getPageImage(this.current++);
         } catch (FOPException var10) {
            throw new NoSuchElementException(var10.getMessage());
         }

         TIFFCompressionValue compression = TIFFCompressionValue.getType(TIFFRenderer.this.imageSettings.getCompressionMethod());
         if (compression != TIFFCompressionValue.CCITT_T4 && compression != TIFFCompressionValue.CCITT_T6) {
            SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)pageImage.getSampleModel();
            int bands = sppsm.getNumBands();
            int[] off = new int[bands];
            int w = pageImage.getWidth();
            int h = pageImage.getHeight();

            for(int i = 0; i < bands; off[i] = i++) {
            }

            SampleModel sm = new PixelInterleavedSampleModel(0, w, h, bands, w * bands, off);
            RenderedImage rimg = new FormatRed(GraphicsUtil.wrap(pageImage), sm);
            return rimg;
         } else {
            return pageImage;
         }
      }

      public void remove() {
         throw new UnsupportedOperationException("Method 'remove' is not supported.");
      }
   }
}
