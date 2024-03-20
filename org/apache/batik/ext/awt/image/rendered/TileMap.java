package org.apache.batik.ext.awt.image.rendered;

import java.awt.Point;
import java.awt.image.Raster;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import org.apache.batik.util.CleanerThread;
import org.apache.batik.util.HaltingThread;

public class TileMap implements TileStore {
   private static final boolean DEBUG = false;
   private static final boolean COUNT = false;
   private HashMap rasters = new HashMap();
   private TileGenerator source = null;
   private LRUCache cache = null;
   static int requests;
   static int misses;

   public TileMap(TileGenerator source, LRUCache cache) {
      this.cache = cache;
      this.source = source;
   }

   public void setTile(int x, int y, Raster ras) {
      Point pt = new Point(x, y);
      Object o;
      if (ras == null) {
         o = this.rasters.remove(pt);
         if (o != null) {
            this.cache.remove((TileMapLRUMember)o);
         }

      } else {
         o = this.rasters.get(pt);
         TileMapLRUMember item;
         if (o == null) {
            item = new TileMapLRUMember(this, pt, ras);
            this.rasters.put(pt, item);
         } else {
            item = (TileMapLRUMember)o;
            item.setRaster(ras);
         }

         this.cache.add(item);
      }
   }

   public Raster getTileNoCompute(int x, int y) {
      Point pt = new Point(x, y);
      Object o = this.rasters.get(pt);
      if (o == null) {
         return null;
      } else {
         TileMapLRUMember item = (TileMapLRUMember)o;
         Raster ret = item.retrieveRaster();
         if (ret != null) {
            this.cache.add(item);
         }

         return ret;
      }
   }

   public Raster getTile(int x, int y) {
      Raster ras = null;
      Point pt = new Point(x, y);
      Object o = this.rasters.get(pt);
      TileMapLRUMember item = null;
      if (o != null) {
         item = (TileMapLRUMember)o;
         ras = item.retrieveRaster();
      }

      if (ras == null) {
         ras = this.source.genTile(x, y);
         if (HaltingThread.hasBeenHalted()) {
            return ras;
         }

         if (item != null) {
            item.setRaster(ras);
         } else {
            item = new TileMapLRUMember(this, pt, ras);
            this.rasters.put(pt, item);
         }
      }

      this.cache.add(item);
      return ras;
   }

   static class TileMapLRUMember extends TileLRUMember {
      public Point pt;
      public SoftReference parent;

      TileMapLRUMember(TileMap parent, Point pt, Raster ras) {
         super(ras);
         this.parent = new SoftReference(parent);
         this.pt = pt;
      }

      public void setRaster(Raster ras) {
         this.hRaster = ras;
         this.wRaster = new RasterSoftRef(ras);
      }

      class RasterSoftRef extends CleanerThread.SoftReferenceCleared {
         RasterSoftRef(Object o) {
            super(o);
         }

         public void cleared() {
            TileMap tm = (TileMap)TileMapLRUMember.this.parent.get();
            if (tm != null) {
               tm.rasters.remove(TileMapLRUMember.this.pt);
            }

         }
      }
   }
}
