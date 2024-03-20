package org.apache.batik.ext.awt.image.renderable;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.ARGBChannel;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.DisplacementMapRed;

public class DisplacementMapRable8Bit extends AbstractColorInterpolationRable implements DisplacementMapRable {
   private double scale;
   private ARGBChannel xChannelSelector;
   private ARGBChannel yChannelSelector;

   public DisplacementMapRable8Bit(List sources, double scale, ARGBChannel xChannelSelector, ARGBChannel yChannelSelector) {
      this.setSources(sources);
      this.setScale(scale);
      this.setXChannelSelector(xChannelSelector);
      this.setYChannelSelector(yChannelSelector);
   }

   public Rectangle2D getBounds2D() {
      return ((Filter)((Filter)this.getSources().get(0))).getBounds2D();
   }

   public void setScale(double scale) {
      this.touch();
      this.scale = scale;
   }

   public double getScale() {
      return this.scale;
   }

   public void setSources(List sources) {
      if (sources.size() != 2) {
         throw new IllegalArgumentException();
      } else {
         this.init(sources, (Map)null);
      }
   }

   public void setXChannelSelector(ARGBChannel xChannelSelector) {
      if (xChannelSelector == null) {
         throw new IllegalArgumentException();
      } else {
         this.touch();
         this.xChannelSelector = xChannelSelector;
      }
   }

   public ARGBChannel getXChannelSelector() {
      return this.xChannelSelector;
   }

   public void setYChannelSelector(ARGBChannel yChannelSelector) {
      if (yChannelSelector == null) {
         throw new IllegalArgumentException();
      } else {
         this.touch();
         this.yChannelSelector = yChannelSelector;
      }
   }

   public ARGBChannel getYChannelSelector() {
      return this.yChannelSelector;
   }

   public RenderedImage createRendering(RenderContext rc) {
      Filter displaced = (Filter)this.getSources().get(0);
      Filter map = (Filter)this.getSources().get(1);
      RenderingHints rh = rc.getRenderingHints();
      if (rh == null) {
         rh = new RenderingHints((Map)null);
      }

      AffineTransform at = rc.getTransform();
      double sx = at.getScaleX();
      double sy = at.getScaleY();
      double shx = at.getShearX();
      double shy = at.getShearY();
      double tx = at.getTranslateX();
      double ty = at.getTranslateY();
      double atScaleX = Math.sqrt(sx * sx + shy * shy);
      double atScaleY = Math.sqrt(sy * sy + shx * shx);
      float scaleX = (float)(this.scale * atScaleX);
      float scaleY = (float)(this.scale * atScaleY);
      if (scaleX == 0.0F && scaleY == 0.0F) {
         return displaced.createRendering(rc);
      } else {
         AffineTransform srcAt = AffineTransform.getScaleInstance(atScaleX, atScaleY);
         Shape origAOI = rc.getAreaOfInterest();
         if (origAOI == null) {
            origAOI = this.getBounds2D();
         }

         Rectangle2D aoiR = ((Shape)origAOI).getBounds2D();
         RenderContext srcRc = new RenderContext(srcAt, aoiR, rh);
         RenderedImage mapRed = map.createRendering(srcRc);
         if (mapRed == null) {
            return null;
         } else {
            Rectangle2D aoiR = new Rectangle2D.Double(aoiR.getX() - this.scale / 2.0, aoiR.getY() - this.scale / 2.0, aoiR.getWidth() + this.scale, aoiR.getHeight() + this.scale);
            Rectangle2D displacedRect = displaced.getBounds2D();
            if (!aoiR.intersects(displacedRect)) {
               return null;
            } else {
               aoiR = aoiR.createIntersection(displacedRect);
               srcRc = new RenderContext(srcAt, aoiR, rh);
               RenderedImage displacedRed = displaced.createRendering(srcRc);
               if (displacedRed == null) {
                  return null;
               } else {
                  RenderedImage mapRed = this.convertSourceCS(mapRed);
                  CachableRed cr = new DisplacementMapRed(GraphicsUtil.wrap(displacedRed), GraphicsUtil.wrap(mapRed), this.xChannelSelector, this.yChannelSelector, scaleX, scaleY, rh);
                  AffineTransform resAt = new AffineTransform(sx / atScaleX, shy / atScaleX, shx / atScaleY, sy / atScaleY, tx, ty);
                  if (!resAt.isIdentity()) {
                     cr = new AffineRed((CachableRed)cr, resAt, rh);
                  }

                  return (RenderedImage)cr;
               }
            }
         }
      }
   }

   public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn) {
      return super.getDependencyRegion(srcIndex, outputRgn);
   }

   public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn) {
      return super.getDirtyRegion(srcIndex, inputRgn);
   }
}
