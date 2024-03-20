package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMSolidColorElement extends SVGStylableElement {
   protected SVGOMSolidColorElement() {
   }

   public SVGOMSolidColorElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "solidColor";
   }

   protected Node newNode() {
      return new SVGOMSolidColorElement();
   }
}
