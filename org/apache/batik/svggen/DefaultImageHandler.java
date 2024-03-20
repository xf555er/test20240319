package org.apache.batik.svggen;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import org.apache.batik.constants.XMLConstants;
import org.w3c.dom.Element;

public class DefaultImageHandler implements ImageHandler, ErrorConstants, XMLConstants {
   public void handleImage(Image image, Element imageElement, SVGGeneratorContext generatorContext) {
      imageElement.setAttributeNS((String)null, "width", String.valueOf(image.getWidth((ImageObserver)null)));
      imageElement.setAttributeNS((String)null, "height", String.valueOf(image.getHeight((ImageObserver)null)));

      try {
         this.handleHREF(image, imageElement, generatorContext);
      } catch (SVGGraphics2DIOException var7) {
         SVGGraphics2DIOException e = var7;

         try {
            generatorContext.errorHandler.handleError(e);
         } catch (SVGGraphics2DIOException var6) {
            throw new SVGGraphics2DRuntimeException(var6);
         }
      }

   }

   public void handleImage(RenderedImage image, Element imageElement, SVGGeneratorContext generatorContext) {
      imageElement.setAttributeNS((String)null, "width", String.valueOf(image.getWidth()));
      imageElement.setAttributeNS((String)null, "height", String.valueOf(image.getHeight()));

      try {
         this.handleHREF(image, imageElement, generatorContext);
      } catch (SVGGraphics2DIOException var7) {
         SVGGraphics2DIOException e = var7;

         try {
            generatorContext.errorHandler.handleError(e);
         } catch (SVGGraphics2DIOException var6) {
            throw new SVGGraphics2DRuntimeException(var6);
         }
      }

   }

   public void handleImage(RenderableImage image, Element imageElement, SVGGeneratorContext generatorContext) {
      imageElement.setAttributeNS((String)null, "width", String.valueOf(image.getWidth()));
      imageElement.setAttributeNS((String)null, "height", String.valueOf(image.getHeight()));

      try {
         this.handleHREF(image, imageElement, generatorContext);
      } catch (SVGGraphics2DIOException var7) {
         SVGGraphics2DIOException e = var7;

         try {
            generatorContext.errorHandler.handleError(e);
         } catch (SVGGraphics2DIOException var6) {
            throw new SVGGraphics2DRuntimeException(var6);
         }
      }

   }

   protected void handleHREF(Image image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", image.toString());
   }

   protected void handleHREF(RenderedImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", image.toString());
   }

   protected void handleHREF(RenderableImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", image.toString());
   }
}
