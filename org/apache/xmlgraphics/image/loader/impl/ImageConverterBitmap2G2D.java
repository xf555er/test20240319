package org.apache.xmlgraphics.image.loader.impl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.util.Map;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

public class ImageConverterBitmap2G2D extends AbstractImageConverter {
   public Image convert(Image src, Map hints) {
      this.checkSourceFlavor(src);

      assert src instanceof ImageRendered;

      ImageRendered rendImage = (ImageRendered)src;
      Graphics2DImagePainterImpl painter = new Graphics2DImagePainterImpl(rendImage);
      ImageGraphics2D g2dImage = new ImageGraphics2D(src.getInfo(), painter);
      return g2dImage;
   }

   public ImageFlavor getSourceFlavor() {
      return ImageFlavor.RENDERED_IMAGE;
   }

   public ImageFlavor getTargetFlavor() {
      return ImageFlavor.GRAPHICS2D;
   }

   static class Graphics2DImagePainterImpl implements Graphics2DImagePainter {
      ImageRendered rendImage;

      public Graphics2DImagePainterImpl(ImageRendered rendImage) {
         this.rendImage = rendImage;
      }

      public Dimension getImageSize() {
         return this.rendImage.getSize().getDimensionMpt();
      }

      public void paint(Graphics2D g2d, Rectangle2D area) {
         RenderedImage ri = this.rendImage.getRenderedImage();
         double w = area.getWidth();
         double h = area.getHeight();
         AffineTransform at = new AffineTransform();
         at.translate(area.getX(), area.getY());
         double sx = w / (double)ri.getWidth();
         double sy = h / (double)ri.getHeight();
         if (sx != 1.0 || sy != 1.0) {
            at.scale(sx, sy);
         }

         g2d.drawRenderedImage(ri, at);
      }
   }
}
