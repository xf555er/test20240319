package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class SVGPaintDescriptor implements SVGDescriptor, SVGSyntax {
   private Element def;
   private String paintValue;
   private String opacityValue;

   public SVGPaintDescriptor(String paintValue, String opacityValue) {
      this.paintValue = paintValue;
      this.opacityValue = opacityValue;
   }

   public SVGPaintDescriptor(String paintValue, String opacityValue, Element def) {
      this(paintValue, opacityValue);
      this.def = def;
   }

   public String getPaintValue() {
      return this.paintValue;
   }

   public String getOpacityValue() {
      return this.opacityValue;
   }

   public Element getDef() {
      return this.def;
   }

   public Map getAttributeMap(Map attrMap) {
      if (attrMap == null) {
         attrMap = new HashMap();
      }

      ((Map)attrMap).put("fill", this.paintValue);
      ((Map)attrMap).put("stroke", this.paintValue);
      ((Map)attrMap).put("fill-opacity", this.opacityValue);
      ((Map)attrMap).put("stroke-opacity", this.opacityValue);
      return (Map)attrMap;
   }

   public List getDefinitionSet(List defSet) {
      if (defSet == null) {
         defSet = new LinkedList();
      }

      if (this.def != null) {
         ((List)defSet).add(this.def);
      }

      return (List)defSet;
   }
}
