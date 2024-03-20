package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMFlowRegionElement extends SVGStylableElement {
   protected SVGOMFlowRegionElement() {
   }

   public SVGOMFlowRegionElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowRegion";
   }

   protected Node newNode() {
      return new SVGOMFlowRegionElement();
   }
}
