package org.apache.batik.ext.awt.image.renderable;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.CompositeRed;
import org.apache.batik.ext.awt.image.rendered.FloodRed;

public class CompositeRable8Bit extends AbstractColorInterpolationRable implements CompositeRable, PaintRable {
   protected CompositeRule rule;

   public CompositeRable8Bit(List srcs, CompositeRule rule, boolean csIsLinear) {
      super(srcs);
      this.setColorSpaceLinear(csIsLinear);
      this.rule = rule;
   }

   public void setSources(List srcs) {
      this.init(srcs, (Map)null);
   }

   public void setCompositeRule(CompositeRule cr) {
      this.touch();
      this.rule = cr;
   }

   public CompositeRule getCompositeRule() {
      return this.rule;
   }

   public boolean paintRable(Graphics2D g2d) {
      Composite c = g2d.getComposite();
      if (!SVGComposite.OVER.equals(c)) {
         return false;
      } else if (this.getCompositeRule() != CompositeRule.OVER) {
         return false;
      } else {
         ColorSpace crCS = this.getOperationColorSpace();
         ColorSpace g2dCS = GraphicsUtil.getDestinationColorSpace(g2d);
         if (g2dCS != null && g2dCS == crCS) {
            Iterator var5 = this.getSources().iterator();

            while(var5.hasNext()) {
               Object o = var5.next();
               GraphicsUtil.drawImage(g2d, (RenderableImage)((Filter)o));
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public RenderedImage createRendering(RenderContext rc) {
      if (this.srcs.size() == 0) {
         return null;
      } else {
         RenderingHints rh = rc.getRenderingHints();
         if (rh == null) {
            rh = new RenderingHints((Map)null);
         }

         AffineTransform at = rc.getTransform();
         Shape aoi = rc.getAreaOfInterest();
         Rectangle2D aoiR;
         if (aoi == null) {
            aoiR = this.getBounds2D();
         } else {
            aoiR = aoi.getBounds2D();
            Rectangle2D bounds2d = this.getBounds2D();
            if (!bounds2d.intersects(aoiR)) {
               return null;
            }

            Rectangle2D.intersect(aoiR, bounds2d, aoiR);
         }

         Rectangle devRect = at.createTransformedShape(aoiR).getBounds();
         rc = new RenderContext(at, aoiR, rh);
         List srcs = new ArrayList();
         Iterator var8 = this.getSources().iterator();

         while(var8.hasNext()) {
            Object o = var8.next();
            Filter filt = (Filter)o;
            RenderedImage ri = filt.createRendering(rc);
            if (ri != null) {
               CachableRed cr = this.convertSourceCS(ri);
               srcs.add(cr);
            } else {
               switch (this.rule.getRule()) {
                  case 2:
                     return null;
                  case 3:
                     srcs.clear();
                  case 4:
                  case 5:
                  default:
                     break;
                  case 6:
                     srcs.add(new FloodRed(devRect));
               }
            }
         }

         if (srcs.size() == 0) {
            return null;
         } else {
            CachableRed cr = new CompositeRed(srcs, this.rule);
            return cr;
         }
      }
   }
}
