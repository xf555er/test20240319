package org.apache.batik.gvt;

import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderContext;
import java.util.Hashtable;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.TileRable;
import org.apache.batik.ext.awt.image.renderable.TileRable8Bit;
import org.apache.batik.ext.awt.image.rendered.TileCacheRed;

public class PatternPaintContext implements PaintContext {
   private ColorModel rasterCM;
   private WritableRaster raster;
   private RenderedImage tiled;
   protected AffineTransform usr2dev;
   private static Rectangle EVERYTHING = new Rectangle(-536870912, -536870912, 1073741823, 1073741823);

   public AffineTransform getUsr2Dev() {
      return this.usr2dev;
   }

   public PatternPaintContext(ColorModel destCM, AffineTransform usr2dev, RenderingHints hints, Filter tile, Rectangle2D patternRegion, boolean overflow) {
      if (usr2dev == null) {
         throw new IllegalArgumentException();
      } else {
         if (hints == null) {
            hints = new RenderingHints((Map)null);
         }

         if (tile == null) {
            throw new IllegalArgumentException();
         } else {
            this.usr2dev = usr2dev;
            TileRable tileRable = new TileRable8Bit(tile, EVERYTHING, patternRegion, overflow);
            ColorSpace destCS = destCM.getColorSpace();
            if (destCS == ColorSpace.getInstance(1000)) {
               tileRable.setColorSpaceLinear(false);
            } else if (destCS == ColorSpace.getInstance(1004)) {
               tileRable.setColorSpaceLinear(true);
            }

            RenderContext rc = new RenderContext(usr2dev, EVERYTHING, hints);
            this.tiled = tileRable.createRendering(rc);
            if (this.tiled == null) {
               this.rasterCM = ColorModel.getRGBdefault();
               WritableRaster wr = this.rasterCM.createCompatibleWritableRaster(32, 32);
               this.tiled = GraphicsUtil.wrap(new BufferedImage(this.rasterCM, wr, false, (Hashtable)null));
            } else {
               Rectangle2D devRgn = usr2dev.createTransformedShape(patternRegion).getBounds();
               if (devRgn.getWidth() > 128.0 || devRgn.getHeight() > 128.0) {
                  this.tiled = new TileCacheRed(GraphicsUtil.wrap(this.tiled), 256, 64);
               }

               this.rasterCM = this.tiled.getColorModel();
               if (this.rasterCM.hasAlpha()) {
                  if (destCM.hasAlpha()) {
                     this.rasterCM = GraphicsUtil.coerceColorModel(this.rasterCM, destCM.isAlphaPremultiplied());
                  } else {
                     this.rasterCM = GraphicsUtil.coerceColorModel(this.rasterCM, false);
                  }
               }

            }
         }
      }
   }

   public void dispose() {
      this.raster = null;
   }

   public ColorModel getColorModel() {
      return this.rasterCM;
   }

   public Raster getRaster(int x, int y, int width, int height) {
      if (this.raster == null || this.raster.getWidth() < width || this.raster.getHeight() < height) {
         this.raster = this.rasterCM.createCompatibleWritableRaster(width, height);
      }

      WritableRaster wr = this.raster.createWritableChild(0, 0, width, height, x, y, (int[])null);
      this.tiled.copyData(wr);
      GraphicsUtil.coerceData(wr, this.tiled.getColorModel(), this.rasterCM.isAlphaPremultiplied());
      return (Raster)(this.raster.getWidth() == width && this.raster.getHeight() == height ? this.raster : wr.createTranslatedChild(0, 0));
   }
}
