package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMSubImageRefElement extends SVGStylableElement {
   protected SVGOMSubImageRefElement() {
   }

   public SVGOMSubImageRefElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "subImageRef";
   }

   protected Node newNode() {
      return new SVGOMSubImageRefElement();
   }
}
