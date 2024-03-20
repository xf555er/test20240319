package org.apache.batik.extension.svg;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Map;
import org.apache.batik.ext.awt.image.rendered.AbstractRed;
import org.apache.batik.ext.awt.image.rendered.CachableRed;

public class HistogramRed extends AbstractRed {
   boolean[] computed;
   int tallied = 0;
   int[] bins = new int[256];

   public HistogramRed(CachableRed src) {
      super((CachableRed)src, (Map)null);
      int tiles = this.getNumXTiles() * this.getNumYTiles();
      this.computed = new boolean[tiles];
   }

   public void tallyTile(Raster r) {
      int minX = r.getMinX();
      int minY = r.getMinY();
      int w = r.getWidth();
      int h = r.getHeight();
      int[] samples = null;

      for(int y = minY; y < minY + h; ++y) {
         samples = r.getPixels(minX, y, w, 1, samples);

         for(int x = 0; x < 3 * w; ++x) {
            int val = samples[x++] * 5;
            val += samples[x++] * 9;
            val += samples[x++] * 2;
            int var10002 = this.bins[val >> 4]++;
         }
      }

      ++this.tallied;
   }

   public int[] getHistogram() {
      if (this.tallied == this.computed.length) {
         return this.bins;
      } else {
         CachableRed src = (CachableRed)this.getSources().get(0);
         int yt0 = src.getMinTileY();
         int xtiles = src.getNumXTiles();
         int xt0 = src.getMinTileX();

         for(int y = 0; y < src.getNumYTiles(); ++y) {
            for(int x = 0; x < xtiles; ++x) {
               int idx = x + xt0 + y * xtiles;
               if (!this.computed[idx]) {
                  Raster r = src.getTile(x + xt0, y + yt0);
                  this.tallyTile(r);
                  this.computed[idx] = true;
               }
            }
         }

         return this.bins;
      }
   }

   public WritableRaster copyData(WritableRaster wr) {
      this.copyToRaster(wr);
      return wr;
   }

   public Raster getTile(int tileX, int tileY) {
      int yt = tileY - this.getMinTileY();
      int xt = tileX - this.getMinTileX();
      CachableRed src = (CachableRed)this.getSources().get(0);
      Raster r = src.getTile(tileX, tileY);
      int idx = xt + yt * this.getNumXTiles();
      if (this.computed[idx]) {
         return r;
      } else {
         this.tallyTile(r);
         this.computed[idx] = true;
         return r;
      }
   }
}
