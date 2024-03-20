package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGVKernElement;

public class SVGOMVKernElement extends SVGOMElement implements SVGVKernElement {
   protected SVGOMVKernElement() {
   }

   public SVGOMVKernElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "vkern";
   }

   protected Node newNode() {
      return new SVGOMVKernElement();
   }
}
