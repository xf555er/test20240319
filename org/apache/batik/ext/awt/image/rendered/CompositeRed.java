package org.apache.batik.ext.awt.image.rendered;

import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.CompositeRule;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.SVGComposite;

public class CompositeRed extends AbstractRed {
   CompositeRule rule;
   CompositeContext[] contexts;

   public CompositeRed(List srcs, CompositeRule rule) {
      CachableRed src = (CachableRed)((List)srcs).get(0);
      ColorModel cm = fixColorModel(src);
      this.rule = rule;
      SVGComposite comp = new SVGComposite(rule);
      this.contexts = new CompositeContext[((List)srcs).size()];
      int idx = 0;
      Iterator i = ((List)srcs).iterator();
      Rectangle myBounds = null;

      while(i.hasNext()) {
         CachableRed cr = (CachableRed)i.next();
         this.contexts[idx++] = comp.createContext(cr.getColorModel(), cm, (RenderingHints)null);
         Rectangle newBound = cr.getBounds();
         if (myBounds == null) {
            myBounds = newBound;
         } else {
            switch (rule.getRule()) {
               case 2:
                  if (myBounds.intersects(newBound)) {
                     myBounds = myBounds.intersection(newBound);
                  } else {
                     myBounds.width = 0;
                     myBounds.height = 0;
                  }
                  break;
               case 3:
                  myBounds = newBound;
                  break;
               default:
                  myBounds.add(newBound);
            }
         }
      }

      if (myBounds == null) {
         throw new IllegalArgumentException("Composite Operation Must have some source!");
      } else {
         if (rule.getRule() == 6) {
            List vec = new ArrayList(((List)srcs).size());

            Object cr;
            for(i = ((List)srcs).iterator(); i.hasNext(); vec.add(cr)) {
               cr = (CachableRed)i.next();
               Rectangle r = ((CachableRed)cr).getBounds();
               if (r.x != myBounds.x || r.y != myBounds.y || r.width != myBounds.width || r.height != myBounds.height) {
                  cr = new PadRed((CachableRed)cr, myBounds, PadMode.ZERO_PAD, (RenderingHints)null);
               }
            }

            srcs = vec;
         }

         SampleModel sm = fixSampleModel(src, cm, myBounds);
         int defSz = AbstractTiledRed.getDefaultTileSize();
         int tgX = defSz * (int)Math.floor((double)(myBounds.x / defSz));
         int tgY = defSz * (int)Math.floor((double)(myBounds.y / defSz));
         this.init((List)srcs, myBounds, cm, sm, tgX, tgY, (Map)null);
      }
   }

   public WritableRaster copyData(WritableRaster wr) {
      this.genRect(wr);
      return wr;
   }

   public Raster getTile(int x, int y) {
      int tx = this.tileGridXOff + x * this.tileWidth;
      int ty = this.tileGridYOff + y * this.tileHeight;
      Point pt = new Point(tx, ty);
      WritableRaster wr = Raster.createWritableRaster(this.sm, pt);
      this.genRect(wr);
      return wr;
   }

   public void emptyRect(WritableRaster wr) {
      PadRed.ZeroRecter zr = PadRed.ZeroRecter.getZeroRecter(wr);
      zr.zeroRect(new Rectangle(wr.getMinX(), wr.getMinY(), wr.getWidth(), wr.getHeight()));
   }

   public void genRect(WritableRaster wr) {
      Rectangle r = wr.getBounds();
      int idx = 0;
      Iterator i = this.srcs.iterator();

      for(boolean first = true; i.hasNext(); ++idx) {
         CachableRed cr = (CachableRed)i.next();
         Rectangle crR;
         if (!first) {
            crR = cr.getBounds();
            if (crR.intersects(r)) {
               Rectangle smR = crR.intersection(r);
               Raster ras = cr.getData(smR);
               WritableRaster smWR = wr.createWritableChild(smR.x, smR.y, smR.width, smR.height, smR.x, smR.y, (int[])null);
               this.contexts[idx].compose(ras, smWR, smWR);
            }
         } else {
            crR = cr.getBounds();
            if (r.x < crR.x || r.y < crR.y || r.x + r.width > crR.x + crR.width || r.y + r.height > crR.y + crR.height) {
               this.emptyRect(wr);
            }

            cr.copyData(wr);
            if (!cr.getColorModel().isAlphaPremultiplied()) {
               GraphicsUtil.coerceData(wr, cr.getColorModel(), true);
            }

            first = false;
         }
      }

   }

   public void genRect_OVER(WritableRaster wr) {
      Rectangle r = wr.getBounds();
      ColorModel cm = this.getColorModel();
      BufferedImage bi = new BufferedImage(cm, wr.createWritableTranslatedChild(0, 0), cm.isAlphaPremultiplied(), (Hashtable)null);
      Graphics2D g2d = GraphicsUtil.createGraphics(bi);
      g2d.translate(-r.x, -r.y);
      Iterator i = this.srcs.iterator();
      boolean first = true;

      while(true) {
         while(i.hasNext()) {
            CachableRed cr = (CachableRed)i.next();
            if (first) {
               Rectangle crR = cr.getBounds();
               if (r.x < crR.x || r.y < crR.y || r.x + r.width > crR.x + crR.width || r.y + r.height > crR.y + crR.height) {
                  this.emptyRect(wr);
               }

               cr.copyData(wr);
               GraphicsUtil.coerceData(wr, cr.getColorModel(), cm.isAlphaPremultiplied());
               first = false;
            } else {
               GraphicsUtil.drawImage(g2d, cr);
            }
         }

         return;
      }
   }

   protected static SampleModel fixSampleModel(CachableRed src, ColorModel cm, Rectangle bounds) {
      int defSz = AbstractTiledRed.getDefaultTileSize();
      int tgX = defSz * (int)Math.floor((double)(bounds.x / defSz));
      int tgY = defSz * (int)Math.floor((double)(bounds.y / defSz));
      int tw = bounds.x + bounds.width - tgX;
      int th = bounds.y + bounds.height - tgY;
      SampleModel sm = src.getSampleModel();
      int w = sm.getWidth();
      if (w < defSz) {
         w = defSz;
      }

      if (w > tw) {
         w = tw;
      }

      int h = sm.getHeight();
      if (h < defSz) {
         h = defSz;
      }

      if (h > th) {
         h = th;
      }

      if (w <= 0 || h <= 0) {
         w = 1;
         h = 1;
      }

      return cm.createCompatibleSampleModel(w, h);
   }

   protected static ColorModel fixColorModel(CachableRed src) {
      ColorModel cm = src.getColorModel();
      if (cm.hasAlpha()) {
         if (!cm.isAlphaPremultiplied()) {
            cm = GraphicsUtil.coerceColorModel(cm, true);
         }

         return cm;
      } else {
         int b = src.getSampleModel().getNumBands() + 1;
         if (b > 4) {
            throw new IllegalArgumentException("CompositeRed can only handle up to three band images");
         } else {
            int[] masks = new int[4];

            for(int i = 0; i < b - 1; ++i) {
               masks[i] = 16711680 >> 8 * i;
            }

            masks[3] = 255 << 8 * (b - 1);
            ColorSpace cs = cm.getColorSpace();
            return new DirectColorModel(cs, 8 * b, masks[0], masks[1], masks[2], masks[3], true, 3);
         }
      }
   }
}
