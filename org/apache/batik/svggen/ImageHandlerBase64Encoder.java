package org.apache.batik.svggen;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.batik.ext.awt.image.spi.ImageWriter;
import org.apache.batik.ext.awt.image.spi.ImageWriterRegistry;
import org.apache.batik.util.Base64EncoderStream;
import org.w3c.dom.Element;

public class ImageHandlerBase64Encoder extends DefaultImageHandler {
   public void handleHREF(Image image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      if (image == null) {
         throw new SVGGraphics2DRuntimeException("image should not be null");
      } else {
         int width = image.getWidth((ImageObserver)null);
         int height = image.getHeight((ImageObserver)null);
         if (width != 0 && height != 0) {
            if (image instanceof RenderedImage) {
               this.handleHREF((RenderedImage)image, imageElement, generatorContext);
            } else {
               BufferedImage buf = new BufferedImage(width, height, 2);
               Graphics2D g = buf.createGraphics();
               g.drawImage(image, 0, 0, (ImageObserver)null);
               g.dispose();
               this.handleHREF((RenderedImage)buf, imageElement, generatorContext);
            }
         } else {
            this.handleEmptyImage(imageElement);
         }

      }
   }

   public void handleHREF(RenderableImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      if (image == null) {
         throw new SVGGraphics2DRuntimeException("image should not be null");
      } else {
         RenderedImage r = image.createDefaultRendering();
         if (r == null) {
            this.handleEmptyImage(imageElement);
         } else {
            this.handleHREF(r, imageElement, generatorContext);
         }

      }
   }

   protected void handleEmptyImage(Element imageElement) {
      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "data:image/png;base64,");
      imageElement.setAttributeNS((String)null, "width", "0");
      imageElement.setAttributeNS((String)null, "height", "0");
   }

   public void handleHREF(RenderedImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      Base64EncoderStream b64Encoder = new Base64EncoderStream(os);

      try {
         this.encodeImage(image, b64Encoder);
         b64Encoder.close();
      } catch (IOException var7) {
         throw new SVGGraphics2DIOException("unexpected exception", var7);
      }

      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "data:image/png;base64," + os.toString());
   }

   public void encodeImage(RenderedImage buf, OutputStream os) throws SVGGraphics2DIOException {
      try {
         ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor("image/png");
         writer.writeImage(buf, os);
      } catch (IOException var4) {
         throw new SVGGraphics2DIOException("unexpected exception");
      }
   }

   public BufferedImage buildBufferedImage(Dimension size) {
      return new BufferedImage(size.width, size.height, 2);
   }
}
