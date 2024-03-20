package org.apache.batik.svggen;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import org.w3c.dom.Element;

public class SimpleImageHandler implements GenericImageHandler, SVGSyntax, ErrorConstants {
   static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
   protected ImageHandler imageHandler;

   public SimpleImageHandler(ImageHandler imageHandler) {
      if (imageHandler == null) {
         throw new IllegalArgumentException();
      } else {
         this.imageHandler = imageHandler;
      }
   }

   public void setDOMTreeManager(DOMTreeManager domTreeManager) {
   }

   public Element createElement(SVGGeneratorContext generatorContext) {
      Element imageElement = generatorContext.getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "image");
      return imageElement;
   }

   public AffineTransform handleImage(Image image, Element imageElement, int x, int y, int width, int height, SVGGeneratorContext generatorContext) {
      int imageWidth = image.getWidth((ImageObserver)null);
      int imageHeight = image.getHeight((ImageObserver)null);
      if (imageWidth != 0 && imageHeight != 0 && width != 0 && height != 0) {
         this.imageHandler.handleImage(image, imageElement, generatorContext);
         this.setImageAttributes(imageElement, (double)x, (double)y, (double)width, (double)height, generatorContext);
      } else {
         this.handleEmptyImage(imageElement);
      }

      return null;
   }

   public AffineTransform handleImage(RenderedImage image, Element imageElement, int x, int y, int width, int height, SVGGeneratorContext generatorContext) {
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      if (imageWidth != 0 && imageHeight != 0 && width != 0 && height != 0) {
         this.imageHandler.handleImage(image, imageElement, generatorContext);
         this.setImageAttributes(imageElement, (double)x, (double)y, (double)width, (double)height, generatorContext);
      } else {
         this.handleEmptyImage(imageElement);
      }

      return null;
   }

   public AffineTransform handleImage(RenderableImage image, Element imageElement, double x, double y, double width, double height, SVGGeneratorContext generatorContext) {
      double imageWidth = (double)image.getWidth();
      double imageHeight = (double)image.getHeight();
      if (imageWidth != 0.0 && imageHeight != 0.0 && width != 0.0 && height != 0.0) {
         this.imageHandler.handleImage(image, imageElement, generatorContext);
         this.setImageAttributes(imageElement, x, y, width, height, generatorContext);
      } else {
         this.handleEmptyImage(imageElement);
      }

      return null;
   }

   protected void setImageAttributes(Element imageElement, double x, double y, double width, double height, SVGGeneratorContext generatorContext) {
      imageElement.setAttributeNS((String)null, "x", generatorContext.doubleString(x));
      imageElement.setAttributeNS((String)null, "y", generatorContext.doubleString(y));
      imageElement.setAttributeNS((String)null, "width", generatorContext.doubleString(width));
      imageElement.setAttributeNS((String)null, "height", generatorContext.doubleString(height));
      imageElement.setAttributeNS((String)null, "preserveAspectRatio", "none");
   }

   protected void handleEmptyImage(Element imageElement) {
      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "");
      imageElement.setAttributeNS((String)null, "width", "0");
      imageElement.setAttributeNS((String)null, "height", "0");
   }
}
