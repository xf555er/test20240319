package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMFlowLineElement extends SVGOMTextPositioningElement {
   protected SVGOMFlowLineElement() {
   }

   public SVGOMFlowLineElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowLine";
   }

   protected Node newNode() {
      return new SVGOMFlowLineElement();
   }
}
