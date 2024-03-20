package org.apache.batik.svggen;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.ext.awt.g2d.GraphicContext;

public class SVGPaint implements SVGConverter {
   private SVGLinearGradient svgLinearGradient;
   private SVGTexturePaint svgTexturePaint;
   private SVGColor svgColor;
   private SVGCustomPaint svgCustomPaint;
   private SVGGeneratorContext generatorContext;

   public SVGPaint(SVGGeneratorContext generatorContext) {
      this.svgLinearGradient = new SVGLinearGradient(generatorContext);
      this.svgTexturePaint = new SVGTexturePaint(generatorContext);
      this.svgCustomPaint = new SVGCustomPaint(generatorContext);
      this.svgColor = new SVGColor(generatorContext);
      this.generatorContext = generatorContext;
   }

   public List getDefinitionSet() {
      List paintDefs = new LinkedList(this.svgLinearGradient.getDefinitionSet());
      paintDefs.addAll(this.svgTexturePaint.getDefinitionSet());
      paintDefs.addAll(this.svgCustomPaint.getDefinitionSet());
      paintDefs.addAll(this.svgColor.getDefinitionSet());
      return paintDefs;
   }

   public SVGTexturePaint getTexturePaintConverter() {
      return this.svgTexturePaint;
   }

   public SVGLinearGradient getGradientPaintConverter() {
      return this.svgLinearGradient;
   }

   public SVGCustomPaint getCustomPaintConverter() {
      return this.svgCustomPaint;
   }

   public SVGColor getColorConverter() {
      return this.svgColor;
   }

   public SVGDescriptor toSVG(GraphicContext gc) {
      return this.toSVG(gc.getPaint());
   }

   public SVGPaintDescriptor toSVG(Paint paint) {
      SVGPaintDescriptor paintDesc = this.svgCustomPaint.toSVG(paint);
      if (paintDesc == null) {
         if (paint instanceof Color) {
            paintDesc = SVGColor.toSVG((Color)paint, this.generatorContext);
         } else if (paint instanceof GradientPaint) {
            paintDesc = this.svgLinearGradient.toSVG((GradientPaint)paint);
         } else if (paint instanceof TexturePaint) {
            paintDesc = this.svgTexturePaint.toSVG((TexturePaint)paint);
         }
      }

      return paintDesc;
   }
}
