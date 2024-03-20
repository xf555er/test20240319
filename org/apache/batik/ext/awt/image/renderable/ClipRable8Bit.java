package org.apache.batik.ext.awt.image.renderable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.MultiplyAlphaRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;

public class ClipRable8Bit extends AbstractRable implements ClipRable {
   protected boolean useAA;
   protected Shape clipPath;

   public ClipRable8Bit(Filter src, Shape clipPath) {
      super((Filter)src, (Map)null);
      this.setClipPath(clipPath);
      this.setUseAntialiasedClip(false);
   }

   public ClipRable8Bit(Filter src, Shape clipPath, boolean useAA) {
      super((Filter)src, (Map)null);
      this.setClipPath(clipPath);
      this.setUseAntialiasedClip(useAA);
   }

   public void setSource(Filter src) {
      this.init(src, (Map)null);
   }

   public Filter getSource() {
      return (Filter)this.getSources().get(0);
   }

   public void setUseAntialiasedClip(boolean useAA) {
      this.touch();
      this.useAA = useAA;
   }

   public boolean getUseAntialiasedClip() {
      return this.useAA;
   }

   public void setClipPath(Shape clipPath) {
      this.touch();
      this.clipPath = clipPath;
   }

   public Shape getClipPath() {
      return this.clipPath;
   }

   public Rectangle2D getBounds2D() {
      return this.getSource().getBounds2D();
   }

   public RenderedImage createRendering(RenderContext rc) {
      AffineTransform usr2dev = rc.getTransform();
      RenderingHints rh = rc.getRenderingHints();
      if (rh == null) {
         rh = new RenderingHints((Map)null);
      }

      Shape aoi = rc.getAreaOfInterest();
      if (aoi == null) {
         aoi = this.getBounds2D();
      }

      Rectangle2D rect = this.getBounds2D();
      Rectangle2D clipRect = this.clipPath.getBounds2D();
      Rectangle2D aoiRect = ((Shape)aoi).getBounds2D();
      if (!rect.intersects(clipRect)) {
         return null;
      } else {
         Rectangle2D.intersect(rect, clipRect, rect);
         if (!rect.intersects(aoiRect)) {
            return null;
         } else {
            Rectangle2D.intersect(rect, ((Shape)aoi).getBounds2D(), rect);
            Rectangle devR = usr2dev.createTransformedShape(rect).getBounds();
            if (devR.width != 0 && devR.height != 0) {
               BufferedImage bi = new BufferedImage(devR.width, devR.height, 10);
               Shape devShape = usr2dev.createTransformedShape(this.getClipPath());
               Rectangle devAOIR = usr2dev.createTransformedShape((Shape)aoi).getBounds();
               Graphics2D g2d = GraphicsUtil.createGraphics(bi, rh);
               g2d.translate(-devR.x, -devR.y);
               g2d.setPaint(Color.white);
               g2d.fill(devShape);
               g2d.dispose();
               RenderedImage ri = this.getSource().createRendering(new RenderContext(usr2dev, rect, rh));
               CachableRed cr = RenderedImageCachableRed.wrap(ri);
               CachableRed clipCr = new BufferedImageCachableRed(bi, devR.x, devR.y);
               CachableRed ret = new MultiplyAlphaRed(cr, clipCr);
               CachableRed ret = new PadRed(ret, devAOIR, PadMode.ZERO_PAD, rh);
               return ret;
            } else {
               return null;
            }
         }
      }
   }
}
