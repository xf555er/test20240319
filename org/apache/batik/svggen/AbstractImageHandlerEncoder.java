package org.apache.batik.svggen;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import org.w3c.dom.Element;

public abstract class AbstractImageHandlerEncoder extends DefaultImageHandler {
   private static final AffineTransform IDENTITY = new AffineTransform();
   private String imageDir = "";
   private String urlRoot = "";
   private static Method createGraphics = null;
   private static boolean initDone = false;
   private static final Class[] paramc = new Class[]{BufferedImage.class};
   private static Object[] paramo = null;

   private static Graphics2D createGraphics(BufferedImage buf) {
      if (!initDone) {
         try {
            Class clazz = Class.forName("org.apache.batik.ext.awt.image.GraphicsUtil");
            createGraphics = clazz.getMethod("createGraphics", paramc);
            paramo = new Object[1];
         } catch (ThreadDeath var8) {
            throw var8;
         } catch (Throwable var9) {
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
         } catch (Exception var7) {
         }

         return g2d;
      }
   }

   public AbstractImageHandlerEncoder(String imageDir, String urlRoot) throws SVGGraphics2DIOException {
      if (imageDir == null) {
         throw new SVGGraphics2DRuntimeException("imageDir should not be null");
      } else {
         File imageDirFile = new File(imageDir);
         if (!imageDirFile.exists()) {
            throw new SVGGraphics2DRuntimeException("imageDir does not exist");
         } else {
            this.imageDir = imageDir;
            if (urlRoot != null) {
               this.urlRoot = urlRoot;
            } else {
               try {
                  this.urlRoot = imageDirFile.toURI().toURL().toString();
               } catch (MalformedURLException var5) {
                  throw new SVGGraphics2DIOException("cannot convert imageDir to a URL value : " + var5.getMessage(), var5);
               }
            }

         }
      }
   }

   protected void handleHREF(Image image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      Dimension size = new Dimension(image.getWidth((ImageObserver)null), image.getHeight((ImageObserver)null));
      BufferedImage buf = this.buildBufferedImage(size);
      Graphics2D g = createGraphics(buf);
      g.drawImage(image, 0, 0, (ImageObserver)null);
      g.dispose();
      this.saveBufferedImageToFile(imageElement, buf, generatorContext);
   }

   protected void handleHREF(RenderedImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      Dimension size = new Dimension(image.getWidth(), image.getHeight());
      BufferedImage buf = this.buildBufferedImage(size);
      Graphics2D g = createGraphics(buf);
      g.drawRenderedImage(image, IDENTITY);
      g.dispose();
      this.saveBufferedImageToFile(imageElement, buf, generatorContext);
   }

   protected void handleHREF(RenderableImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      Dimension size = new Dimension((int)Math.ceil((double)image.getWidth()), (int)Math.ceil((double)image.getHeight()));
      BufferedImage buf = this.buildBufferedImage(size);
      Graphics2D g = createGraphics(buf);
      g.drawRenderableImage(image, IDENTITY);
      g.dispose();
      this.saveBufferedImageToFile(imageElement, buf, generatorContext);
   }

   private void saveBufferedImageToFile(Element imageElement, BufferedImage buf, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
      if (generatorContext == null) {
         throw new SVGGraphics2DRuntimeException("generatorContext should not be null");
      } else {
         File imageFile = null;

         while(imageFile == null) {
            String fileId = generatorContext.idGenerator.generateID(this.getPrefix());
            imageFile = new File(this.imageDir, fileId + this.getSuffix());
            if (imageFile.exists()) {
               imageFile = null;
            }
         }

         this.encodeImage(buf, imageFile);
         imageElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", this.urlRoot + "/" + imageFile.getName());
      }
   }

   public abstract String getSuffix();

   public abstract String getPrefix();

   public abstract void encodeImage(BufferedImage var1, File var2) throws SVGGraphics2DIOException;

   public abstract BufferedImage buildBufferedImage(Dimension var1);
}
