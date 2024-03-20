package org.apache.batik.ext.awt.image.rendered;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;

public class AffineRed extends AbstractRed {
   RenderingHints hints;
   AffineTransform src2me;
   AffineTransform me2src;

   public AffineTransform getTransform() {
      return (AffineTransform)this.src2me.clone();
   }

   public CachableRed getSource() {
      return (CachableRed)this.getSources().get(0);
   }

   public AffineRed(CachableRed src, AffineTransform src2me, RenderingHints hints) {
      this.src2me = src2me;
      this.hints = hints;

      try {
         this.me2src = src2me.createInverse();
      } catch (NoninvertibleTransformException var9) {
         this.me2src = null;
      }

      Rectangle srcBounds = src.getBounds();
      Rectangle myBounds = src2me.createTransformedShape(srcBounds).getBounds();
      ColorModel cm = fixColorModel(src);
      SampleModel sm = this.fixSampleModel(src, cm, myBounds);
      Point2D pt = new Point2D.Float((float)src.getTileGridXOffset(), (float)src.getTileGridYOffset());
      Point2D pt = src2me.transform(pt, (Point2D)null);
      this.init(src, myBounds, cm, sm, (int)pt.getX(), (int)pt.getY(), (Map)null);
   }

   public WritableRaster copyData(WritableRaster wr) {
      PadRed.ZeroRecter zr = PadRed.ZeroRecter.getZeroRecter(wr);
      zr.zeroRect(new Rectangle(wr.getMinX(), wr.getMinY(), wr.getWidth(), wr.getHeight()));
      this.genRect(wr);
      return wr;
   }

   public Raster getTile(int x, int y) {
      if (this.me2src == null) {
         return null;
      } else {
         int tx = this.tileGridXOff + x * this.tileWidth;
         int ty = this.tileGridYOff + y * this.tileHeight;
         Point pt = new Point(tx, ty);
         WritableRaster wr = Raster.createWritableRaster(this.sm, pt);
         this.genRect(wr);
         return wr;
      }
   }

   public void genRect(WritableRaster wr) {
      if (this.me2src != null) {
         Rectangle srcR = this.me2src.createTransformedShape(wr.getBounds()).getBounds();
         srcR.setBounds(srcR.x - 1, srcR.y - 1, srcR.width + 2, srcR.height + 2);
         CachableRed src = (CachableRed)this.getSources().get(0);
         if (srcR.intersects(src.getBounds())) {
            Raster srcRas = src.getData(srcR.intersection(src.getBounds()));
            if (srcRas != null) {
               AffineTransform aff = (AffineTransform)this.src2me.clone();
               aff.concatenate(AffineTransform.getTranslateInstance((double)srcRas.getMinX(), (double)srcRas.getMinY()));
               Point2D srcPt = new Point2D.Float((float)wr.getMinX(), (float)wr.getMinY());
               Point2D srcPt = this.me2src.transform(srcPt, (Point2D)null);
               Point2D destPt = new Point2D.Double(srcPt.getX() - (double)srcRas.getMinX(), srcPt.getY() - (double)srcRas.getMinY());
               Point2D destPt = aff.transform(destPt, (Point2D)null);
               aff.preConcatenate(AffineTransform.getTranslateInstance(-destPt.getX(), -destPt.getY()));
               AffineTransformOp op = new AffineTransformOp(aff, this.hints);
               ColorModel srcCM = src.getColorModel();
               ColorModel myCM = this.getColorModel();
               WritableRaster srcWR = (WritableRaster)srcRas;
               srcCM = GraphicsUtil.coerceData(srcWR, srcCM, true);
               BufferedImage srcBI = new BufferedImage(srcCM, srcWR.createWritableTranslatedChild(0, 0), srcCM.isAlphaPremultiplied(), (Hashtable)null);
               BufferedImage myBI = new BufferedImage(myCM, wr.createWritableTranslatedChild(0, 0), myCM.isAlphaPremultiplied(), (Hashtable)null);
               op.filter(srcBI.getRaster(), myBI.getRaster());
            }
         }
      }
   }

   protected static ColorModel fixColorModel(CachableRed src) {
      ColorModel cm = src.getColorModel();
      if (cm.hasAlpha()) {
         if (!cm.isAlphaPremultiplied()) {
            cm = GraphicsUtil.coerceColorModel(cm, true);
         }

         return cm;
      } else {
         ColorSpace cs = cm.getColorSpace();
         int b = src.getSampleModel().getNumBands() + 1;
         int[] bits;
         int i;
         if (b == 4) {
            bits = new int[4];

            for(i = 0; i < b - 1; ++i) {
               bits[i] = 16711680 >> 8 * i;
            }

            bits[3] = 255 << 8 * (b - 1);
            return new DirectColorModel(cs, 8 * b, bits[0], bits[1], bits[2], bits[3], true, 3);
         } else {
            bits = new int[b];

            for(i = 0; i < b; ++i) {
               bits[i] = 8;
            }

            return new ComponentColorModel(cs, bits, true, true, 3, 3);
         }
      }
   }

   protected SampleModel fixSampleModel(CachableRed src, ColorModel cm, Rectangle bounds) {
      SampleModel sm = src.getSampleModel();
      int defSz = AbstractTiledRed.getDefaultTileSize();
      int w = sm.getWidth();
      if (w < defSz) {
         w = defSz;
      }

      if (w > bounds.width) {
         w = bounds.width;
      }

      int h = sm.getHeight();
      if (h < defSz) {
         h = defSz;
      }

      if (h > bounds.height) {
         h = bounds.height;
      }

      if (w <= 0 || h <= 0) {
         w = 1;
         h = 1;
      }

      return cm.createCompatibleSampleModel(w, h);
   }
}
