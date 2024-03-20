package org.apache.batik.dom.svg;

import org.apache.batik.dom.AbstractNode;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

public class SVGZoomAndPanSupport implements SVGConstants {
   protected SVGZoomAndPanSupport() {
   }

   public static void setZoomAndPan(Element elt, short val) throws DOMException {
      switch (val) {
         case 1:
            elt.setAttributeNS((String)null, "zoomAndPan", "disable");
            break;
         case 2:
            elt.setAttributeNS((String)null, "zoomAndPan", "magnify");
            break;
         default:
            throw ((AbstractNode)elt).createDOMException((short)13, "zoom.and.pan", new Object[]{Integer.valueOf(val)});
      }

   }

   public static short getZoomAndPan(Element elt) {
      String s = elt.getAttributeNS((String)null, "zoomAndPan");
      return (short)(s.equals("magnify") ? 2 : 1);
   }
}
