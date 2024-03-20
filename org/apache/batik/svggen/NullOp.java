package org.apache.batik.svggen;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.util.Hashtable;

class NullOp implements BufferedImageOp {
   public BufferedImage filter(BufferedImage src, BufferedImage dest) {
      Graphics2D g = dest.createGraphics();
      g.drawImage(src, 0, 0, (ImageObserver)null);
      g.dispose();
      return dest;
   }

   public Rectangle2D getBounds2D(BufferedImage src) {
      return new Rectangle(0, 0, src.getWidth(), src.getHeight());
   }

   public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
      BufferedImage dest = null;
      if (destCM == null) {
         destCM = src.getColorModel();
      }

      dest = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), destCM.isAlphaPremultiplied(), (Hashtable)null);
      return dest;
   }

   public Point2D getPoint2D(Point2D srcPt, Point2D destPt) {
      if (destPt == null) {
         destPt = new Point2D.Double();
      }

      ((Point2D)destPt).setLocation(srcPt.getX(), srcPt.getY());
      return (Point2D)destPt;
   }

   public RenderingHints getRenderingHints() {
      return null;
   }
}
