package org.apache.batik.svggen;

import java.awt.Paint;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.w3c.dom.Element;

public class SVGCustomPaint extends AbstractSVGConverter {
   public SVGCustomPaint(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      return this.toSVG(gc.getPaint());
   }

   public SVGPaintDescriptor toSVG(Paint paint) {
      SVGPaintDescriptor paintDesc = (SVGPaintDescriptor)this.descMap.get(paint);
      if (paintDesc == null) {
         paintDesc = this.generatorContext.extensionHandler.handlePaint(paint, this.generatorContext);
         if (paintDesc != null) {
            Element def = paintDesc.getDef();
            if (def != null) {
               this.defSet.add(def);
            }

            this.descMap.put(paint, paintDesc);
         }
      }

      return paintDesc;
   }
}
