package org.apache.batik.gvt.filter;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.renderable.AbstractRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PaintRable;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.TranslateRed;
import org.apache.batik.gvt.GraphicsNode;

public class GraphicsNodeRable8Bit extends AbstractRable implements GraphicsNodeRable, PaintRable {
   private AffineTransform cachedGn2dev = null;
   private AffineTransform cachedUsr2dev = null;
   private CachableRed cachedRed = null;
   private Rectangle2D cachedBounds = null;
   private boolean usePrimitivePaint = true;
   private GraphicsNode node;

   public boolean getUsePrimitivePaint() {
      return this.usePrimitivePaint;
   }

   public void setUsePrimitivePaint(boolean usePrimitivePaint) {
      this.usePrimitivePaint = usePrimitivePaint;
   }

   public GraphicsNode getGraphicsNode() {
      return this.node;
   }

   public void setGraphicsNode(GraphicsNode node) {
      if (node == null) {
         throw new IllegalArgumentException();
      } else {
         this.node = node;
      }
   }

   public void clearCache() {
      this.cachedRed = null;
      this.cachedUsr2dev = null;
      this.cachedGn2dev = null;
      this.cachedBounds = null;
   }

   public GraphicsNodeRable8Bit(GraphicsNode node) {
      if (node == null) {
         throw new IllegalArgumentException();
      } else {
         this.node = node;
         this.usePrimitivePaint = true;
      }
   }

   public GraphicsNodeRable8Bit(GraphicsNode node, Map props) {
      super((Filter)null, props);
      if (node == null) {
         throw new IllegalArgumentException();
      } else {
         this.node = node;
         this.usePrimitivePaint = true;
      }
   }

   public GraphicsNodeRable8Bit(GraphicsNode node, boolean usePrimitivePaint) {
      if (node == null) {
         throw new IllegalArgumentException();
      } else {
         this.node = node;
         this.usePrimitivePaint = usePrimitivePaint;
      }
   }

   public Rectangle2D getBounds2D() {
      Rectangle2D bounds;
      if (this.usePrimitivePaint) {
         bounds = this.node.getPrimitiveBounds();
         return (Rectangle2D)(bounds == null ? new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0) : (Rectangle2D)((Rectangle2D)bounds.clone()));
      } else {
         bounds = this.node.getBounds();
         if (bounds == null) {
            return new Rectangle2D.Double(0.0, 0.0, 0.0, 0.0);
         } else {
            AffineTransform at = this.node.getTransform();
            if (at != null) {
               bounds = at.createTransformedShape(bounds).getBounds2D();
            }

            return bounds;
         }
      }
   }

   public boolean isDynamic() {
      return false;
   }

   public boolean paintRable(Graphics2D g2d) {
      Composite c = g2d.getComposite();
      if (!SVGComposite.OVER.equals(c)) {
         return false;
      } else {
         ColorSpace g2dCS = GraphicsUtil.getDestinationColorSpace(g2d);
         if (g2dCS != null && g2dCS == ColorSpace.getInstance(1000)) {
            GraphicsNode gn = this.getGraphicsNode();
            if (this.getUsePrimitivePaint()) {
               gn.primitivePaint(g2d);
            } else {
               gn.paint(g2d);
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public RenderedImage createRendering(RenderContext renderContext) {
      AffineTransform usr2dev = renderContext.getTransform();
      AffineTransform gn2dev;
      if (usr2dev == null) {
         usr2dev = new AffineTransform();
         gn2dev = usr2dev;
      } else {
         gn2dev = (AffineTransform)usr2dev.clone();
      }

      AffineTransform gn2usr = this.node.getTransform();
      if (gn2usr != null) {
         gn2dev.concatenate(gn2usr);
      }

      Rectangle2D bounds2D = this.getBounds2D();
      if (this.cachedBounds != null && this.cachedGn2dev != null && this.cachedBounds.equals(bounds2D) && gn2dev.getScaleX() == this.cachedGn2dev.getScaleX() && gn2dev.getScaleY() == this.cachedGn2dev.getScaleY() && gn2dev.getShearX() == this.cachedGn2dev.getShearX() && gn2dev.getShearY() == this.cachedGn2dev.getShearY()) {
         double deltaX = usr2dev.getTranslateX() - this.cachedUsr2dev.getTranslateX();
         double deltaY = usr2dev.getTranslateY() - this.cachedUsr2dev.getTranslateY();
         if (deltaX == 0.0 && deltaY == 0.0) {
            return this.cachedRed;
         }

         if (deltaX == (double)((int)deltaX) && deltaY == (double)((int)deltaY)) {
            return new TranslateRed(this.cachedRed, (int)Math.round((double)this.cachedRed.getMinX() + deltaX), (int)Math.round((double)this.cachedRed.getMinY() + deltaY));
         }
      }

      if (bounds2D.getWidth() > 0.0 && bounds2D.getHeight() > 0.0) {
         this.cachedUsr2dev = (AffineTransform)usr2dev.clone();
         this.cachedGn2dev = gn2dev;
         this.cachedBounds = bounds2D;
         this.cachedRed = new GraphicsNodeRed8Bit(this.node, usr2dev, this.usePrimitivePaint, renderContext.getRenderingHints());
         return this.cachedRed;
      } else {
         this.cachedUsr2dev = null;
         this.cachedGn2dev = null;
         this.cachedBounds = null;
         this.cachedRed = null;
         return null;
      }
   }
}
