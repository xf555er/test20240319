package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGHKernElement;

public class SVGOMHKernElement extends SVGOMElement implements SVGHKernElement {
   protected SVGOMHKernElement() {
   }

   public SVGOMHKernElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "hkern";
   }

   protected Node newNode() {
      return new SVGOMHKernElement();
   }
}
