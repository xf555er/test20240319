package org.apache.batik.ext.awt.image.rendered;

import java.awt.image.Raster;
import org.apache.batik.util.HaltingThread;

public class TileGrid implements TileStore {
   private static final boolean DEBUG = false;
   private static final boolean COUNT = false;
   private int xSz;
   private int ySz;
   private int minTileX;
   private int minTileY;
   private TileLRUMember[][] rasters = (TileLRUMember[][])null;
   private TileGenerator source = null;
   private LRUCache cache = null;
   static int requests;
   static int misses;

   public TileGrid(int minTileX, int minTileY, int xSz, int ySz, TileGenerator source, LRUCache cache) {
      this.cache = cache;
      this.source = source;
      this.minTileX = minTileX;
      this.minTileY = minTileY;
      this.xSz = xSz;
      this.ySz = ySz;
      this.rasters = new TileLRUMember[ySz][];
   }

   public void setTile(int x, int y, Raster ras) {
      x -= this.minTileX;
      y -= this.minTileY;
      if (x >= 0 && x < this.xSz) {
         if (y >= 0 && y < this.ySz) {
            TileLRUMember[] row = this.rasters[y];
            TileLRUMember item;
            if (ras == null) {
               if (row != null) {
                  item = row[x];
                  if (item != null) {
                     row[x] = null;
                     this.cache.remove(item);
                  }
               }
            } else {
               if (row != null) {
                  item = row[x];
                  if (item == null) {
                     item = new TileLRUMember();
                     row[x] = item;
                  }
               } else {
                  row = new TileLRUMember[this.xSz];
                  item = new TileLRUMember();
                  row[x] = item;
                  this.rasters[y] = row;
               }

               item.setRaster(ras);
               this.cache.add(item);
            }
         }
      }
   }

   public Raster getTileNoCompute(int x, int y) {
      x -= this.minTileX;
      y -= this.minTileY;
      if (x >= 0 && x < this.xSz) {
         if (y >= 0 && y < this.ySz) {
            TileLRUMember[] row = this.rasters[y];
            if (row == null) {
               return null;
            } else {
               TileLRUMember item = row[x];
               if (item == null) {
                  return null;
               } else {
                  Raster ret = item.retrieveRaster();
                  if (ret != null) {
                     this.cache.add(item);
                  }

                  return ret;
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public Raster getTile(int x, int y) {
      x -= this.minTileX;
      y -= this.minTileY;
      if (x >= 0 && x < this.xSz) {
         if (y >= 0 && y < this.ySz) {
            Raster ras = null;
            TileLRUMember[] row = this.rasters[y];
            TileLRUMember item = null;
            if (row != null) {
               item = row[x];
               if (item != null) {
                  ras = item.retrieveRaster();
               } else {
                  item = new TileLRUMember();
                  row[x] = item;
               }
            } else {
               row = new TileLRUMember[this.xSz];
               this.rasters[y] = row;
               item = new TileLRUMember();
               row[x] = item;
            }

            if (ras == null) {
               ras = this.source.genTile(x + this.minTileX, y + this.minTileY);
               if (HaltingThread.hasBeenHalted()) {
                  return ras;
               }

               item.setRaster(ras);
            }

            this.cache.add(item);
            return ras;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
