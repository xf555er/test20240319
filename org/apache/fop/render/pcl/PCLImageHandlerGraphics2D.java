package org.apache.fop.render.pcl;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerUtil;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.ImageGraphics2D;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.util.UnitConv;

public class PCLImageHandlerGraphics2D implements ImageHandler {
   private static Log log = LogFactory.getLog(PCLImageHandlerGraphics2D.class);

   public int getPriority() {
      return 400;
   }

   public Class getSupportedImageClass() {
      return ImageGraphics2D.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return new ImageFlavor[]{ImageFlavor.GRAPHICS2D};
   }

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      PCLRenderingContext pclContext = (PCLRenderingContext)context;
      ImageGraphics2D imageG2D = (ImageGraphics2D)image;
      Dimension imageDim = imageG2D.getSize().getDimensionMpt();
      PCLGenerator gen = pclContext.getPCLGenerator();
      Point2D transPoint = pclContext.transformedPoint(pos.x, pos.y);
      gen.setCursorPos(transPoint.getX(), transPoint.getY());
      boolean painted = false;
      ByteArrayOutputStream baout = new ByteArrayOutputStream();
      PCLGenerator tempGen = new PCLGenerator(baout, gen.getMaximumBitmapResolution());
      tempGen.setDitheringQuality(gen.getDitheringQuality());

      try {
         GraphicContext ctx = (GraphicContext)pclContext.getGraphicContext().clone();
         AffineTransform prepareHPGL2 = new AffineTransform();
         prepareHPGL2.scale(0.001, 0.001);
         int direction = PCLRenderingUtil.determinePrintDirection(ctx.getTransform());
         this.rotate(prepareHPGL2, imageDim, direction);
         int height = pos.height;
         int width = pos.width;
         if (direction == 90 || direction == 270) {
            int tmp = height;
            height = width;
            width = tmp;
         }

         ctx.setTransform(prepareHPGL2);
         PCLGraphics2D graphics = new PCLGraphics2D(tempGen);
         graphics.setGraphicContext(ctx);
         graphics.setClippingDisabled(false);
         Rectangle2D area = new Rectangle2D.Double(0.0, 0.0, imageDim.getWidth(), imageDim.getHeight());
         imageG2D.getGraphics2DImagePainter().paint(graphics, area);
         gen.writeCommand("*c" + gen.formatDouble4((double)((float)width / 100.0F)) + "x" + gen.formatDouble4((double)((float)height / 100.0F)) + "Y");
         gen.writeCommand("*c0T");
         gen.enterHPGL2Mode(false);
         gen.writeText("\nIN;");
         gen.writeText("SP1;");
         double scale = imageDim.getWidth() / UnitConv.mm2pt(imageDim.getWidth() * 0.025);
         gen.writeText("SC0," + gen.formatDouble4(scale) + ",0,-" + gen.formatDouble4(scale) + ",2;");
         gen.writeText("IR0,100,0,100;");
         gen.writeText("PU;PA0,0;\n");
         baout.writeTo(gen.getOutputStream());
         gen.writeText("\n");
         gen.enterPCLMode(false);
         painted = true;
      } catch (UnsupportedOperationException var22) {
         log.debug("Cannot paint graphic natively. Falling back to bitmap painting. Reason: " + var22.getMessage());
      }

      if (!painted) {
         FOUserAgent ua = context.getUserAgent();
         ImageManager imageManager = ua.getImageManager();

         ImageRendered imgRend;
         try {
            imgRend = (ImageRendered)imageManager.convertImage(imageG2D, new ImageFlavor[]{ImageFlavor.RENDERED_IMAGE});
         } catch (ImageException var21) {
            throw new IOException("Image conversion error while converting the image to a bitmap as a fallback measure: " + var21.getMessage());
         }

         gen.paintBitmap(imgRend.getRenderedImage(), new Dimension(pos.width, pos.height), pclContext.isSourceTransparencyEnabled(), pclContext.getPCLUtil());
      }

   }

   private void rotate(AffineTransform prepareHPGL2, Dimension imageDim, int direction) {
      if (direction != 0) {
         double rads = Math.toRadians((double)(-direction));
         double sin = Math.abs(Math.sin(rads));
         double cos = Math.abs(Math.cos(rads));
         double w = Math.floor(imageDim.getWidth() * cos + imageDim.getHeight() * sin);
         double h = Math.floor(imageDim.getHeight() * cos + imageDim.getWidth() * sin);
         prepareHPGL2.translate(w / 2.0, h / 2.0);
         prepareHPGL2.rotate(rads, 0.0, 0.0);
         prepareHPGL2.translate(-imageDim.getWidth() / 2.0, -imageDim.getHeight() / 2.0);
      }

   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      boolean supported = (image == null || image instanceof ImageGraphics2D) && targetContext instanceof PCLRenderingContext;
      if (supported) {
         String mode = (String)targetContext.getHint(ImageHandlerUtil.CONVERSION_MODE);
         if (ImageHandlerUtil.isConversionModeBitmap(mode)) {
            return false;
         }
      }

      return supported;
   }
}
