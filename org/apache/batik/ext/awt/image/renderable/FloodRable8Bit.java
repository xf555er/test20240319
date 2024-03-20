package org.apache.batik.ext.awt.image.renderable;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.FloodRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;

public class FloodRable8Bit extends AbstractRable implements FloodRable {
   Paint floodPaint;
   Rectangle2D floodRegion;

   public FloodRable8Bit(Rectangle2D floodRegion, Paint floodPaint) {
      this.setFloodPaint(floodPaint);
      this.setFloodRegion(floodRegion);
   }

   public void setFloodPaint(Paint paint) {
      this.touch();
      if (paint == null) {
         this.floodPaint = new Color(0, 0, 0, 0);
      } else {
         this.floodPaint = paint;
      }

   }

   public Paint getFloodPaint() {
      return this.floodPaint;
   }

   public Rectangle2D getBounds2D() {
      return (Rectangle2D)this.floodRegion.clone();
   }

   public Rectangle2D getFloodRegion() {
      return (Rectangle2D)this.floodRegion.clone();
   }

   public void setFloodRegion(Rectangle2D floodRegion) {
      if (floodRegion == null) {
         throw new IllegalArgumentException();
      } else {
         this.touch();
         this.floodRegion = floodRegion;
      }
   }

   public RenderedImage createRendering(RenderContext rc) {
      AffineTransform usr2dev = rc.getTransform();
      if (usr2dev == null) {
         usr2dev = new AffineTransform();
      }

      Rectangle2D imageRect = this.getBounds2D();
      Shape aoi = rc.getAreaOfInterest();
      Rectangle2D userAOI;
      if (aoi == null) {
         userAOI = imageRect;
      } else {
         userAOI = aoi.getBounds2D();
         if (!imageRect.intersects(userAOI)) {
            return null;
         }

         Rectangle2D.intersect(imageRect, userAOI, userAOI);
      }

      Rectangle renderedArea = usr2dev.createTransformedShape(userAOI).getBounds();
      if (renderedArea.width > 0 && renderedArea.height > 0) {
         CachableRed cr = new FloodRed(renderedArea, this.getFloodPaint());
         CachableRed cr = new PadRed(cr, renderedArea, PadMode.ZERO_PAD, (RenderingHints)null);
         return cr;
      } else {
         return null;
      }
   }
}
