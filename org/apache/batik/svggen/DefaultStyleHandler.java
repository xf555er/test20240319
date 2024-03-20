package org.apache.batik.svggen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

public class DefaultStyleHandler implements StyleHandler, SVGConstants {
   static Map ignoreAttributes = new HashMap();

   public void setStyle(Element element, Map styleMap, SVGGeneratorContext generatorContext) {
      String tagName = element.getTagName();
      Iterator var5 = styleMap.keySet().iterator();

      while(var5.hasNext()) {
         Object o = var5.next();
         String styleName = (String)o;
         if (element.getAttributeNS((String)null, styleName).length() == 0 && this.appliesTo(styleName, tagName)) {
            element.setAttributeNS((String)null, styleName, (String)styleMap.get(styleName));
         }
      }

   }

   protected boolean appliesTo(String styleName, String tagName) {
      Set s = (Set)ignoreAttributes.get(tagName);
      if (s == null) {
         return true;
      } else {
         return !s.contains(styleName);
      }
   }

   static {
      Set textAttributes = new HashSet();
      textAttributes.add("font-size");
      textAttributes.add("font-family");
      textAttributes.add("font-style");
      textAttributes.add("font-weight");
      ignoreAttributes.put("rect", textAttributes);
      ignoreAttributes.put("circle", textAttributes);
      ignoreAttributes.put("ellipse", textAttributes);
      ignoreAttributes.put("polygon", textAttributes);
      ignoreAttributes.put("polygon", textAttributes);
      ignoreAttributes.put("line", textAttributes);
      ignoreAttributes.put("path", textAttributes);
   }
}
