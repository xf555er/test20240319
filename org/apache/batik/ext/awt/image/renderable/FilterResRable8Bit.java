package org.apache.batik.ext.awt.image.renderable;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.SVGComposite;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.TileCacheRed;

public class FilterResRable8Bit extends AbstractRable implements FilterResRable, PaintRable {
   private int filterResolutionX = -1;
   private int filterResolutionY = -1;
   Reference resRed = null;
   float resScale = 0.0F;

   public FilterResRable8Bit() {
   }

   public FilterResRable8Bit(Filter src, int filterResX, int filterResY) {
      this.init(src, (Map)null);
      this.setFilterResolutionX(filterResX);
      this.setFilterResolutionY(filterResY);
   }

   public Filter getSource() {
      return (Filter)this.srcs.get(0);
   }

   public void setSource(Filter src) {
      this.init(src, (Map)null);
   }

   public int getFilterResolutionX() {
      return this.filterResolutionX;
   }

   public void setFilterResolutionX(int filterResolutionX) {
      if (filterResolutionX < 0) {
         throw new IllegalArgumentException();
      } else {
         this.touch();
         this.filterResolutionX = filterResolutionX;
      }
   }

   public int getFilterResolutionY() {
      return this.filterResolutionY;
   }

   public void setFilterResolutionY(int filterResolutionY) {
      this.touch();
      this.filterResolutionY = filterResolutionY;
   }

   public boolean allPaintRable(RenderableImage ri) {
      if (!(ri instanceof PaintRable)) {
         return false;
      } else {
         List v = ri.getSources();
         if (v == null) {
            return true;
         } else {
            Iterator var3 = v.iterator();

            RenderableImage nri;
            do {
               if (!var3.hasNext()) {
                  return true;
               }

               Object aV = var3.next();
               nri = (RenderableImage)aV;
            } while(this.allPaintRable(nri));

            return false;
         }
      }
   }

   public boolean distributeAcross(RenderableImage src, Graphics2D g2d) {
      if (src instanceof PadRable) {
         PadRable pad = (PadRable)src;
         Shape clip = g2d.getClip();
         g2d.clip(pad.getPadRect());
         boolean ret = this.distributeAcross(pad.getSource(), g2d);
         g2d.setClip(clip);
         return ret;
      } else if (!(src instanceof CompositeRable)) {
         return false;
      } else {
         CompositeRable comp = (CompositeRable)src;
         if (comp.getCompositeRule() != CompositeRule.OVER) {
            return false;
         } else {
            List v = comp.getSources();
            if (v == null) {
               return true;
            } else {
               ListIterator li = v.listIterator(v.size());

               while(li.hasPrevious()) {
                  RenderableImage csrc = (RenderableImage)li.previous();
                  if (!this.allPaintRable(csrc)) {
                     li.next();
                     break;
                  }
               }

               if (!li.hasPrevious()) {
                  GraphicsUtil.drawImage(g2d, (RenderableImage)comp);
                  return true;
               } else if (!li.hasNext()) {
                  return false;
               } else {
                  int idx = li.nextIndex();
                  Filter f = new CompositeRable8Bit(v.subList(0, idx), comp.getCompositeRule(), comp.isColorSpaceLinear());
                  Filter f = new FilterResRable8Bit(f, this.getFilterResolutionX(), this.getFilterResolutionY());
                  GraphicsUtil.drawImage(g2d, (RenderableImage)f);

                  while(li.hasNext()) {
                     PaintRable pr = (PaintRable)li.next();
                     if (!pr.paintRable(g2d)) {
                        Filter prf = (Filter)pr;
                        Filter prf = new FilterResRable8Bit(prf, this.getFilterResolutionX(), this.getFilterResolutionY());
                        GraphicsUtil.drawImage(g2d, (RenderableImage)prf);
                     }
                  }

                  return true;
               }
            }
         }
      }
   }

   public boolean paintRable(Graphics2D g2d) {
      Composite c = g2d.getComposite();
      if (!SVGComposite.OVER.equals(c)) {
         return false;
      } else {
         Filter src = this.getSource();
         return this.distributeAcross(src, g2d);
      }
   }

   private float getResScale() {
      return this.resScale;
   }

   private RenderedImage getResRed(RenderingHints hints) {
      Rectangle2D imageRect = this.getBounds2D();
      double resScaleX = (double)this.getFilterResolutionX() / imageRect.getWidth();
      double resScaleY = (double)this.getFilterResolutionY() / imageRect.getHeight();
      float resScale = (float)Math.min(resScaleX, resScaleY);
      RenderedImage ret;
      if (resScale == this.resScale) {
         ret = (RenderedImage)this.resRed.get();
         if (ret != null) {
            return ret;
         }
      }

      AffineTransform resUsr2Dev = AffineTransform.getScaleInstance((double)resScale, (double)resScale);
      RenderContext newRC = new RenderContext(resUsr2Dev, (Shape)null, hints);
      ret = this.getSource().createRendering(newRC);
      RenderedImage ret = new TileCacheRed(GraphicsUtil.wrap(ret));
      this.resScale = resScale;
      this.resRed = new SoftReference(ret);
      return ret;
   }

   public RenderedImage createRendering(RenderContext renderContext) {
      AffineTransform usr2dev = renderContext.getTransform();
      if (usr2dev == null) {
         usr2dev = new AffineTransform();
      }

      RenderingHints hints = renderContext.getRenderingHints();
      int filterResolutionX = this.getFilterResolutionX();
      int filterResolutionY = this.getFilterResolutionY();
      if (filterResolutionX > 0 && filterResolutionY != 0) {
         Rectangle2D imageRect = this.getBounds2D();
         Rectangle devRect = usr2dev.createTransformedShape(imageRect).getBounds();
         float scaleX = 1.0F;
         if (filterResolutionX < devRect.width) {
            scaleX = (float)filterResolutionX / (float)devRect.width;
         }

         float scaleY = 1.0F;
         if (filterResolutionY < 0) {
            scaleY = scaleX;
         } else if (filterResolutionY < devRect.height) {
            scaleY = (float)filterResolutionY / (float)devRect.height;
         }

         if (scaleX >= 1.0F && scaleY >= 1.0F) {
            return this.getSource().createRendering(renderContext);
         } else {
            RenderedImage resRed = this.getResRed(hints);
            float resScale = this.getResScale();
            AffineTransform residualAT = new AffineTransform(usr2dev.getScaleX() / (double)resScale, usr2dev.getShearY() / (double)resScale, usr2dev.getShearX() / (double)resScale, usr2dev.getScaleY() / (double)resScale, usr2dev.getTranslateX(), usr2dev.getTranslateY());
            return new AffineRed(GraphicsUtil.wrap(resRed), residualAT, hints);
         }
      } else {
         return null;
      }
   }
}
