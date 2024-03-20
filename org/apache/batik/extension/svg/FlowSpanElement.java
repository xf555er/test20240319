package org.apache.batik.extension.svg;

import org.apache.batik.anim.dom.SVGOMTextPositioningElement;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class FlowSpanElement extends SVGOMTextPositioningElement implements BatikExtConstants {
   protected FlowSpanElement() {
   }

   public FlowSpanElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowSpan";
   }

   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   protected Node newNode() {
      return new FlowSpanElement();
   }
}
