package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SVGStrokeDescriptor implements SVGDescriptor, SVGSyntax {
   private String strokeWidth;
   private String capStyle;
   private String joinStyle;
   private String miterLimit;
   private String dashArray;
   private String dashOffset;

   public SVGStrokeDescriptor(String strokeWidth, String capStyle, String joinStyle, String miterLimit, String dashArray, String dashOffset) {
      if (strokeWidth != null && capStyle != null && joinStyle != null && miterLimit != null && dashArray != null && dashOffset != null) {
         this.strokeWidth = strokeWidth;
         this.capStyle = capStyle;
         this.joinStyle = joinStyle;
         this.miterLimit = miterLimit;
         this.dashArray = dashArray;
         this.dashOffset = dashOffset;
      } else {
         throw new SVGGraphics2DRuntimeException("none of the stroke description parameters should be null");
      }
   }

   String getStrokeWidth() {
      return this.strokeWidth;
   }

   String getCapStyle() {
      return this.capStyle;
   }

   String getJoinStyle() {
      return this.joinStyle;
   }

   String getMiterLimit() {
      return this.miterLimit;
   }

   String getDashArray() {
      return this.dashArray;
   }

   String getDashOffset() {
      return this.dashOffset;
   }

   public Map getAttributeMap(Map attrMap) {
      if (attrMap == null) {
         attrMap = new HashMap();
      }

      ((Map)attrMap).put("stroke-width", this.strokeWidth);
      ((Map)attrMap).put("stroke-linecap", this.capStyle);
      ((Map)attrMap).put("stroke-linejoin", this.joinStyle);
      ((Map)attrMap).put("stroke-miterlimit", this.miterLimit);
      ((Map)attrMap).put("stroke-dasharray", this.dashArray);
      ((Map)attrMap).put("stroke-dashoffset", this.dashOffset);
      return (Map)attrMap;
   }

   public List getDefinitionSet(List defSet) {
      if (defSet == null) {
         defSet = new LinkedList();
      }

      return (List)defSet;
   }
}
