package org.apache.batik.svggen;

public abstract class SVGGraphicObjectConverter implements SVGSyntax {
   protected SVGGeneratorContext generatorContext;

   public SVGGraphicObjectConverter(SVGGeneratorContext generatorContext) {
      if (generatorContext == null) {
         throw new SVGGraphics2DRuntimeException("generatorContext should not be null");
      } else {
         this.generatorContext = generatorContext;
      }
   }

   public final String doubleString(double value) {
      return this.generatorContext.doubleString(value);
   }
}
