package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractSVGFilterConverter implements SVGFilterConverter, ErrorConstants {
   protected SVGGeneratorContext generatorContext;
   protected Map descMap = new HashMap();
   protected List defSet = new LinkedList();

   public AbstractSVGFilterConverter(SVGGeneratorContext generatorContext) {
      if (generatorContext == null) {
         throw new SVGGraphics2DRuntimeException("generatorContext should not be null");
      } else {
         this.generatorContext = generatorContext;
      }
   }

   public List getDefinitionSet() {
      return this.defSet;
   }

   public final String doubleString(double value) {
      return this.generatorContext.doubleString(value);
   }
}
