package org.apache.batik.svggen;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import org.w3c.dom.Element;

public abstract class DefaultCachedImageHandler implements CachedImageHandler, SVGSyntax, ErrorConstants {
   static final String XLINK_NAMESPACE_URI = "http://www.w3.org/1999/xlink";
   static final AffineTransform IDENTITY = new AffineTransform();
   private static Method createGraphics = null;
   private static boolean initDone = false;
   private static final Class[] paramc = new Class[]{BufferedImage.class};
   private static Object[] paramo = null;
   protected ImageCacher imageCacher;

   public ImageCacher getImageCacher() {
      return this.imageCacher;
   }

   void setImageCacher(ImageCacher imageCacher) {
      if (imageCacher == null) {
         throw new IllegalArgumentException();
      } else {
         DOMTreeManager dtm = null;
         if (this.imageCacher != null) {
            dtm = this.imageCacher.getDOMTreeManager();
         }

         this.imageCacher = imageCacher;
         if (dtm != null) {
            this.imageCacher.setDOMTreeManager(dtm);
         }

      }
   }

   public void setDOMTreeManager(DOMTreeManager domTreeManager) {
      this.imageCacher.setDOMTreeManager(domTreeManager);
   }

   private static Graphics2D createGraphics(BufferedImage buf) {
      if (!initDone) {
         try {
            Class clazz = Class.forName("org.apache.batik.ext.awt.image.GraphicsUtil");
            createGraphics = clazz.getMethod("createGraphics", paramc);
            paramo = new Object[1];
         } catch (Throwable var7) {
         } finally {
            initDone = true;
         }
      }

      if (createGraphics == null) {
         return buf.createGraphics();
      } else {
         paramo[0] = buf;
         Graphics2D g2d = null;

         try {
            g2d = (Graphics2D)createGraphics.invoke((Object)null, paramo);
         } catch (Exception var6) {
         }

         return g2d;
      }
   }

   public Element createElement(SVGGeneratorContext generatorContext) {
      Element imageElement = generatorContext.getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "image");
      return imageElement;
   }

   public AffineTransform handleImage(Image image, Element imageElement, int x, int y, int width, int height, SVGGeneratorContext generatorContext) {
      int imageWidth = image.getWidth((ImageObserver)null);
      int imageHeight = image.getHeight((ImageObserver)null);
      AffineTransform af = null;
      if (imageWidth != 0 && imageHeight != 0 && width != 0 && height != 0) {
         try {
            this.handleHREF(image, imageElement, generatorContext);
         } catch (SVGGraphics2DIOException var14) {
            SVGGraphics2DIOException e = var14;

            try {
               generatorContext.errorHandler.handleError(e);
            } catch (SVGGraphics2DIOException var13) {
               throw new SVGGraphics2DRuntimeException(var13);
            }
         }

         af = this.handleTransform(imageElement, (double)x, (double)y, (double)imageWidth, (double)imageHeight, (double)width, (double)height, generatorContext);
      } else {
         this.handleEmptyImage(imageElement);
      }

      return af;
   }

   public AffineTransform handleImage(RenderedImage image, Element imageElement, int x, int y, int width, int height, SVGGeneratorContext generatorContext) {
      int imageWidth = image.getWidth();
      int imageHeight = image.getHeight();
      AffineTransform af = null;
      if (imageWidth != 0 && imageHeight != 0 && width != 0 && height != 0) {
         try {
            this.handleHREF(image, imageElement, generatorContext);
         } catch (SVGGraphics2DIOException var14) {
            SVGGraphics2DIOException e = var14;

            try {
               generatorContext.errorHandler.handleError(e);
            } catch (SVGGraphics2DIOException var13) {
               throw new SVGGraphics2DRuntimeException(var13);
            }
         }

         af = this.handleTransform(imageElement, (double)x, (double)y, (double)imageWidth, (double)imageHeight, (double)width, (double)height, generatorContext);
      } else {
         this.handleEmptyImage(imageElement);
      }

      return af;
   }

   public AffineTransform handleImage(RenderableImage image, Element imageElement, double x, double y, double width, double height, SVGGeneratorContext generatorContext) {
      double imageWidth = (double)image.getWidth();
      double imageHeight = (double)image.getHeight();
      AffineTransform af = null;
      if (imageWidth != 0.0 && imageHeight != 0.0 && width != 0.0 && height != 0.0) {
         try {
            this.handleHREF(image, imageElement, generatorContext);
         } catch (SVGGraphics2DIOException var20) {
            SVGGraphics2DIOException e = var20;

            try {
               generatorContext.errorHandler.handleError(e);
            } catch (SVGGraphics2DIOException var19) {
               throw new SVGGraphics2DRuntimeException(var19);
            }
         }

         af = this.handleTransform(imageElement, x, y, imageWidth, imageHeight, width, height, generatorContext);
      } else {
         this.handleEmptyImage(imageElement);
      }

      return af;
   }

   protected AffineTransform handleTransform(Element imageElement, double x, double y, double srcWidth, double srcHeight, double dstWidth, double dstHeight, SVGGeneratorContext generatorContext) {
      imageElement.setAttributeNS((String)null, "x", generatorContext.doubleString(x));
      imageElement.setAttributeNS((String)null, "y", generatorContext.doubleString(y));
      imageElement.setAttributeNS((String)null, "width", generatorContext.doubleString(dstWidth));
      imageElement.setAttributeNS((String)null, "height", generatorContext.doubleString(dstHeight));
      return null;
   }

   protected void handleEmptyImage(Element imageElement) {
      imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "");
      imageElement.setAttributeNS((String)null, "width", "0");
      imageElement.setAttributeNS((String)null, "height", "0");
   }

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
               BufferedImage buf = this.buildBufferedImage(new Dimension(width, height));
               Graphics2D g = createGraphics(buf);
               g.drawImage(image, 0, 0, (ImageObserver)null);
               g.dispose();
               this.handleHREF((RenderedImage)buf, imageElement, generatorContext);
            }
         } else {
            this.handleEmptyImage(imageElement);
         }

      }
   }

   public BufferedImage buildBufferedImage(Dimension size) {
      return new BufferedImage(size.width, size.height, this.getBufferedImageType());
   }

   protected void handleHREF(RenderedImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      BufferedImage buf = null;
      if (image instanceof BufferedImage && ((BufferedImage)image).getType() == this.getBufferedImageType()) {
         buf = (BufferedImage)image;
      } else {
         Dimension size = new Dimension(image.getWidth(), image.getHeight());
         buf = this.buildBufferedImage(size);
         Graphics2D g = createGraphics(buf);
         g.drawRenderedImage(image, IDENTITY);
         g.dispose();
      }

      this.cacheBufferedImage(imageElement, buf, generatorContext);
   }

   protected void handleHREF(RenderableImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      Dimension size = new Dimension((int)Math.ceil((double)image.getWidth()), (int)Math.ceil((double)image.getHeight()));
      BufferedImage buf = this.buildBufferedImage(size);
      Graphics2D g = createGraphics(buf);
      g.drawRenderableImage(image, IDENTITY);
      g.dispose();
      this.handleHREF((RenderedImage)buf, imageElement, generatorContext);
   }

   protected void cacheBufferedImage(Element imageElement, BufferedImage buf, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      if (generatorContext == null) {
         throw new SVGGraphics2DRuntimeException("generatorContext should not be null");
      } else {
         ByteArrayOutputStream os;
         try {
            os = new ByteArrayOutputStream();
            this.encodeImage(buf, os);
            os.flush();
            os.close();
         } catch (IOException var6) {
            throw new SVGGraphics2DIOException("unexpected exception", var6);
         }

         String ref = this.imageCacher.lookup(os, buf.getWidth(), buf.getHeight(), generatorContext);
         imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", this.getRefPrefix() + ref);
      }
   }

   public abstract String getRefPrefix();

   public abstract void encodeImage(BufferedImage var1, OutputStream var2) throws IOException;

   public abstract int getBufferedImageType();
}
