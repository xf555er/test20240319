package org.apache.batik.ext.awt.image.renderable;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BumpMap;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.DiffuseLightingRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;

public class DiffuseLightingRable8Bit extends AbstractColorInterpolationRable implements DiffuseLightingRable {
   private double surfaceScale;
   private double kd;
   private Light light;
   private Rectangle2D litRegion;
   private float[] kernelUnitLength = null;

   public DiffuseLightingRable8Bit(Filter src, Rectangle2D litRegion, Light light, double kd, double surfaceScale, double[] kernelUnitLength) {
      super((Filter)src, (Map)null);
      this.setLight(light);
      this.setKd(kd);
      this.setSurfaceScale(surfaceScale);
      this.setLitRegion(litRegion);
      this.setKernelUnitLength(kernelUnitLength);
   }

   public Filter getSource() {
      return (Filter)this.getSources().get(0);
   }

   public void setSource(Filter src) {
      this.init(src, (Map)null);
   }

   public Rectangle2D getBounds2D() {
      return (Rectangle2D)((Rectangle2D)this.litRegion.clone());
   }

   public Rectangle2D getLitRegion() {
      return this.getBounds2D();
   }

   public void setLitRegion(Rectangle2D litRegion) {
      this.touch();
      this.litRegion = litRegion;
   }

   public Light getLight() {
      return this.light;
   }

   public void setLight(Light light) {
      this.touch();
      this.light = light;
   }

   public double getSurfaceScale() {
      return this.surfaceScale;
   }

   public void setSurfaceScale(double surfaceScale) {
      this.touch();
      this.surfaceScale = surfaceScale;
   }

   public double getKd() {
      return this.kd;
   }

   public void setKd(double kd) {
      this.touch();
      this.kd = kd;
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

   public RenderedImage createRendering(RenderContext rc) {
      Shape aoi = rc.getAreaOfInterest();
      if (aoi == null) {
         aoi = this.getBounds2D();
      }

      Rectangle2D aoiR = ((Shape)aoi).getBounds2D();
      Rectangle2D.intersect(aoiR, this.getBounds2D(), aoiR);
      AffineTransform at = rc.getTransform();
      Rectangle devRect = at.createTransformedShape(aoiR).getBounds();
      if (devRect.width != 0 && devRect.height != 0) {
         double sx = at.getScaleX();
         double sy = at.getScaleY();
         double shx = at.getShearX();
         double shy = at.getShearY();
         double tx = at.getTranslateX();
         double ty = at.getTranslateY();
         double scaleX = Math.sqrt(sx * sx + shy * shy);
         double scaleY = Math.sqrt(sy * sy + shx * shx);
         if (scaleX != 0.0 && scaleY != 0.0) {
            if (this.kernelUnitLength != null) {
               if (this.kernelUnitLength[0] > 0.0F && scaleX > (double)(1.0F / this.kernelUnitLength[0])) {
                  scaleX = (double)(1.0F / this.kernelUnitLength[0]);
               }

               if (this.kernelUnitLength[1] > 0.0F && scaleY > (double)(1.0F / this.kernelUnitLength[1])) {
                  scaleY = (double)(1.0F / this.kernelUnitLength[1]);
               }
            }

            AffineTransform scale = AffineTransform.getScaleInstance(scaleX, scaleY);
            devRect = scale.createTransformedShape(aoiR).getBounds();
            aoiR.setRect(aoiR.getX() - 2.0 / scaleX, aoiR.getY() - 2.0 / scaleY, aoiR.getWidth() + 4.0 / scaleX, aoiR.getHeight() + 4.0 / scaleY);
            rc = (RenderContext)rc.clone();
            rc.setAreaOfInterest(aoiR);
            rc.setTransform(scale);
            CachableRed cr = GraphicsUtil.wrap(this.getSource().createRendering(rc));
            BumpMap bumpMap = new BumpMap(cr, this.surfaceScale, scaleX, scaleY);
            CachableRed cr = new DiffuseLightingRed(this.kd, this.light, bumpMap, devRect, 1.0 / scaleX, 1.0 / scaleY, this.isColorSpaceLinear());
            AffineTransform shearAt = new AffineTransform(sx / scaleX, shy / scaleX, shx / scaleY, sy / scaleY, tx, ty);
            if (!shearAt.isIdentity()) {
               RenderingHints rh = rc.getRenderingHints();
               Rectangle padRect = new Rectangle(devRect.x - 1, devRect.y - 1, devRect.width + 2, devRect.height + 2);
               CachableRed cr = new PadRed((CachableRed)cr, padRect, PadMode.REPLICATE, rh);
               cr = new AffineRed(cr, shearAt, rh);
            }

            return (RenderedImage)cr;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }
}
