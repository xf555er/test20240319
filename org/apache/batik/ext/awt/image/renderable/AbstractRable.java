package org.apache.batik.ext.awt.image.renderable;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.ext.awt.image.rendered.RenderedImageCachableRed;

public abstract class AbstractRable implements Filter {
   protected Vector srcs;
   protected Map props;
   protected long stamp;

   protected AbstractRable() {
      this.props = new HashMap();
      this.stamp = 0L;
      this.srcs = new Vector();
   }

   protected AbstractRable(Filter src) {
      this.props = new HashMap();
      this.stamp = 0L;
      this.init((Filter)src, (Map)null);
   }

   protected AbstractRable(Filter src, Map props) {
      this.props = new HashMap();
      this.stamp = 0L;
      this.init(src, props);
   }

   protected AbstractRable(List srcs) {
      this((List)srcs, (Map)null);
   }

   protected AbstractRable(List srcs, Map props) {
      this.props = new HashMap();
      this.stamp = 0L;
      this.init(srcs, props);
   }

   public final void touch() {
      ++this.stamp;
   }

   public long getTimeStamp() {
      return this.stamp;
   }

   protected void init(Filter src) {
      this.touch();
      this.srcs = new Vector(1);
      if (src != null) {
         this.srcs.add(src);
      }

   }

   protected void init(Filter src, Map props) {
      this.init(src);
      if (props != null) {
         this.props.putAll(props);
      }

   }

   protected void init(List srcs) {
      this.touch();
      this.srcs = new Vector(srcs);
   }

   protected void init(List srcs, Map props) {
      this.init(srcs);
      if (props != null) {
         this.props.putAll(props);
      }

   }

   public Rectangle2D getBounds2D() {
      Rectangle2D bounds = null;
      if (this.srcs.size() != 0) {
         Iterator i = this.srcs.iterator();
         Filter src = (Filter)i.next();
         bounds = (Rectangle2D)src.getBounds2D().clone();

         while(i.hasNext()) {
            src = (Filter)i.next();
            Rectangle2D r = src.getBounds2D();
            Rectangle2D.union(bounds, r, bounds);
         }
      }

      return bounds;
   }

   public Vector getSources() {
      return this.srcs;
   }

   public RenderedImage createDefaultRendering() {
      return this.createScaledRendering(100, 100, (RenderingHints)null);
   }

   public RenderedImage createScaledRendering(int w, int h, RenderingHints hints) {
      float sX = (float)w / this.getWidth();
      float sY = (float)h / this.getHeight();
      float scale = Math.min(sX, sY);
      AffineTransform at = AffineTransform.getScaleInstance((double)scale, (double)scale);
      RenderContext rc = new RenderContext(at, hints);
      float dX = this.getWidth() * scale - (float)w;
      float dY = this.getHeight() * scale - (float)h;
      RenderedImage ri = this.createRendering(rc);
      CachableRed cr = RenderedImageCachableRed.wrap(ri);
      return new PadRed(cr, new Rectangle((int)(dX / 2.0F), (int)(dY / 2.0F), w, h), PadMode.ZERO_PAD, (RenderingHints)null);
   }

   public float getMinX() {
      return (float)this.getBounds2D().getX();
   }

   public float getMinY() {
      return (float)this.getBounds2D().getY();
   }

   public float getWidth() {
      return (float)this.getBounds2D().getWidth();
   }

   public float getHeight() {
      return (float)this.getBounds2D().getHeight();
   }

   public Object getProperty(String name) {
      Object ret = this.props.get(name);
      if (ret != null) {
         return ret;
      } else {
         Iterator var3 = this.srcs.iterator();

         do {
            if (!var3.hasNext()) {
               return null;
            }

            Object src = var3.next();
            RenderableImage ri = (RenderableImage)src;
            ret = ri.getProperty(name);
         } while(ret == null);

         return ret;
      }
   }

   public String[] getPropertyNames() {
      Set keys = this.props.keySet();
      Iterator iter = keys.iterator();
      String[] ret = new String[keys.size()];

      for(int i = 0; iter.hasNext(); ret[i++] = (String)iter.next()) {
      }

      iter = this.srcs.iterator();

      while(iter.hasNext()) {
         RenderableImage ri = (RenderableImage)iter.next();
         String[] srcProps = ri.getPropertyNames();
         if (srcProps.length != 0) {
            String[] tmp = new String[ret.length + srcProps.length];
            System.arraycopy(ret, 0, tmp, 0, ret.length);
            System.arraycopy(tmp, ret.length, srcProps, 0, srcProps.length);
            ret = tmp;
         }
      }

      return ret;
   }

   public boolean isDynamic() {
      return false;
   }

   public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn) {
      if (srcIndex >= 0 && srcIndex <= this.srcs.size()) {
         Rectangle2D srect = (Rectangle2D)outputRgn.clone();
         Rectangle2D bounds = this.getBounds2D();
         if (!bounds.intersects(srect)) {
            return new Rectangle2D.Float();
         } else {
            Rectangle2D.intersect(srect, bounds, srect);
            return srect;
         }
      } else {
         throw new IndexOutOfBoundsException("Nonexistant source requested.");
      }
   }

   public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn) {
      if (srcIndex >= 0 && srcIndex <= this.srcs.size()) {
         Rectangle2D drect = (Rectangle2D)inputRgn.clone();
         Rectangle2D bounds = this.getBounds2D();
         if (!bounds.intersects(drect)) {
            return new Rectangle2D.Float();
         } else {
            Rectangle2D.intersect(drect, bounds, drect);
            return drect;
         }
      } else {
         throw new IndexOutOfBoundsException("Nonexistant source requested.");
      }
   }
}
