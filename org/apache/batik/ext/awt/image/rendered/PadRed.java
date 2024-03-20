package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;

public class PadRed extends AbstractRed {
   static final boolean DEBUG = false;
   PadMode padMode;
   RenderingHints hints;

   public PadRed(CachableRed src, Rectangle bounds, PadMode padMode, RenderingHints hints) {
      super((CachableRed)src, bounds, src.getColorModel(), fixSampleModel(src, bounds), bounds.x, bounds.y, (Map)null);
      this.padMode = padMode;
      this.hints = hints;
   }

   public WritableRaster copyData(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      Rectangle srcR = src.getBounds();
      Rectangle wrR = wr.getBounds();
      if (wrR.intersects(srcR)) {
         Rectangle r = wrR.intersection(srcR);
         WritableRaster srcWR = wr.createWritableChild(r.x, r.y, r.width, r.height, r.x, r.y, (int[])null);
         src.copyData(srcWR);
      }

      if (this.padMode == PadMode.ZERO_PAD) {
         this.handleZero(wr);
      } else if (this.padMode == PadMode.REPLICATE) {
         this.handleReplicate(wr);
      } else if (this.padMode == PadMode.WRAP) {
         this.handleWrap(wr);
      }

      return wr;
   }

   protected void handleZero(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      Rectangle srcR = src.getBounds();
      Rectangle wrR = wr.getBounds();
      ZeroRecter zr = PadRed.ZeroRecter.getZeroRecter(wr);
      Rectangle ar = new Rectangle(wrR.x, wrR.y, wrR.width, wrR.height);
      Rectangle dr = new Rectangle(wrR.x, wrR.y, wrR.width, wrR.height);
      int w;
      if (ar.x < srcR.x) {
         w = srcR.x - ar.x;
         if (w > ar.width) {
            w = ar.width;
         }

         dr.width = w;
         zr.zeroRect(dr);
         ar.x += w;
         ar.width -= w;
      }

      if (ar.y < srcR.y) {
         w = srcR.y - ar.y;
         if (w > ar.height) {
            w = ar.height;
         }

         dr.x = ar.x;
         dr.y = ar.y;
         dr.width = ar.width;
         dr.height = w;
         zr.zeroRect(dr);
         ar.y += w;
         ar.height -= w;
      }

      int x0;
      if (ar.y + ar.height > srcR.y + srcR.height) {
         w = ar.y + ar.height - (srcR.y + srcR.height);
         if (w > ar.height) {
            w = ar.height;
         }

         x0 = ar.y + ar.height - w;
         dr.x = ar.x;
         dr.y = x0;
         dr.width = ar.width;
         dr.height = w;
         zr.zeroRect(dr);
         ar.height -= w;
      }

      if (ar.x + ar.width > srcR.x + srcR.width) {
         w = ar.x + ar.width - (srcR.x + srcR.width);
         if (w > ar.width) {
            w = ar.width;
         }

         x0 = ar.x + ar.width - w;
         dr.x = x0;
         dr.y = ar.y;
         dr.width = w;
         dr.height = ar.height;
         zr.zeroRect(dr);
         ar.width -= w;
      }

   }

   protected void handleReplicate(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      Rectangle srcR = src.getBounds();
      Rectangle wrR = wr.getBounds();
      int x = wrR.x;
      int y = wrR.y;
      int width = wrR.width;
      int height = wrR.height;
      int repW = srcR.x > x ? srcR.x : x;
      int xLoc = srcR.x + srcR.width - 1 < x + width - 1 ? srcR.x + srcR.width - 1 : x + width - 1;
      int endX = srcR.y > y ? srcR.y : y;
      int wrX = srcR.y + srcR.height - 1 < y + height - 1 ? srcR.y + srcR.height - 1 : y + height - 1;
      int wrY = repW;
      int endY = xLoc - repW + 1;
      int endY = endX;
      int h = wrX - endX + 1;
      if (endY < 0) {
         wrY = 0;
         endY = 0;
      }

      if (h < 0) {
         endY = 0;
         h = 0;
      }

      Rectangle r = new Rectangle(wrY, endY, endY, h);
      if (y < srcR.y) {
         repW = r.width;
         xLoc = r.x;
         endX = r.x;
         if (x + width - 1 <= srcR.x) {
            repW = 1;
            xLoc = srcR.x;
            endX = x + width - 1;
         } else if (x >= srcR.x + srcR.width) {
            repW = 1;
            xLoc = srcR.x + srcR.width - 1;
            endX = x;
         }

         WritableRaster wr1 = wr.createWritableChild(endX, y, repW, 1, xLoc, srcR.y, (int[])null);
         src.copyData(wr1);
         wrX = y + 1;
         endY = srcR.y;
         if (y + height < endY) {
            endY = y + height;
         }

         if (wrX < endY) {
            for(int[] pixels = wr.getPixels(endX, wrX - 1, repW, 1, (int[])null); wrX < srcR.y; ++wrX) {
               wr.setPixels(endX, wrX, repW, 1, pixels);
            }
         }
      }

      if (y + height > srcR.y + srcR.height) {
         repW = r.width;
         xLoc = r.x;
         endX = srcR.y + srcR.height - 1;
         wrX = r.x;
         wrY = srcR.y + srcR.height;
         if (wrY < y) {
            wrY = y;
         }

         if (x + width <= srcR.x) {
            repW = 1;
            xLoc = srcR.x;
            wrX = x + width - 1;
         } else if (x >= srcR.x + srcR.width) {
            repW = 1;
            xLoc = srcR.x + srcR.width - 1;
            wrX = x;
         }

         WritableRaster wr1 = wr.createWritableChild(wrX, wrY, repW, 1, xLoc, endX, (int[])null);
         src.copyData(wr1);
         ++wrY;
         endY = y + height;
         if (wrY < endY) {
            for(int[] pixels = wr.getPixels(wrX, wrY - 1, repW, 1, (int[])null); wrY < endY; ++wrY) {
               wr.setPixels(wrX, wrY, repW, 1, pixels);
            }
         }
      }

      if (x < srcR.x) {
         repW = srcR.x;
         if (x + width <= srcR.x) {
            repW = x + width - 1;
         }

         xLoc = x;

         for(int[] pixels = wr.getPixels(repW, y, 1, height, (int[])null); xLoc < repW; ++xLoc) {
            wr.setPixels(xLoc, y, 1, height, pixels);
         }
      }

      if (x + width > srcR.x + srcR.width) {
         repW = srcR.x + srcR.width - 1;
         if (x >= srcR.x + srcR.width) {
            repW = x;
         }

         xLoc = repW + 1;
         endX = x + width - 1;

         for(int[] pixels = wr.getPixels(repW, y, 1, height, (int[])null); xLoc < endX; ++xLoc) {
            wr.setPixels(xLoc, y, 1, height, pixels);
         }
      }

   }

   protected void handleWrap(WritableRaster wr) {
      this.handleZero(wr);
   }

   protected static SampleModel fixSampleModel(CachableRed src, Rectangle bounds) {
      int defSz = AbstractTiledRed.getDefaultTileSize();
      SampleModel sm = src.getSampleModel();
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

      return sm.createCompatibleSampleModel(w, h);
   }

   protected static class ZeroRecter_INT_PACK extends ZeroRecter {
      final int base;
      final int scanStride;
      final int[] pixels;
      final int[] zeros;
      final int x0;
      final int y0;

      public ZeroRecter_INT_PACK(WritableRaster wr) {
         super(wr);
         SinglePixelPackedSampleModel sppsm = (SinglePixelPackedSampleModel)wr.getSampleModel();
         this.scanStride = sppsm.getScanlineStride();
         DataBufferInt db = (DataBufferInt)wr.getDataBuffer();
         this.x0 = wr.getMinY();
         this.y0 = wr.getMinX();
         this.base = db.getOffset() + sppsm.getOffset(this.x0 - wr.getSampleModelTranslateX(), this.y0 - wr.getSampleModelTranslateY());
         this.pixels = db.getBankData()[0];
         if (wr.getWidth() > 10) {
            this.zeros = new int[wr.getWidth()];
         } else {
            this.zeros = null;
         }

      }

      public void zeroRect(Rectangle r) {
         int rbase = this.base + (r.x - this.x0) + (r.y - this.y0) * this.scanStride;
         int sp;
         int end;
         if (r.width > 10) {
            for(sp = 0; sp < r.height; ++sp) {
               end = rbase + sp * this.scanStride;
               System.arraycopy(this.zeros, 0, this.pixels, end, r.width);
            }
         } else {
            sp = rbase;
            end = rbase + r.width;
            int adj = this.scanStride - r.width;

            for(int y = 0; y < r.height; ++y) {
               while(sp < end) {
                  this.pixels[sp++] = 0;
               }

               sp += adj;
               end += this.scanStride;
            }
         }

      }
   }

   protected static class ZeroRecter {
      WritableRaster wr;
      int bands;
      static int[] zeros = null;

      public ZeroRecter(WritableRaster wr) {
         this.wr = wr;
         this.bands = wr.getSampleModel().getNumBands();
      }

      public void zeroRect(Rectangle r) {
         synchronized(this) {
            if (zeros == null || zeros.length < r.width * this.bands) {
               zeros = new int[r.width * this.bands];
            }
         }

         for(int y = 0; y < r.height; ++y) {
            this.wr.setPixels(r.x, r.y + y, r.width, 1, zeros);
         }

      }

      public static ZeroRecter getZeroRecter(WritableRaster wr) {
         return (ZeroRecter)(GraphicsUtil.is_INT_PACK_Data(wr.getSampleModel(), false) ? new ZeroRecter_INT_PACK(wr) : new ZeroRecter(wr));
      }

      public static void zeroRect(WritableRaster wr) {
         ZeroRecter zr = getZeroRecter(wr);
         zr.zeroRect(wr.getBounds());
      }
   }
}
