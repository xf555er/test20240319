package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.ext.awt.g2d.TransformStackElement;

public class SVGGraphicContextConverter {
   private static final int GRAPHIC_CONTEXT_CONVERTER_COUNT = 6;
   private SVGTransform transformConverter;
   private SVGPaint paintConverter;
   private SVGBasicStroke strokeConverter;
   private SVGComposite compositeConverter;
   private SVGClip clipConverter;
   private SVGRenderingHints hintsConverter;
   private SVGFont fontConverter;
   private SVGConverter[] converters = new SVGConverter[6];

   public SVGTransform getTransformConverter() {
      return this.transformConverter;
   }

   public SVGPaint getPaintConverter() {
      return this.paintConverter;
   }

   public SVGBasicStroke getStrokeConverter() {
      return this.strokeConverter;
   }

   public SVGComposite getCompositeConverter() {
      return this.compositeConverter;
   }

   public SVGClip getClipConverter() {
      return this.clipConverter;
   }

   public SVGRenderingHints getHintsConverter() {
      return this.hintsConverter;
   }

   public SVGFont getFontConverter() {
      return this.fontConverter;
   }

   public SVGGraphicContextConverter(SVGGeneratorContext generatorContext) {
      if (generatorContext == null) {
         throw new SVGGraphics2DRuntimeException("generatorContext should not be null");
      } else {
         this.transformConverter = new SVGTransform(generatorContext);
         this.paintConverter = new SVGPaint(generatorContext);
         this.strokeConverter = new SVGBasicStroke(generatorContext);
         this.compositeConverter = new SVGComposite(generatorContext);
         this.clipConverter = new SVGClip(generatorContext);
         this.hintsConverter = new SVGRenderingHints(generatorContext);
         this.fontConverter = new SVGFont(generatorContext);
         int i = 0;
         this.converters[i++] = this.paintConverter;
         this.converters[i++] = this.strokeConverter;
         this.converters[i++] = this.compositeConverter;
         this.converters[i++] = this.clipConverter;
         this.converters[i++] = this.hintsConverter;
         this.converters[i++] = this.fontConverter;
      }
   }

   public String toSVG(TransformStackElement[] transformStack) {
      return this.transformConverter.toSVGTransform(transformStack);
   }

   public SVGGraphicContext toSVG(GraphicContext gc) {
      Map groupAttrMap = new HashMap();
      SVGConverter[] var3 = this.converters;
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         SVGConverter converter = var3[var5];
         SVGDescriptor desc = converter.toSVG(gc);
         if (desc != null) {
            desc.getAttributeMap(groupAttrMap);
         }
      }

      return new SVGGraphicContext(groupAttrMap, gc.getTransformStack());
   }

   public List getDefinitionSet() {
      List defSet = new LinkedList();
      SVGConverter[] var2 = this.converters;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         SVGConverter converter = var2[var4];
         defSet.addAll(converter.getDefinitionSet());
      }

      return defSet;
   }
}
