package org.apache.batik.dom.util;

import org.apache.batik.constants.XMLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public class XLinkSupport implements XMLConstants {
   public static String getXLinkType(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "type");
   }

   public static void setXLinkType(Element elt, String str) {
      if (!"simple".equals(str) && !"extended".equals(str) && !"locator".equals(str) && !"arc".equals(str)) {
         throw new DOMException((short)12, "xlink:type='" + str + "'");
      } else {
         elt.setAttributeNS("http://www.w3.org/1999/xlink", "type", str);
      }
   }

   public static String getXLinkRole(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "role");
   }

   public static void setXLinkRole(Element elt, String str) {
      elt.setAttributeNS("http://www.w3.org/1999/xlink", "role", str);
   }

   public static String getXLinkArcRole(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "arcrole");
   }

   public static void setXLinkArcRole(Element elt, String str) {
      elt.setAttributeNS("http://www.w3.org/1999/xlink", "arcrole", str);
   }

   public static String getXLinkTitle(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "title");
   }

   public static void setXLinkTitle(Element elt, String str) {
      elt.setAttributeNS("http://www.w3.org/1999/xlink", "title", str);
   }

   public static String getXLinkShow(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "show");
   }

   public static void setXLinkShow(Element elt, String str) {
      if (!"new".equals(str) && !"replace".equals(str) && !"embed".equals(str)) {
         throw new DOMException((short)12, "xlink:show='" + str + "'");
      } else {
         elt.setAttributeNS("http://www.w3.org/1999/xlink", "show", str);
      }
   }

   public static String getXLinkActuate(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "actuate");
   }

   public static void setXLinkActuate(Element elt, String str) {
      if (!"onReplace".equals(str) && !"onLoad".equals(str)) {
         throw new DOMException((short)12, "xlink:actuate='" + str + "'");
      } else {
         elt.setAttributeNS("http://www.w3.org/1999/xlink", "actuate", str);
      }
   }

   public static String getXLinkHref(Element elt) {
      return elt.getAttributeNS("http://www.w3.org/1999/xlink", "href");
   }

   public static void setXLinkHref(Element elt, String str) {
      elt.setAttributeNS("http://www.w3.org/1999/xlink", "href", str);
   }
}
