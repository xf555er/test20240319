package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SVGHintsDescriptor implements SVGDescriptor, SVGSyntax {
   private String colorInterpolation;
   private String colorRendering;
   private String textRendering;
   private String shapeRendering;
   private String imageRendering;

   public SVGHintsDescriptor(String colorInterpolation, String colorRendering, String textRendering, String shapeRendering, String imageRendering) {
      if (colorInterpolation != null && colorRendering != null && textRendering != null && shapeRendering != null && imageRendering != null) {
         this.colorInterpolation = colorInterpolation;
         this.colorRendering = colorRendering;
         this.textRendering = textRendering;
         this.shapeRendering = shapeRendering;
         this.imageRendering = imageRendering;
      } else {
         throw new SVGGraphics2DRuntimeException("none of the hints description parameters should be null");
      }
   }

   public Map getAttributeMap(Map attrMap) {
      if (attrMap == null) {
         attrMap = new HashMap();
      }

      ((Map)attrMap).put("color-interpolation", this.colorInterpolation);
      ((Map)attrMap).put("color-rendering", this.colorRendering);
      ((Map)attrMap).put("text-rendering", this.textRendering);
      ((Map)attrMap).put("shape-rendering", this.shapeRendering);
      ((Map)attrMap).put("image-rendering", this.imageRendering);
      return (Map)attrMap;
   }

   public List getDefinitionSet(List defSet) {
      if (defSet == null) {
         defSet = new LinkedList();
      }

      return (List)defSet;
   }
}
