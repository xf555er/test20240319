package org.apache.batik.ext.awt.image.renderable;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.rendered.TurbulencePatternRed;

public class TurbulenceRable8Bit extends AbstractColorInterpolationRable implements TurbulenceRable {
   int seed = 0;
   int numOctaves = 1;
   double baseFreqX = 0.0;
   double baseFreqY = 0.0;
   boolean stitched = false;
   boolean fractalNoise = false;
   Rectangle2D region;

   public TurbulenceRable8Bit(Rectangle2D region) {
      this.region = region;
   }

   public TurbulenceRable8Bit(Rectangle2D region, int seed, int numOctaves, double baseFreqX, double baseFreqY, boolean stitched, boolean fractalNoise) {
      this.seed = seed;
      this.numOctaves = numOctaves;
      this.baseFreqX = baseFreqX;
      this.baseFreqY = baseFreqY;
      this.stitched = stitched;
      this.fractalNoise = fractalNoise;
      this.region = region;
   }

   public Rectangle2D getTurbulenceRegion() {
      return (Rectangle2D)this.region.clone();
   }

   public Rectangle2D getBounds2D() {
      return (Rectangle2D)this.region.clone();
   }

   public int getSeed() {
      return this.seed;
   }

   public int getNumOctaves() {
      return this.numOctaves;
   }

   public double getBaseFrequencyX() {
      return this.baseFreqX;
   }

   public double getBaseFrequencyY() {
      return this.baseFreqY;
   }

   public boolean isStitched() {
      return this.stitched;
   }

   public boolean isFractalNoise() {
      return this.fractalNoise;
   }

   public void setTurbulenceRegion(Rectangle2D turbulenceRegion) {
      this.touch();
      this.region = turbulenceRegion;
   }

   public void setSeed(int seed) {
      this.touch();
      this.seed = seed;
   }

   public void setNumOctaves(int numOctaves) {
      this.touch();
      this.numOctaves = numOctaves;
   }

   public void setBaseFrequencyX(double baseFreqX) {
      this.touch();
      this.baseFreqX = baseFreqX;
   }

   public void setBaseFrequencyY(double baseFreqY) {
      this.touch();
      this.baseFreqY = baseFreqY;
   }

   public void setStitched(boolean stitched) {
      this.touch();
      this.stitched = stitched;
   }

   public void setFractalNoise(boolean fractalNoise) {
      this.touch();
      this.fractalNoise = fractalNoise;
   }

   public RenderedImage createRendering(RenderContext rc) {
      Shape aoi = rc.getAreaOfInterest();
      Rectangle2D aoiRect;
      if (aoi == null) {
         aoiRect = this.getBounds2D();
      } else {
         Rectangle2D rect = this.getBounds2D();
         aoiRect = aoi.getBounds2D();
         if (!aoiRect.intersects(rect)) {
            return null;
         }

         Rectangle2D.intersect(aoiRect, rect, aoiRect);
      }

      AffineTransform usr2dev = rc.getTransform();
      Rectangle devRect = usr2dev.createTransformedShape(aoiRect).getBounds();
      if (devRect.width > 0 && devRect.height > 0) {
         ColorSpace cs = this.getOperationColorSpace();
         Rectangle2D tile = null;
         if (this.stitched) {
            tile = (Rectangle2D)this.region.clone();
         }

         AffineTransform patternTxf = new AffineTransform();

         try {
            patternTxf = usr2dev.createInverse();
         } catch (NoninvertibleTransformException var10) {
         }

         return new TurbulencePatternRed(this.baseFreqX, this.baseFreqY, this.numOctaves, this.seed, this.fractalNoise, tile, patternTxf, devRect, cs, true);
      } else {
         return null;
      }
   }
}
