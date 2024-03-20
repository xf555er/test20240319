package org.apache.batik.svggen;

import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGConvolveOp extends AbstractSVGFilterConverter {
   public SVGConvolveOp(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGFilterDescriptor toSVG(BufferedImageOp filter, Rectangle filterRect) {
      return filter instanceof ConvolveOp ? this.toSVG((ConvolveOp)filter) : null;
   }

   public SVGFilterDescriptor toSVG(ConvolveOp convolveOp) {
      SVGFilterDescriptor filterDesc = (SVGFilterDescriptor)this.descMap.get(convolveOp);
      Document domFactory = this.generatorContext.domFactory;
      if (filterDesc == null) {
         Kernel kernel = convolveOp.getKernel();
         Element filterDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "filter");
         Element feConvolveMatrixDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "feConvolveMatrix");
         feConvolveMatrixDef.setAttributeNS((String)null, "order", kernel.getWidth() + " " + kernel.getHeight());
         float[] data = kernel.getKernelData((float[])null);
         StringBuffer kernelMatrixBuf = new StringBuffer(data.length * 8);
         float[] var9 = data;
         int var10 = data.length;

         for(int var11 = 0; var11 < var10; ++var11) {
            float aData = var9[var11];
            kernelMatrixBuf.append(this.doubleString((double)aData));
            kernelMatrixBuf.append(" ");
         }

         feConvolveMatrixDef.setAttributeNS((String)null, "kernelMatrix", kernelMatrixBuf.toString().trim());
         filterDef.appendChild(feConvolveMatrixDef);
         filterDef.setAttributeNS((String)null, "id", this.generatorContext.idGenerator.generateID("convolve"));
         if (convolveOp.getEdgeCondition() == 1) {
            feConvolveMatrixDef.setAttributeNS((String)null, "edgeMode", "duplicate");
         } else {
            feConvolveMatrixDef.setAttributeNS((String)null, "edgeMode", "none");
         }

         StringBuffer filterAttrBuf = new StringBuffer("url(");
         filterAttrBuf.append("#");
         filterAttrBuf.append(filterDef.getAttributeNS((String)null, "id"));
         filterAttrBuf.append(")");
         filterDesc = new SVGFilterDescriptor(filterAttrBuf.toString(), filterDef);
         this.defSet.add(filterDef);
         this.descMap.put(convolveOp, filterDesc);
      }

      return filterDesc;
   }
}
