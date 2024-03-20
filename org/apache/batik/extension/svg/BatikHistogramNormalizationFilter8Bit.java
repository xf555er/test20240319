package org.apache.batik.extension.svg;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.RenderContext;
import java.util.Map;
import org.apache.batik.ext.awt.image.LinearTransfer;
import org.apache.batik.ext.awt.image.TransferFunction;
import org.apache.batik.ext.awt.image.renderable.AbstractColorInterpolationRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.rendered.ComponentTransferRed;

public class BatikHistogramNormalizationFilter8Bit extends AbstractColorInterpolationRable implements BatikHistogramNormalizationFilter {
   private float trim = 0.01F;
   protected int[] histo = null;
   protected float slope;
   protected float intercept;

   public void setSource(Filter src) {
      this.init(src, (Map)null);
   }

   public Filter getSource() {
      return (Filter)this.getSources().get(0);
   }

   public float getTrim() {
      return this.trim;
   }

   public void setTrim(float trim) {
      this.trim = trim;
      this.touch();
   }

   public BatikHistogramNormalizationFilter8Bit(Filter src, float trim) {
      this.setSource(src);
      this.setTrim(trim);
   }

   public void computeHistogram(RenderContext rc) {
      if (this.histo == null) {
         Filter src = this.getSource();
         float scale = 100.0F / src.getWidth();
         float yscale = 100.0F / src.getHeight();
         if (scale > yscale) {
            scale = yscale;
         }

         AffineTransform at = AffineTransform.getScaleInstance((double)scale, (double)scale);
         rc = new RenderContext(at, rc.getRenderingHints());
         RenderedImage histRI = this.getSource().createRendering(rc);
         this.histo = (new HistogramRed(this.convertSourceCS(histRI))).getHistogram();
         int t = (int)((double)((float)(histRI.getWidth() * histRI.getHeight()) * this.trim) + 0.5);
         int c = 0;

         int i;
         for(i = 0; i < 255; ++i) {
            c += this.histo[i];
            if (c >= t) {
               break;
            }
         }

         int low = i;
         c = 0;

         for(i = 255; i > 0; --i) {
            c += this.histo[i];
            if (c >= t) {
               break;
            }
         }

         this.slope = 255.0F / (float)(i - low);
         this.intercept = this.slope * (float)(-low) / 255.0F;
      }
   }

   public RenderedImage createRendering(RenderContext rc) {
      RenderedImage srcRI = this.getSource().createRendering(rc);
      if (srcRI == null) {
         return null;
      } else {
         this.computeHistogram(rc);
         SampleModel sm = srcRI.getSampleModel();
         int bands = sm.getNumBands();
         TransferFunction[] tfs = new TransferFunction[bands];
         TransferFunction tf = new LinearTransfer(this.slope, this.intercept);

         for(int i = 0; i < tfs.length; ++i) {
            tfs[i] = tf;
         }

         return new ComponentTransferRed(this.convertSourceCS(srcRI), tfs, (RenderingHints)null);
      }
   }
}
