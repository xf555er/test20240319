package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGColorProfileElement;

public class SVGOMColorProfileElement extends SVGOMURIReferenceElement implements SVGColorProfileElement {
   protected static final AttributeInitializer attributeInitializer = new AttributeInitializer(5);

   protected SVGOMColorProfileElement() {
   }

   public SVGOMColorProfileElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "color-profile";
   }

   public String getLocal() {
      return this.getAttributeNS((String)null, "local");
   }

   public void setLocal(String local) throws DOMException {
      this.setAttributeNS((String)null, "local", local);
   }

   public String getName() {
      return this.getAttributeNS((String)null, "name");
   }

   public void setName(String name) throws DOMException {
      this.setAttributeNS((String)null, "name", name);
   }

   public short getRenderingIntent() {
      Attr attr = this.getAttributeNodeNS((String)null, "rendering-intent");
      if (attr == null) {
         return 1;
      } else {
         String val = attr.getValue();
         switch (val.length()) {
            case 4:
               if (val.equals("auto")) {
                  return 1;
               }
               break;
            case 10:
               if (val.equals("perceptual")) {
                  return 2;
               }

               if (val.equals("saturate")) {
                  return 4;
               }
               break;
            case 21:
               if (val.equals("absolute-colorimetric")) {
                  return 5;
               }

               if (val.equals("relative-colorimetric")) {
                  return 3;
               }
         }

         return 0;
      }
   }

   public void setRenderingIntent(short renderingIntent) throws DOMException {
      switch (renderingIntent) {
         case 1:
            this.setAttributeNS((String)null, "rendering-intent", "auto");
            break;
         case 2:
            this.setAttributeNS((String)null, "rendering-intent", "perceptual");
            break;
         case 3:
            this.setAttributeNS((String)null, "rendering-intent", "relative-colorimetric");
            break;
         case 4:
            this.setAttributeNS((String)null, "rendering-intent", "saturate");
            break;
         case 5:
            this.setAttributeNS((String)null, "rendering-intent", "absolute-colorimetric");
      }

   }

   protected AttributeInitializer getAttributeInitializer() {
      return attributeInitializer;
   }

   protected Node newNode() {
      return new SVGOMColorProfileElement();
   }

   static {
      attributeInitializer.addAttribute((String)null, (String)null, "rendering-intent", "auto");
      attributeInitializer.addAttribute("http://www.w3.org/2000/xmlns/", (String)null, "xmlns:xlink", "http://www.w3.org/1999/xlink");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "type", "simple");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "show", "other");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "actuate", "onLoad");
   }
}
