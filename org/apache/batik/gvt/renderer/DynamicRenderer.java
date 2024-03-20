package org.apache.batik.gvt.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import org.apache.batik.ext.awt.geom.RectListManager;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.util.HaltingThread;

public class DynamicRenderer extends StaticRenderer {
   static final int COPY_OVERHEAD = 1000;
   static final int COPY_LINE_OVERHEAD = 10;
   RectListManager damagedAreas;

   public DynamicRenderer() {
   }

   public DynamicRenderer(RenderingHints rh, AffineTransform at) {
      super(rh, at);
   }

   protected CachableRed setupCache(CachableRed img) {
      return img;
   }

   public void flush(Rectangle r) {
   }

   public void flush(Collection areas) {
   }

   protected void updateWorkingBuffers() {
      if (this.rootFilter == null) {
         this.rootFilter = this.rootGN.getGraphicsNodeRable(true);
         this.rootCR = null;
      }

      this.rootCR = this.renderGNR();
      if (this.rootCR == null) {
         this.workingRaster = null;
         this.workingOffScreen = null;
         this.workingBaseRaster = null;
         this.currentOffScreen = null;
         this.currentBaseRaster = null;
         this.currentRaster = null;
      } else {
         SampleModel sm = this.rootCR.getSampleModel();
         int w = this.offScreenWidth;
         int h = this.offScreenHeight;
         if (this.workingBaseRaster == null || this.workingBaseRaster.getWidth() < w || this.workingBaseRaster.getHeight() < h) {
            sm = sm.createCompatibleSampleModel(w, h);
            this.workingBaseRaster = Raster.createWritableRaster(sm, new Point(0, 0));
            this.workingRaster = this.workingBaseRaster.createWritableChild(0, 0, w, h, 0, 0, (int[])null);
            this.workingOffScreen = new BufferedImage(this.rootCR.getColorModel(), this.workingRaster, this.rootCR.getColorModel().isAlphaPremultiplied(), (Hashtable)null);
         }

         if (!this.isDoubleBuffered) {
            this.currentOffScreen = this.workingOffScreen;
            this.currentBaseRaster = this.workingBaseRaster;
            this.currentRaster = this.workingRaster;
         }

      }
   }

   public void repaint(RectListManager devRLM) {
      if (devRLM != null) {
         this.updateWorkingBuffers();
         if (this.rootCR != null && this.workingBaseRaster != null) {
            CachableRed cr = this.rootCR;
            WritableRaster syncRaster = this.workingBaseRaster;
            WritableRaster copyRaster = this.workingRaster;
            Rectangle srcR = this.rootCR.getBounds();
            Rectangle dstR = this.workingRaster.getBounds();
            if (dstR.x < srcR.x || dstR.y < srcR.y || dstR.x + dstR.width > srcR.x + srcR.width || dstR.y + dstR.height > srcR.y + srcR.height) {
               cr = new PadRed((CachableRed)cr, dstR, PadMode.ZERO_PAD, (RenderingHints)null);
            }

            boolean repaintAll = false;
            Rectangle dr = copyRaster.getBounds();
            Rectangle sr = null;
            if (this.currentRaster != null) {
               sr = this.currentRaster.getBounds();
            }

            synchronized(syncRaster) {
               if (repaintAll) {
                  ((CachableRed)cr).copyData(copyRaster);
               } else {
                  Graphics2D g2d = null;
                  Color fillColor;
                  Color borderColor;
                  Iterator var14;
                  Object damagedArea;
                  Rectangle r;
                  WritableRaster src;
                  if (this.isDoubleBuffered && this.currentRaster != null && this.damagedAreas != null) {
                     this.damagedAreas.subtract(devRLM, 1000, 10);
                     this.damagedAreas.mergeRects(1000, 10);
                     fillColor = new Color(0, 0, 255, 50);
                     borderColor = new Color(0, 0, 0, 50);
                     var14 = this.damagedAreas.iterator();

                     label73:
                     while(true) {
                        do {
                           do {
                              if (!var14.hasNext()) {
                                 break label73;
                              }

                              damagedArea = var14.next();
                              r = (Rectangle)damagedArea;
                           } while(!dr.intersects(r));

                           r = dr.intersection(r);
                        } while(sr != null && !sr.intersects(r));

                        r = sr.intersection(r);
                        src = this.currentRaster.createWritableChild(r.x, r.y, r.width, r.height, r.x, r.y, (int[])null);
                        GraphicsUtil.copyData((Raster)src, (WritableRaster)copyRaster);
                        if (g2d != null) {
                           ((Graphics2D)g2d).setPaint(fillColor);
                           ((Graphics2D)g2d).fill(r);
                           ((Graphics2D)g2d).setPaint(borderColor);
                           ((Graphics2D)g2d).draw(r);
                        }
                     }
                  }

                  fillColor = new Color(255, 0, 0, 50);
                  borderColor = new Color(0, 0, 0, 50);
                  var14 = devRLM.iterator();

                  while(var14.hasNext()) {
                     damagedArea = var14.next();
                     r = (Rectangle)damagedArea;
                     if (dr.intersects(r)) {
                        r = dr.intersection(r);
                        src = copyRaster.createWritableChild(r.x, r.y, r.width, r.height, r.x, r.y, (int[])null);
                        ((CachableRed)cr).copyData(src);
                        if (g2d != null) {
                           ((Graphics2D)g2d).setPaint(fillColor);
                           ((Graphics2D)g2d).fill(r);
                           ((Graphics2D)g2d).setPaint(borderColor);
                           ((Graphics2D)g2d).draw(r);
                        }
                     }
                  }
               }
            }

            if (!HaltingThread.hasBeenHalted()) {
               BufferedImage tmpBI = this.workingOffScreen;
               this.workingBaseRaster = this.currentBaseRaster;
               this.workingRaster = this.currentRaster;
               this.workingOffScreen = this.currentOffScreen;
               this.currentRaster = copyRaster;
               this.currentBaseRaster = syncRaster;
               this.currentOffScreen = tmpBI;
               this.damagedAreas = devRLM;
            }
         }
      }
   }
}
