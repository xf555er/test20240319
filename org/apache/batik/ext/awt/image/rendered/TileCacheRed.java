package org.apache.batik.ext.awt.image.rendered;

import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;

public class TileCacheRed extends AbstractTiledRed {
   public TileCacheRed(CachableRed cr) {
      super((CachableRed)cr, (Map)null);
   }

   public TileCacheRed(CachableRed cr, int tileWidth, int tileHeight) {
      ColorModel cm = cr.getColorModel();
      Rectangle bounds = cr.getBounds();
      if (tileWidth > bounds.width) {
         tileWidth = bounds.width;
      }

      if (tileHeight > bounds.height) {
         tileHeight = bounds.height;
      }

      SampleModel sm = cm.createCompatibleSampleModel(tileWidth, tileHeight);
      this.init(cr, bounds, cm, sm, cr.getTileGridXOffset(), cr.getTileGridYOffset(), (Map)null);
   }

   public void genRect(WritableRaster wr) {
      CachableRed src = (CachableRed)this.getSources().get(0);
      src.copyData(wr);
   }

   public void flushCache(Rectangle rect) {
      int tx0 = this.getXTile(rect.x);
      int ty0 = this.getYTile(rect.y);
      int tx1 = this.getXTile(rect.x + rect.width - 1);
      int ty1 = this.getYTile(rect.y + rect.height - 1);
      if (tx0 < this.minTileX) {
         tx0 = this.minTileX;
      }

      if (ty0 < this.minTileY) {
         ty0 = this.minTileY;
      }

      if (tx1 >= this.minTileX + this.numXTiles) {
         tx1 = this.minTileX + this.numXTiles - 1;
      }

      if (ty1 >= this.minTileY + this.numYTiles) {
         ty1 = this.minTileY + this.numYTiles - 1;
      }

      if (tx1 >= tx0 && ty1 >= ty0) {
         TileStore store = this.getTileStore();

         for(int y = ty0; y <= ty1; ++y) {
            for(int x = tx0; x <= tx1; ++x) {
               store.setTile(x, y, (Raster)null);
            }
         }

      }
   }
}
