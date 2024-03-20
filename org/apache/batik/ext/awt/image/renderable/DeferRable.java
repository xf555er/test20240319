package org.apache.batik.ext.awt.image.renderable;

import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import java.util.Vector;

public class DeferRable implements Filter {
   volatile Filter src;
   Rectangle2D bounds;
   Map props;

   public synchronized Filter getSource() {
      while(this.src == null) {
         try {
            this.wait();
         } catch (InterruptedException var2) {
         }
      }

      return this.src;
   }

   public synchronized void setSource(Filter src) {
      if (this.src == null) {
         this.src = src;
         this.bounds = src.getBounds2D();
         this.notifyAll();
      }
   }

   public synchronized void setBounds(Rectangle2D bounds) {
      if (this.bounds == null) {
         this.bounds = bounds;
         this.notifyAll();
      }
   }

   public synchronized void setProperties(Map props) {
      this.props = props;
      this.notifyAll();
   }

   public long getTimeStamp() {
      return this.getSource().getTimeStamp();
   }

   public Vector getSources() {
      return this.getSource().getSources();
   }

   public boolean isDynamic() {
      return this.getSource().isDynamic();
   }

   public Rectangle2D getBounds2D() {
      synchronized(this) {
         while(this.src == null && this.bounds == null) {
            try {
               this.wait();
            } catch (InterruptedException var4) {
            }
         }
      }

      return this.src != null ? this.src.getBounds2D() : this.bounds;
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
      synchronized(this) {
         while(this.src == null && this.props == null) {
            try {
               this.wait();
            } catch (InterruptedException var5) {
            }
         }
      }

      return this.src != null ? this.src.getProperty(name) : this.props.get(name);
   }

   public String[] getPropertyNames() {
      synchronized(this) {
         while(this.src == null && this.props == null) {
            try {
               this.wait();
            } catch (InterruptedException var4) {
            }
         }
      }

      if (this.src != null) {
         return this.src.getPropertyNames();
      } else {
         String[] ret = new String[this.props.size()];
         this.props.keySet().toArray(ret);
         return ret;
      }
   }

   public RenderedImage createDefaultRendering() {
      return this.getSource().createDefaultRendering();
   }

   public RenderedImage createScaledRendering(int w, int h, RenderingHints hints) {
      return this.getSource().createScaledRendering(w, h, hints);
   }

   public RenderedImage createRendering(RenderContext rc) {
      return this.getSource().createRendering(rc);
   }

   public Shape getDependencyRegion(int srcIndex, Rectangle2D outputRgn) {
      return this.getSource().getDependencyRegion(srcIndex, outputRgn);
   }

   public Shape getDirtyRegion(int srcIndex, Rectangle2D inputRgn) {
      return this.getSource().getDirtyRegion(srcIndex, inputRgn);
   }
}
