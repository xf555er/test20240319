package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMHandlerElement extends SVGOMElement {
   protected SVGOMHandlerElement() {
   }

   public SVGOMHandlerElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "handler";
   }

   protected Node newNode() {
      return new SVGOMHandlerElement();
   }
}
