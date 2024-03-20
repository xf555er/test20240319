package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class SVGCompositeDescriptor implements SVGDescriptor, SVGSyntax {
   private Element def;
   private String opacityValue;
   private String filterValue;

   public SVGCompositeDescriptor(String opacityValue, String filterValue) {
      this.opacityValue = opacityValue;
      this.filterValue = filterValue;
   }

   public SVGCompositeDescriptor(String opacityValue, String filterValue, Element def) {
      this(opacityValue, filterValue);
      this.def = def;
   }

   public String getOpacityValue() {
      return this.opacityValue;
   }

   public String getFilterValue() {
      return this.filterValue;
   }

   public Element getDef() {
      return this.def;
   }

   public Map getAttributeMap(Map attrMap) {
      if (attrMap == null) {
         attrMap = new HashMap();
      }

      ((Map)attrMap).put("opacity", this.opacityValue);
      ((Map)attrMap).put("filter", this.filterValue);
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
