package org.apache.batik.svggen;

import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import org.w3c.dom.Element;

public class SVGCustomBufferedImageOp extends AbstractSVGFilterConverter {
   private static final String ERROR_EXTENSION = "SVGCustomBufferedImageOp:: ExtensionHandler could not convert filter";

   public SVGCustomBufferedImageOp(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGFilterDescriptor toSVG(BufferedImageOp filter, Rectangle filterRect) {
      SVGFilterDescriptor filterDesc = (SVGFilterDescriptor)this.descMap.get(filter);
      if (filterDesc == null) {
         filterDesc = this.generatorContext.extensionHandler.handleFilter(filter, filterRect, this.generatorContext);
         if (filterDesc != null) {
            Element def = filterDesc.getDef();
            if (def != null) {
               this.defSet.add(def);
            }

            this.descMap.put(filter, filterDesc);
         } else {
            System.err.println("SVGCustomBufferedImageOp:: ExtensionHandler could not convert filter");
         }
      }

      return filterDesc;
   }
}
