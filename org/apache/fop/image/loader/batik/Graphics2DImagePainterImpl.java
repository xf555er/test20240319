package org.apache.fop.image.loader.batik;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.xmlgraphics.java2d.AbstractGraphics2D;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

public class Graphics2DImagePainterImpl implements Graphics2DImagePainter {
   private final GraphicsNode root;
   protected final BridgeContext ctx;
   protected final Dimension imageSize;

   public Graphics2DImagePainterImpl(GraphicsNode root, BridgeContext ctx, Dimension imageSize) {
      this.root = root;
      this.imageSize = imageSize;
      this.ctx = ctx;
   }

   public Dimension getImageSize() {
      return this.imageSize;
   }

   private void prepare(Graphics2D g2d, Rectangle2D area) {
      double tx = area.getX();
      double ty = area.getY();
      if (tx != 0.0 || ty != 0.0) {
         g2d.translate(tx, ty);
      }

      float iw = (float)this.ctx.getDocumentSize().getWidth();
      float ih = (float)this.ctx.getDocumentSize().getHeight();
      float w = (float)area.getWidth();
      float h = (float)area.getHeight();
      float sx = w / iw;
      float sy = h / ih;
      if ((double)sx != 1.0 || (double)sy != 1.0) {
         g2d.scale((double)sx, (double)sy);
      }

      this.normaliseScale(g2d);
   }

   private void normaliseScale(Graphics2D g2d) {
      if (!(g2d instanceof AbstractGraphics2D)) {
         AffineTransform old = g2d.getTransform();
         double scaleX = BigDecimal.valueOf(old.getScaleX()).setScale(2, RoundingMode.HALF_UP).doubleValue();
         double scaleY = BigDecimal.valueOf(old.getScaleY()).setScale(2, RoundingMode.HALF_UP).doubleValue();
         AffineTransform newat = new AffineTransform(scaleX, old.getShearY(), old.getShearX(), scaleY, old.getTranslateX(), old.getTranslateY());
         g2d.setTransform(newat);
      }

   }

   public void paint(Graphics2D g2d, Rectangle2D area) {
      this.prepare(g2d, area);
      this.root.paint(g2d);
   }
}
