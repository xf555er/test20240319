package org.apache.batik.gvt;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.event.GraphicsNodeChangeAdapter;
import org.apache.batik.gvt.event.GraphicsNodeChangeEvent;

public class UpdateTracker extends GraphicsNodeChangeAdapter {
   Map dirtyNodes = null;
   Map fromBounds = new HashMap();
   protected static Rectangle2D NULL_RECT = new Rectangle();

   public boolean hasChanged() {
      return this.dirtyNodes != null;
   }

   public List getDirtyAreas() {
      if (this.dirtyNodes == null) {
         return null;
      } else {
         List ret = new LinkedList();
         Set keys = this.dirtyNodes.keySet();
         Iterator var3 = keys.iterator();

         while(true) {
            WeakReference gnWRef;
            Object gn;
            do {
               if (!var3.hasNext()) {
                  this.fromBounds.clear();
                  this.dirtyNodes.clear();
                  return ret;
               }

               Object key = var3.next();
               gnWRef = (WeakReference)key;
               gn = (GraphicsNode)gnWRef.get();
            } while(gn == null);

            AffineTransform oat = (AffineTransform)this.dirtyNodes.get(gnWRef);
            if (oat != null) {
               oat = new AffineTransform(oat);
            }

            Rectangle2D srcORgn = (Rectangle2D)this.fromBounds.remove(gnWRef);
            Rectangle2D srcNRgn = null;
            AffineTransform nat = null;
            if (!(srcORgn instanceof ChngSrcRect)) {
               srcNRgn = ((GraphicsNode)gn).getBounds();
               nat = ((GraphicsNode)gn).getTransform();
               if (nat != null) {
                  nat = new AffineTransform(nat);
               }
            }

            while(true) {
               gn = ((GraphicsNode)gn).getParent();
               if (gn == null) {
                  if (gn == null) {
                     Shape oRgn = srcORgn;
                     if (srcORgn != null && srcORgn != NULL_RECT) {
                        if (oat != null) {
                           oRgn = oat.createTransformedShape(srcORgn);
                        }

                        ret.add(oRgn);
                     }

                     if (srcNRgn != null) {
                        Shape nRgn = srcNRgn;
                        if (nat != null) {
                           nRgn = nat.createTransformedShape(srcNRgn);
                        }

                        if (nRgn != null) {
                           ret.add(nRgn);
                        }
                     }
                  }
                  break;
               }

               Filter f = ((GraphicsNode)gn).getFilter();
               if (f != null) {
                  srcNRgn = f.getBounds2D();
                  nat = null;
               }

               AffineTransform at = ((GraphicsNode)gn).getTransform();
               gnWRef = ((GraphicsNode)gn).getWeakReference();
               AffineTransform poat = (AffineTransform)this.dirtyNodes.get(gnWRef);
               if (poat == null) {
                  poat = at;
               }

               if (poat != null) {
                  if (oat != null) {
                     oat.preConcatenate(poat);
                  } else {
                     oat = new AffineTransform(poat);
                  }
               }

               if (at != null) {
                  if (nat != null) {
                     nat.preConcatenate(at);
                  } else {
                     nat = new AffineTransform(at);
                  }
               }
            }
         }
      }
   }

   public Rectangle2D getNodeDirtyRegion(GraphicsNode gn, AffineTransform at) {
      WeakReference gnWRef = gn.getWeakReference();
      AffineTransform nat = (AffineTransform)this.dirtyNodes.get(gnWRef);
      if (nat == null) {
         nat = gn.getTransform();
      }

      if (nat != null) {
         at = new AffineTransform(at);
         at.concatenate(nat);
      }

      Filter f = gn.getFilter();
      Rectangle2D ret = null;
      if (gn instanceof CompositeGraphicsNode) {
         CompositeGraphicsNode cgn = (CompositeGraphicsNode)gn;
         Iterator var8 = cgn.iterator();

         while(true) {
            while(true) {
               Rectangle2D r2d;
               do {
                  if (!var8.hasNext()) {
                     return ret;
                  }

                  Object aCgn = var8.next();
                  GraphicsNode childGN = (GraphicsNode)aCgn;
                  r2d = this.getNodeDirtyRegion(childGN, at);
               } while(r2d == null);

               if (f != null) {
                  Shape s = at.createTransformedShape(f.getBounds2D());
                  ret = s.getBounds2D();
                  return ret;
               }

               if (ret != null && ret != NULL_RECT) {
                  ret.add(r2d);
               } else {
                  ret = r2d;
               }
            }
         }
      } else {
         ret = (Rectangle2D)this.fromBounds.remove(gnWRef);
         if (ret == null) {
            if (f != null) {
               ret = f.getBounds2D();
            } else {
               ret = gn.getBounds();
            }
         } else if (ret == NULL_RECT) {
            ret = null;
         }

         if (ret != null) {
            ret = at.createTransformedShape(ret).getBounds2D();
         }
      }

      return ret;
   }

   public Rectangle2D getNodeDirtyRegion(GraphicsNode gn) {
      return this.getNodeDirtyRegion(gn, new AffineTransform());
   }

   public void changeStarted(GraphicsNodeChangeEvent gnce) {
      GraphicsNode gn = gnce.getGraphicsNode();
      WeakReference gnWRef = gn.getWeakReference();
      boolean doPut = false;
      if (this.dirtyNodes == null) {
         this.dirtyNodes = new HashMap();
         doPut = true;
      } else if (!this.dirtyNodes.containsKey(gnWRef)) {
         doPut = true;
      }

      if (doPut) {
         AffineTransform at = gn.getTransform();
         if (at != null) {
            at = (AffineTransform)at.clone();
         } else {
            at = new AffineTransform();
         }

         this.dirtyNodes.put(gnWRef, at);
      }

      GraphicsNode chngSrc = gnce.getChangeSrc();
      Rectangle2D rgn = null;
      if (chngSrc != null) {
         Rectangle2D drgn = this.getNodeDirtyRegion(chngSrc);
         if (drgn != null) {
            rgn = new ChngSrcRect(drgn);
         }
      } else {
         rgn = gn.getBounds();
      }

      Rectangle2D r2d = (Rectangle2D)this.fromBounds.remove(gnWRef);
      if (rgn != null) {
         if (r2d != null && r2d != NULL_RECT) {
            ((Rectangle2D)r2d).add((Rectangle2D)rgn);
         } else {
            r2d = rgn;
         }
      }

      if (r2d == null) {
         r2d = NULL_RECT;
      }

      this.fromBounds.put(gnWRef, r2d);
   }

   public void clear() {
      this.dirtyNodes = null;
   }

   static class ChngSrcRect extends Rectangle2D.Float {
      ChngSrcRect(Rectangle2D r2d) {
         super((float)r2d.getX(), (float)r2d.getY(), (float)r2d.getWidth(), (float)r2d.getHeight());
      }
   }
}
