package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMFlowRootElement extends SVGGraphicsElement {
   protected SVGOMFlowRootElement() {
   }

   public SVGOMFlowRootElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowRoot";
   }

   protected Node newNode() {
      return new SVGOMFlowRootElement();
   }
}
