package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class ProxyGraphicsNode extends AbstractGraphicsNode {
   protected GraphicsNode source;

   public void setSource(GraphicsNode source) {
      this.source = source;
   }

   public GraphicsNode getSource() {
      return this.source;
   }

   public void primitivePaint(Graphics2D g2d) {
      if (this.source != null) {
         this.source.paint(g2d);
      }

   }

   public Rectangle2D getPrimitiveBounds() {
      return this.source == null ? null : this.source.getBounds();
   }

   public Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf) {
      if (this.source == null) {
         return null;
      } else {
         AffineTransform t = txf;
         if (this.transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(this.transform);
         }

         return this.source.getTransformedPrimitiveBounds(t);
      }
   }

   public Rectangle2D getGeometryBounds() {
      return this.source == null ? null : this.source.getGeometryBounds();
   }

   public Rectangle2D getTransformedGeometryBounds(AffineTransform txf) {
      if (this.source == null) {
         return null;
      } else {
         AffineTransform t = txf;
         if (this.transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(this.transform);
         }

         return this.source.getTransformedGeometryBounds(t);
      }
   }

   public Rectangle2D getSensitiveBounds() {
      return this.source == null ? null : this.source.getSensitiveBounds();
   }

   public Shape getOutline() {
      return this.source == null ? null : this.source.getOutline();
   }
}
