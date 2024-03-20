package org.apache.batik.gvt.filter;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.renderable.AbstractRable;
import org.apache.batik.ext.awt.image.renderable.AffineRable8Bit;
import org.apache.batik.ext.awt.image.renderable.CompositeRable8Bit;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.PadRable8Bit;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;

public class BackgroundRable8Bit extends AbstractRable {
   private GraphicsNode node;

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

   public BackgroundRable8Bit(GraphicsNode node) {
      if (node == null) {
         throw new IllegalArgumentException();
      } else {
         this.node = node;
      }
   }

   static Rectangle2D addBounds(CompositeGraphicsNode cgn, GraphicsNode child, Rectangle2D init) {
      List children = cgn.getChildren();
      Iterator i = children.iterator();
      Rectangle2D r2d = null;

      while(i.hasNext()) {
         GraphicsNode gn = (GraphicsNode)i.next();
         if (gn == child) {
            break;
         }

         Rectangle2D cr2d = gn.getBounds();
         if (cr2d != null) {
            AffineTransform at = gn.getTransform();
            if (at != null) {
               cr2d = at.createTransformedShape(cr2d).getBounds2D();
            }

            if (r2d == null) {
               r2d = (Rectangle2D)cr2d.clone();
            } else {
               r2d.add(cr2d);
            }
         }
      }

      if (r2d == null) {
         return init == null ? CompositeGraphicsNode.VIEWPORT : init;
      } else if (init == null) {
         return r2d;
      } else {
         init.add(r2d);
         return init;
      }
   }

   static Rectangle2D getViewportBounds(GraphicsNode gn, GraphicsNode child) {
      Rectangle2D r2d = null;
      CompositeGraphicsNode cgn;
      if (gn instanceof CompositeGraphicsNode) {
         cgn = (CompositeGraphicsNode)gn;
         r2d = cgn.getBackgroundEnable();
      }

      if (r2d == null) {
         r2d = getViewportBounds(gn.getParent(), gn);
      }

      if (r2d == null) {
         return null;
      } else if (r2d == CompositeGraphicsNode.VIEWPORT) {
         if (child == null) {
            return (Rectangle2D)gn.getPrimitiveBounds().clone();
         } else {
            cgn = (CompositeGraphicsNode)gn;
            return addBounds(cgn, child, (Rectangle2D)null);
         }
      } else {
         AffineTransform at = gn.getTransform();
         if (at != null) {
            try {
               at = at.createInverse();
               r2d = at.createTransformedShape(r2d).getBounds2D();
            } catch (NoninvertibleTransformException var5) {
               r2d = null;
            }
         }

         if (child != null) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = addBounds(cgn, child, r2d);
         } else {
            Rectangle2D gnb = gn.getPrimitiveBounds();
            if (gnb != null) {
               r2d.add(gnb);
            }
         }

         return r2d;
      }
   }

   static Rectangle2D getBoundsRecursive(GraphicsNode gn, GraphicsNode child) {
      Rectangle2D r2d = null;
      if (gn == null) {
         return null;
      } else {
         if (gn instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = cgn.getBackgroundEnable();
         }

         if (r2d != null) {
            return r2d;
         } else {
            r2d = getBoundsRecursive(gn.getParent(), gn);
            if (r2d == null) {
               return new Rectangle2D.Float(0.0F, 0.0F, 0.0F, 0.0F);
            } else if (r2d == CompositeGraphicsNode.VIEWPORT) {
               return r2d;
            } else {
               AffineTransform at = gn.getTransform();
               if (at != null) {
                  try {
                     at = at.createInverse();
                     r2d = at.createTransformedShape(r2d).getBounds2D();
                  } catch (NoninvertibleTransformException var5) {
                     r2d = null;
                  }
               }

               return r2d;
            }
         }
      }
   }

   public Rectangle2D getBounds2D() {
      Rectangle2D r2d = getBoundsRecursive(this.node, (GraphicsNode)null);
      if (r2d == CompositeGraphicsNode.VIEWPORT) {
         r2d = getViewportBounds(this.node, (GraphicsNode)null);
      }

      return r2d;
   }

   public Filter getBackground(GraphicsNode gn, GraphicsNode child, Rectangle2D aoi) {
      if (gn == null) {
         throw new IllegalArgumentException("BackgroundImage requested yet no parent has 'enable-background:new'");
      } else {
         Rectangle2D r2d = null;
         if (gn instanceof CompositeGraphicsNode) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            r2d = cgn.getBackgroundEnable();
         }

         List srcs = new ArrayList();
         Rectangle2D ret;
         AffineTransform at;
         if (r2d == null) {
            ret = aoi;
            at = gn.getTransform();
            if (at != null) {
               ret = at.createTransformedShape(aoi).getBounds2D();
            }

            Filter f = this.getBackground(gn.getParent(), gn, ret);
            if (f != null && f.getBounds2D().intersects(aoi)) {
               srcs.add(f);
            }
         }

         if (child != null) {
            CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
            List children = cgn.getChildren();
            Iterator var18 = children.iterator();

            while(var18.hasNext()) {
               Object aChildren = var18.next();
               GraphicsNode childGN = (GraphicsNode)aChildren;
               if (childGN == child) {
                  break;
               }

               Rectangle2D cbounds = childGN.getBounds();
               if (cbounds != null) {
                  AffineTransform at = childGN.getTransform();
                  if (at != null) {
                     cbounds = at.createTransformedShape(cbounds).getBounds2D();
                  }

                  if (aoi.intersects(cbounds)) {
                     srcs.add(childGN.getEnableBackgroundGraphicsNodeRable(true));
                  }
               }
            }
         }

         if (srcs.size() == 0) {
            return null;
         } else {
            ret = null;
            Object ret;
            if (srcs.size() == 1) {
               ret = (Filter)srcs.get(0);
            } else {
               ret = new CompositeRable8Bit(srcs, CompositeRule.OVER, false);
            }

            if (child != null) {
               at = child.getTransform();
               if (at != null) {
                  try {
                     at = at.createInverse();
                     ret = new AffineRable8Bit((Filter)ret, at);
                  } catch (NoninvertibleTransformException var13) {
                     ret = null;
                  }
               }
            }

            return (Filter)ret;
         }
      }
   }

   public boolean isDynamic() {
      return false;
   }

   public RenderedImage createRendering(RenderContext renderContext) {
      Rectangle2D r2d = this.getBounds2D();
      Shape aoi = renderContext.getAreaOfInterest();
      if (aoi != null) {
         Rectangle2D aoiR2d = aoi.getBounds2D();
         if (!r2d.intersects(aoiR2d)) {
            return null;
         }

         Rectangle2D.intersect(r2d, aoiR2d, r2d);
      }

      Filter background = this.getBackground(this.node, (GraphicsNode)null, r2d);
      if (background == null) {
         return null;
      } else {
         Filter background = new PadRable8Bit(background, r2d, PadMode.ZERO_PAD);
         RenderedImage ri = background.createRendering(new RenderContext(renderContext.getTransform(), r2d, renderContext.getRenderingHints()));
         return ri;
      }
   }
}
