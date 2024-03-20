package org.apache.batik.ext.awt.image.renderable;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import java.util.Hashtable;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BufferedImageCachableRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;

public class ConvolveMatrixRable8Bit extends AbstractColorInterpolationRable implements ConvolveMatrixRable {
   Kernel kernel;
   Point target;
   float bias;
   boolean kernelHasNegValues;
   PadMode edgeMode;
   float[] kernelUnitLength = new float[2];
   boolean preserveAlpha = false;

   public ConvolveMatrixRable8Bit(Filter source) {
      super(source);
   }

   public Filter getSource() {
      return (Filter)this.getSources().get(0);
   }

   public void setSource(Filter src) {
      this.init(src);
   }

   public Kernel getKernel() {
      return this.kernel;
   }

   public void setKernel(Kernel k) {
      this.touch();
      this.kernel = k;
      this.kernelHasNegValues = false;
      float[] kv = k.getKernelData((float[])null);
      float[] var3 = kv;
      int var4 = kv.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         float aKv = var3[var5];
         if (aKv < 0.0F) {
            this.kernelHasNegValues = true;
            break;
         }
      }

   }

   public Point getTarget() {
      return (Point)this.target.clone();
   }

   public void setTarget(Point pt) {
      this.touch();
      this.target = (Point)pt.clone();
   }

   public double getBias() {
      return (double)this.bias;
   }

   public void setBias(double bias) {
      this.touch();
      this.bias = (float)bias;
   }

   public PadMode getEdgeMode() {
      return this.edgeMode;
   }

   public void setEdgeMode(PadMode edgeMode) {
      this.touch();
      this.edgeMode = edgeMode;
   }

   public double[] getKernelUnitLength() {
      if (this.kernelUnitLength == null) {
         return null;
      } else {
         double[] ret = new double[]{(double)this.kernelUnitLength[0], (double)this.kernelUnitLength[1]};
         return ret;
      }
   }

   public void setKernelUnitLength(double[] kernelUnitLength) {
      this.touch();
      if (kernelUnitLength == null) {
         this.kernelUnitLength = null;
      } else {
         if (this.kernelUnitLength == null) {
            this.kernelUnitLength = new float[2];
         }

         this.kernelUnitLength[0] = (float)kernelUnitLength[0];
         this.kernelUnitLength[1] = (float)kernelUnitLength[1];
      }
   }

   public boolean getPreserveAlpha() {
      return this.preserveAlpha;
   }

   public void setPreserveAlpha(boolean preserveAlpha) {
      this.touch();
      this.preserveAlpha = preserveAlpha;
   }

   public void fixAlpha(BufferedImage bi) {
      if (bi.getColorModel().hasAlpha() && bi.isAlphaPremultiplied()) {
         if (GraphicsUtil.is_INT_PACK_Data(bi.getSampleModel(), true)) {
            this.fixAlpha_INT_PACK(bi.getRaster());
         } else {
            this.fixAlpha_FALLBACK(bi.getRaster());
         }

      }
   }

   public void fixAlpha_INT_PACK(WritableRaster wr) {
      SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
      int width = wr.getWidth();
      int scanStride = sppsm.getScanlineStride();
      DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
      int base = db.getOffset() + sppsm.getOffset(wr.getMinX() - wr.getSampleModelTranslateX(), wr.getMinY() - wr.getSampleModelTranslateY());
      int[] pixels = db.getBankData()[0];

      for(int y = 0; y < wr.getHeight(); ++y) {
         int sp = base + y * scanStride;

         for(int end = sp + width; sp < end; ++sp) {
            int pixel = pixels[sp];
            int a = pixel >>> 24;
            int v = pixel >> 16 & 255;
            if (a < v) {
               a = v;
            }

            v = pixel >> 8 & 255;
            if (a < v) {
               a = v;
            }

            v = pixel & 255;
            if (a < v) {
               a = v;
            }

            pixels[sp] = pixel & 16777215 | a << 24;
         }
      }

   }

   public void fixAlpha_FALLBACK(WritableRaster wr) {
      int x0 = wr.getMinX();
      int w = wr.getWidth();
      int y0 = wr.getMinY();
      int y1 = y0 + wr.getHeight() - 1;
      int bands = wr.getNumBands();
      int[] pixel = null;

      for(int y = y0; y <= y1; ++y) {
         pixel = wr.getPixels(x0, y, w, 1, pixel);
         int i = 0;

         for(int x = 0; x < w; ++x) {
            int a = pixel[i];

            for(int b = 1; b < bands; ++b) {
               if (pixel[i + b] > a) {
                  a = pixel[i + b];
               }
            }

            pixel[i + bands - 1] = a;
            i += bands;
         }

         wr.setPixels(x0, y, w, 1, pixel);
      }

   }

   public RenderedImage createRendering(RenderContext rc) {
      RenderingHints rh = rc.getRenderingHints();
      if (rh == null) {
         rh = new RenderingHints((Map)null);
      }

      AffineTransform at = rc.getTransform();
      double sx = at.getScaleX();
      double sy = at.getScaleY();
      double shx = at.getShearX();
      double shy = at.getShearY();
      double tx = at.getTranslateX();
      double ty = at.getTranslateY();
      double scaleX = Math.sqrt(sx * sx + shy * shy);
      double scaleY = Math.sqrt(sy * sy + shx * shx);
      if (this.kernelUnitLength != null) {
         if ((double)this.kernelUnitLength[0] > 0.0) {
            scaleX = (double)(1.0F / this.kernelUnitLength[0]);
         }

         if ((double)this.kernelUnitLength[1] > 0.0) {
            scaleY = (double)(1.0F / this.kernelUnitLength[1]);
         }
      }

      Shape aoi = rc.getAreaOfInterest();
      if (aoi == null) {
         aoi = this.getBounds2D();
      }

      Rectangle2D r = ((Shape)aoi).getBounds2D();
      int kw = this.kernel.getWidth();
      int kh = this.kernel.getHeight();
      int kx = this.target.x;
      int ky = this.target.y;
      double rx0 = r.getX() - (double)kx / scaleX;
      double ry0 = r.getY() - (double)ky / scaleY;
      double rx1 = rx0 + r.getWidth() + (double)(kw - 1) / scaleX;
      double ry1 = ry0 + r.getHeight() + (double)(kh - 1) / scaleY;
      Rectangle2D r = new Rectangle2D.Double(Math.floor(rx0), Math.floor(ry0), Math.ceil(rx1 - Math.floor(rx0)), Math.ceil(ry1 - Math.floor(ry0)));
      AffineTransform srcAt = AffineTransform.getScaleInstance(scaleX, scaleY);
      AffineTransform resAt = new AffineTransform(sx / scaleX, shy / scaleX, shx / scaleY, sy / scaleY, tx, ty);
      RenderedImage ri = this.getSource().createRendering(new RenderContext(srcAt, r, rh));
      if (ri == null) {
         return null;
      } else {
         CachableRed cr = this.convertSourceCS(ri);
         Shape devShape = srcAt.createTransformedShape((Shape)aoi);
         Rectangle2D devRect = devShape.getBounds2D();
         r = new Rectangle2D.Double(Math.floor(devRect.getX() - (double)kx), Math.floor(devRect.getY() - (double)ky), Math.ceil(devRect.getX() + devRect.getWidth()) - Math.floor(devRect.getX()) + (double)(kw - 1), Math.ceil(devRect.getY() + devRect.getHeight()) - Math.floor(devRect.getY()) + (double)(kh - 1));
         if (!r.getBounds().equals(((CachableRed)cr).getBounds())) {
            if (this.edgeMode == PadMode.WRAP) {
               throw new IllegalArgumentException("edgeMode=\"wrap\" is not supported by ConvolveMatrix.");
            }

            cr = new PadRed((CachableRed)cr, r.getBounds(), this.edgeMode, rh);
         }

         if ((double)this.bias != 0.0) {
            throw new IllegalArgumentException("Only bias equal to zero is supported in ConvolveMatrix.");
         } else {
            BufferedImageOp op = new ConvolveOp(this.kernel, 1, rh);
            ColorModel cm = ((CachableRed)cr).getColorModel();
            Raster rr = ((CachableRed)cr).getData();
            WritableRaster wr = GraphicsUtil.makeRasterWritable(rr, 0, 0);
            int phaseShiftX = this.target.x - this.kernel.getXOrigin();
            int phaseShiftY = this.target.y - this.kernel.getYOrigin();
            int destX = (int)(r.getX() + (double)phaseShiftX);
            int destY = (int)(r.getY() + (double)phaseShiftY);
            BufferedImage destBI;
            BufferedImage srcBI;
            if (!this.preserveAlpha) {
               cm = GraphicsUtil.coerceData(wr, cm, true);
               srcBI = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), (Hashtable)null);
               destBI = op.filter(srcBI, (BufferedImage)null);
               if (this.kernelHasNegValues) {
                  this.fixAlpha(destBI);
               }
            } else {
               srcBI = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), (Hashtable)null);
               ColorModel cm = new DirectColorModel(ColorSpace.getInstance(1004), 24, 16711680, 65280, 255, 0, false, 3);
               BufferedImage tmpSrcBI = new BufferedImage(cm, cm.createCompatibleWritableRaster(wr.getWidth(), wr.getHeight()), cm.isAlphaPremultiplied(), (Hashtable)null);
               GraphicsUtil.copyData(srcBI, tmpSrcBI);
               ColorModel dstCM = GraphicsUtil.Linear_sRGB_Unpre;
               destBI = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(wr.getWidth(), wr.getHeight()), dstCM.isAlphaPremultiplied(), (Hashtable)null);
               WritableRaster dstWR = Raster.createWritableRaster(cm.createCompatibleSampleModel(wr.getWidth(), wr.getHeight()), destBI.getRaster().getDataBuffer(), new Point(0, 0));
               BufferedImage tmpDstBI = new BufferedImage(cm, dstWR, cm.isAlphaPremultiplied(), (Hashtable)null);
               op.filter(tmpSrcBI, tmpDstBI);
               Rectangle srcRect = wr.getBounds();
               Rectangle dstRect = new Rectangle(srcRect.x - phaseShiftX, srcRect.y - phaseShiftY, srcRect.width, srcRect.height);
               GraphicsUtil.copyBand(wr, srcRect, wr.getNumBands() - 1, destBI.getRaster(), dstRect, destBI.getRaster().getNumBands() - 1);
            }

            CachableRed cr = new BufferedImageCachableRed(destBI, destX, destY);
            cr = new PadRed(cr, devRect.getBounds(), PadMode.ZERO_PAD, rh);
            if (!resAt.isIdentity()) {
               cr = new AffineRed((CachableRed)cr, resAt, (RenderingHints)null);
            }

            return (RenderedImage)cr;
         }
      }
   }
}
