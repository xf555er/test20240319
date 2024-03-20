package org.apache.batik.svggen;

import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGRescaleOp extends AbstractSVGFilterConverter {
   public SVGRescaleOp(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGFilterDescriptor toSVG(BufferedImageOp filter, Rectangle filterRect) {
      return filter instanceof RescaleOp ? this.toSVG((RescaleOp)filter) : null;
   }

   public SVGFilterDescriptor toSVG(RescaleOp rescaleOp) {
      SVGFilterDescriptor filterDesc = (SVGFilterDescriptor)this.descMap.get(rescaleOp);
      Document domFactory = this.generatorContext.domFactory;
      if (filterDesc == null) {
         Element filterDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "filter");
         Element feComponentTransferDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "feComponentTransfer");
         float[] offsets = rescaleOp.getOffsets((float[])null);
         float[] scaleFactors = rescaleOp.getScaleFactors((float[])null);
         if (offsets.length != scaleFactors.length) {
            throw new SVGGraphics2DRuntimeException("RescapeOp offsets and scaleFactor array length do not match");
         }

         if (offsets.length != 1 && offsets.length != 3 && offsets.length != 4) {
            throw new SVGGraphics2DRuntimeException("BufferedImage RescaleOp should have 1, 3 or 4 scale factors");
         }

         Element feFuncR = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncR");
         Element feFuncG = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncG");
         Element feFuncB = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncB");
         Element feFuncA = null;
         String type = "linear";
         String filterAttrBuf;
         if (offsets.length == 1) {
            filterAttrBuf = this.doubleString((double)scaleFactors[0]);
            String intercept = this.doubleString((double)offsets[0]);
            feFuncR.setAttributeNS((String)null, "type", type);
            feFuncG.setAttributeNS((String)null, "type", type);
            feFuncB.setAttributeNS((String)null, "type", type);
            feFuncR.setAttributeNS((String)null, "slope", filterAttrBuf);
            feFuncG.setAttributeNS((String)null, "slope", filterAttrBuf);
            feFuncB.setAttributeNS((String)null, "slope", filterAttrBuf);
            feFuncR.setAttributeNS((String)null, "intercept", intercept);
            feFuncG.setAttributeNS((String)null, "intercept", intercept);
            feFuncB.setAttributeNS((String)null, "intercept", intercept);
         } else if (offsets.length >= 3) {
            feFuncR.setAttributeNS((String)null, "type", type);
            feFuncG.setAttributeNS((String)null, "type", type);
            feFuncB.setAttributeNS((String)null, "type", type);
            feFuncR.setAttributeNS((String)null, "slope", this.doubleString((double)scaleFactors[0]));
            feFuncG.setAttributeNS((String)null, "slope", this.doubleString((double)scaleFactors[1]));
            feFuncB.setAttributeNS((String)null, "slope", this.doubleString((double)scaleFactors[2]));
            feFuncR.setAttributeNS((String)null, "intercept", this.doubleString((double)offsets[0]));
            feFuncG.setAttributeNS((String)null, "intercept", this.doubleString((double)offsets[1]));
            feFuncB.setAttributeNS((String)null, "intercept", this.doubleString((double)offsets[2]));
            if (offsets.length == 4) {
               feFuncA = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncA");
               feFuncA.setAttributeNS((String)null, "type", type);
               feFuncA.setAttributeNS((String)null, "slope", this.doubleString((double)scaleFactors[3]));
               feFuncA.setAttributeNS((String)null, "intercept", this.doubleString((double)offsets[3]));
            }
         }

         feComponentTransferDef.appendChild(feFuncR);
         feComponentTransferDef.appendChild(feFuncG);
         feComponentTransferDef.appendChild(feFuncB);
         if (feFuncA != null) {
            feComponentTransferDef.appendChild(feFuncA);
         }

         filterDef.appendChild(feComponentTransferDef);
         filterDef.setAttributeNS((String)null, "id", this.generatorContext.idGenerator.generateID("componentTransfer"));
         filterAttrBuf = "url(#" + filterDef.getAttributeNS((String)null, "id") + ")";
         filterDesc = new SVGFilterDescriptor(filterAttrBuf, filterDef);
         this.defSet.add(filterDef);
         this.descMap.put(rescaleOp, filterDesc);
      }

      return filterDesc;
   }
}
