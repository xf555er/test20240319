package org.apache.batik.svggen;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.ext.awt.g2d.GraphicContext;

public class SVGComposite implements SVGConverter {
   private SVGAlphaComposite svgAlphaComposite;
   private SVGCustomComposite svgCustomComposite;

   public SVGComposite(SVGGeneratorContext generatorContext) {
      this.svgAlphaComposite = new SVGAlphaComposite(generatorContext);
      this.svgCustomComposite = new SVGCustomComposite(generatorContext);
   }

   public List getDefinitionSet() {
      List compositeDefs = new LinkedList(this.svgAlphaComposite.getDefinitionSet());
      compositeDefs.addAll(this.svgCustomComposite.getDefinitionSet());
      return compositeDefs;
   }

   public SVGAlphaComposite getAlphaCompositeConverter() {
      return this.svgAlphaComposite;
   }

   public SVGCustomComposite getCustomCompositeConverter() {
      return this.svgCustomComposite;
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      return this.toSVG(gc.getComposite());
   }

   public SVGCompositeDescriptor toSVG(Composite composite) {
      return composite instanceof AlphaComposite ? this.svgAlphaComposite.toSVG((AlphaComposite)composite) : this.svgCustomComposite.toSVG(composite);
   }
}
