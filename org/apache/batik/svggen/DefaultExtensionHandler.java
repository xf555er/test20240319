package org.apache.batik.svggen;

import java.awt.Composite;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;

public class DefaultExtensionHandler implements ExtensionHandler {
   public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext generatorContext) {
      return null;
   }

   public SVGCompositeDescriptor handleComposite(Composite composite, SVGGeneratorContext generatorContext) {
      return null;
   }

   public SVGFilterDescriptor handleFilter(BufferedImageOp filter, Rectangle filterRect, SVGGeneratorContext generatorContext) {
      return null;
   }
}
