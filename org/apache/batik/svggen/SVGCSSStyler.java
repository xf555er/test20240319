package org.apache.batik.svggen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SVGCSSStyler implements SVGSyntax {
   private static final char CSS_PROPERTY_VALUE_SEPARATOR = ':';
   private static final char CSS_RULE_SEPARATOR = ';';
   private static final char SPACE = ' ';

   public static void style(Node node) {
      NamedNodeMap attributes = node.getAttributes();
      int i;
      if (attributes != null) {
         Element element = (Element)node;
         StringBuffer styleAttrBuffer = new StringBuffer();
         i = attributes.getLength();
         List toBeRemoved = new ArrayList();

         int n;
         for(n = 0; n < i; ++n) {
            Attr attr = (Attr)attributes.item(n);
            String attrName = attr.getName();
            if (SVGStylingAttributes.set.contains(attrName)) {
               styleAttrBuffer.append(attrName);
               styleAttrBuffer.append(':');
               styleAttrBuffer.append(attr.getValue());
               styleAttrBuffer.append(';');
               styleAttrBuffer.append(' ');
               toBeRemoved.add(attrName);
            }
         }

         if (styleAttrBuffer.length() > 0) {
            element.setAttributeNS((String)null, "style", styleAttrBuffer.toString().trim());
            n = toBeRemoved.size();
            Iterator var12 = toBeRemoved.iterator();

            while(var12.hasNext()) {
               Object aToBeRemoved = var12.next();
               element.removeAttribute((String)aToBeRemoved);
            }
         }
      }

      NodeList children = node.getChildNodes();
      int nChildren = children.getLength();

      for(i = 0; i < nChildren; ++i) {
         Node child = children.item(i);
         style(child);
      }

   }
}
