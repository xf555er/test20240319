package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;

public class SVGFontDescriptor implements SVGDescriptor, SVGSyntax {
   private Element def;
   private String fontSize;
   private String fontWeight;
   private String fontStyle;
   private String fontFamily;

   public SVGFontDescriptor(String fontSize, String fontWeight, String fontStyle, String fontFamily, Element def) {
      if (fontSize != null && fontWeight != null && fontStyle != null && fontFamily != null) {
         this.fontSize = fontSize;
         this.fontWeight = fontWeight;
         this.fontStyle = fontStyle;
         this.fontFamily = fontFamily;
         this.def = def;
      } else {
         throw new SVGGraphics2DRuntimeException("none of the font description parameters should be null");
      }
   }

   public Map getAttributeMap(Map attrMap) {
      if (attrMap == null) {
         attrMap = new HashMap();
      }

      ((Map)attrMap).put("font-size", this.fontSize);
      ((Map)attrMap).put("font-weight", this.fontWeight);
      ((Map)attrMap).put("font-style", this.fontStyle);
      ((Map)attrMap).put("font-family", this.fontFamily);
      return (Map)attrMap;
   }

   public Element getDef() {
      return this.def;
   }

   public List getDefinitionSet(List defSet) {
      if (defSet == null) {
         defSet = new LinkedList();
      }

      if (this.def != null && !((List)defSet).contains(this.def)) {
         ((List)defSet).add(this.def);
      }

      return (List)defSet;
   }
}
