package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class SVGClipDescriptor implements SVGDescriptor, SVGSyntax {
   private String clipPathValue;
   private Element clipPathDef;

   public SVGClipDescriptor(String clipPathValue, Element clipPathDef) {
      if (clipPathValue == null) {
         throw new SVGGraphics2DRuntimeException("clipPathValue should not be null");
      } else {
         this.clipPathValue = clipPathValue;
         this.clipPathDef = clipPathDef;
      }
   }

   public Map getAttributeMap(Map attrMap) {
      if (attrMap == null) {
         attrMap = new HashMap();
      }

      ((Map)attrMap).put("clip-path", this.clipPathValue);
      return (Map)attrMap;
   }

   public List getDefinitionSet(List defSet) {
      if (defSet == null) {
         defSet = new LinkedList();
      }

      if (this.clipPathDef != null) {
         ((List)defSet).add(this.clipPathDef);
      }

      return (List)defSet;
   }
}
