package org.apache.batik.ext.awt.image.renderable;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class AffineRable8Bit extends AbstractRable implements AffineRable, PaintRable {
   AffineTransform affine;
   AffineTransform invAffine;

   public AffineRable8Bit(Filter src, AffineTransform affine) {
      this.init(src);
      this.setAffine(affine);
   }

   public Rectangle2D getBounds2D() {
      Filter src = this.getSource();
      Rectangle2D r = src.getBounds2D();
      return this.affine.createTransformedShape(r).getBounds2D();
   }

   public Filter getSource() {
      return (Filter)this.srcs.get(0);
   }

   public void setSource(Filter src) {
      this.init(src);
   }

   public void setAffine(AffineTransform affine) {
      this.touch();
      this.affine = affine;

      try {
         this.invAffine = affine.createInverse();
      } catch (NoninvertibleTransformException var3) {
         this.invAffine = null;
      }

   }

   public AffineTransform getAffine() {
      return (AffineTransform)this.affine.clone();
   }

   public boolean paintRable(Graphics2D g2d) {
      AffineTransform at = g2d.getTransform();
      g2d.transform(this.getAffine());
      GraphicsUtil.drawImage(g2d, (RenderableImage)this.getSource());
      g2d.setTransform(at);
      return true;
   }

   public RenderedImage createRendering(RenderContext rc) {
      if (this.invAffine == null) {
         return null;
      } else {
         RenderingHints rh = rc.getRenderingHints();
         if (rh == null) {
            rh = new RenderingHints((Map)null);
         }

         Shape aoi = rc.getAreaOfInterest();
         if (aoi != null) {
            aoi = this.invAffine.createTransformedShape(aoi);
         }

         AffineTransform at = rc.getTransform();
         at.concatenate(this.affine);
         return this.getSource().createRendering(new RenderContext(at, aoi, rh));
      }
   }

   public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn) {
      if (srcIndex != 0) {
         throw new IndexOutOfBoundsException("Affine only has one input");
      } else {
         return this.invAffine == null ? null : this.invAffine.createTransformedShape(outputRgn);
      }
   }

   public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn) {
      if (srcIndex != 0) {
         throw new IndexOutOfBoundsException("Affine only has one input");
      } else {
         return this.affine.createTransformedShape(inputRgn);
      }
   }
}
