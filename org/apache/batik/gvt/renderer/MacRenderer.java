package org.apache.batik.gvt.renderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.ext.awt.geom.RectListManager;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.HaltingThread;

public class MacRenderer implements ImageRenderer {
   static final int COPY_OVERHEAD = 1000;
   static final int COPY_LINE_OVERHEAD = 10;
   static final AffineTransform IDENTITY = new AffineTransform();
   protected RenderingHints renderingHints = new RenderingHints((Map)null);
   protected AffineTransform usr2dev;
   protected GraphicsNode rootGN;
   protected int offScreenWidth;
   protected int offScreenHeight;
   protected boolean isDoubleBuffered;
   protected BufferedImage currImg;
   protected BufferedImage workImg;
   protected RectListManager damagedAreas;
   public static int IMAGE_TYPE = 3;
   public static Color TRANSPARENT_WHITE = new Color(255, 255, 255, 0);
   protected static RenderingHints defaultRenderingHints = new RenderingHints((Map)null);

   public MacRenderer() {
      this.renderingHints.add(defaultRenderingHints);
      this.usr2dev = new AffineTransform();
   }

   public MacRenderer(RenderingHints rh, AffineTransform at) {
      this.renderingHints.add(rh);
      if (at == null) {
         this.usr2dev = new AffineTransform();
      } else {
         this.usr2dev = new AffineTransform(at);
      }

   }

   public void dispose() {
      this.rootGN = null;
      this.currImg = null;
      this.workImg = null;
      this.renderingHints = null;
      this.usr2dev = null;
      if (this.damagedAreas != null) {
         this.damagedAreas.clear();
      }

      this.damagedAreas = null;
   }

   public void setTree(GraphicsNode treeRoot) {
      this.rootGN = treeRoot;
   }

   public GraphicsNode getTree() {
      return this.rootGN;
   }

   public void setTransform(AffineTransform usr2dev) {
      if (usr2dev == null) {
         this.usr2dev = new AffineTransform();
      } else {
         this.usr2dev = new AffineTransform(usr2dev);
      }

      if (this.workImg != null) {
         synchronized(this.workImg) {
            Graphics2D g2d = this.workImg.createGraphics();
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, this.workImg.getWidth(), this.workImg.getHeight());
            g2d.dispose();
         }

         this.damagedAreas = null;
      }
   }

   public AffineTransform getTransform() {
      return this.usr2dev;
   }

   public void setRenderingHints(RenderingHints rh) {
      this.renderingHints = new RenderingHints((Map)null);
      this.renderingHints.add(rh);
      this.damagedAreas = null;
   }

   public RenderingHints getRenderingHints() {
      return this.renderingHints;
   }

   public boolean isDoubleBuffered() {
      return this.isDoubleBuffered;
   }

   public void setDoubleBuffered(boolean isDoubleBuffered) {
      if (this.isDoubleBuffered != isDoubleBuffered) {
         this.isDoubleBuffered = isDoubleBuffered;
         if (isDoubleBuffered) {
            this.workImg = null;
         } else {
            this.workImg = this.currImg;
            this.damagedAreas = null;
         }

      }
   }

   public void updateOffScreen(int width, int height) {
      this.offScreenWidth = width;
      this.offScreenHeight = height;
   }

   public BufferedImage getOffScreen() {
      return this.rootGN == null ? null : this.currImg;
   }

   public void clearOffScreen() {
      if (!this.isDoubleBuffered) {
         this.updateWorkingBuffers();
         if (this.workImg != null) {
            synchronized(this.workImg) {
               Graphics2D g2d = this.workImg.createGraphics();
               g2d.setComposite(AlphaComposite.Clear);
               g2d.fillRect(0, 0, this.workImg.getWidth(), this.workImg.getHeight());
               g2d.dispose();
            }

            this.damagedAreas = null;
         }
      }
   }

   public void flush() {
   }

   public void flush(Rectangle r) {
   }

   public void flush(Collection areas) {
   }

   protected void updateWorkingBuffers() {
      if (this.rootGN == null) {
         this.currImg = null;
         this.workImg = null;
      } else {
         int w = this.offScreenWidth;
         int h = this.offScreenHeight;
         if (this.workImg == null || this.workImg.getWidth() < w || this.workImg.getHeight() < h) {
            this.workImg = new BufferedImage(w, h, IMAGE_TYPE);
         }

         if (!this.isDoubleBuffered) {
            this.currImg = this.workImg;
         }

      }
   }

   public void repaint(Shape area) {
      if (area != null) {
         RectListManager rlm = new RectListManager();
         rlm.add(this.usr2dev.createTransformedShape(area).getBounds());
         this.repaint(rlm);
      }
   }

   public void repaint(RectListManager devRLM) {
      if (devRLM != null) {
         this.updateWorkingBuffers();
         if (this.rootGN != null && this.workImg != null) {
            try {
               synchronized(this.workImg) {
                  Graphics2D g2d = GraphicsUtil.createGraphics(this.workImg, this.renderingHints);
                  Rectangle dr = new Rectangle(0, 0, this.offScreenWidth, this.offScreenHeight);
                  Iterator iter;
                  if (this.isDoubleBuffered && this.currImg != null && this.damagedAreas != null) {
                     this.damagedAreas.subtract(devRLM, 1000, 10);
                     this.damagedAreas.mergeRects(1000, 10);
                     iter = this.damagedAreas.iterator();
                     g2d.setComposite(AlphaComposite.Src);

                     while(iter.hasNext()) {
                        Rectangle r = (Rectangle)iter.next();
                        if (dr.intersects(r)) {
                           r = dr.intersection(r);
                           g2d.setClip(r.x, r.y, r.width, r.height);
                           g2d.setComposite(AlphaComposite.Clear);
                           g2d.fillRect(r.x, r.y, r.width, r.height);
                           g2d.setComposite(AlphaComposite.SrcOver);
                           g2d.drawImage(this.currImg, 0, 0, (ImageObserver)null);
                        }
                     }
                  }

                  iter = devRLM.iterator();

                  while(iter.hasNext()) {
                     Object aDevRLM = iter.next();
                     Rectangle r = (Rectangle)aDevRLM;
                     if (dr.intersects(r)) {
                        r = dr.intersection(r);
                        g2d.setTransform(IDENTITY);
                        g2d.setClip(r.x, r.y, r.width, r.height);
                        g2d.setComposite(AlphaComposite.Clear);
                        g2d.fillRect(r.x, r.y, r.width, r.height);
                        g2d.setComposite(AlphaComposite.SrcOver);
                        g2d.transform(this.usr2dev);
                        this.rootGN.paint(g2d);
                     }
                  }

                  g2d.dispose();
               }
            } catch (Throwable var10) {
               var10.printStackTrace();
            }

            if (!HaltingThread.hasBeenHalted()) {
               if (this.isDoubleBuffered) {
                  BufferedImage tmpImg = this.workImg;
                  this.workImg = this.currImg;
                  this.currImg = tmpImg;
                  this.damagedAreas = devRLM;
               }

            }
         }
      }
   }

   static {
      defaultRenderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      defaultRenderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
   }
}
