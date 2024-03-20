package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMFlowRegionExcludeElement extends SVGStylableElement {
   protected SVGOMFlowRegionExcludeElement() {
   }

   public SVGOMFlowRegionExcludeElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowRegionExclude";
   }

   protected Node newNode() {
      return new SVGOMFlowRegionExcludeElement();
   }
}
