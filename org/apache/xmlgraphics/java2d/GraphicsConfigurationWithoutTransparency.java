package org.apache.xmlgraphics.java2d;

import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GraphicsConfigurationWithoutTransparency extends AbstractGraphicsConfiguration {
   private static final Log LOG = LogFactory.getLog(GraphicsConfigurationWithoutTransparency.class);
   private static final BufferedImage BI_WITHOUT_ALPHA = new BufferedImage(1, 1, 1);
   private final GraphicsConfigurationWithTransparency defaultDelegate = new GraphicsConfigurationWithTransparency();

   public GraphicsDevice getDevice() {
      return new GenericGraphicsDevice(this);
   }

   public BufferedImage createCompatibleImage(int width, int height) {
      return this.defaultDelegate.createCompatibleImage(width, height, 1);
   }

   public BufferedImage createCompatibleImage(int width, int height, int transparency) {
      if (transparency != 1) {
         LOG.warn("Does not support transparencies (alpha channels) in images");
      }

      return this.defaultDelegate.createCompatibleImage(width, height, 1);
   }

   public ColorModel getColorModel() {
      return BI_WITHOUT_ALPHA.getColorModel();
   }

   public ColorModel getColorModel(int transparency) {
      if (transparency == 1) {
         LOG.warn("Does not support transparencies (alpha channels) in images");
      }

      return this.getColorModel();
   }

   public AffineTransform getDefaultTransform() {
      return this.defaultDelegate.getDefaultTransform();
   }

   public AffineTransform getNormalizingTransform() {
      return this.defaultDelegate.getNormalizingTransform();
   }

   public Rectangle getBounds() {
      return new Rectangle();
   }
}
