package org.apache.batik.extension.svg;

import org.apache.batik.anim.dom.SVGOMTextPositioningElement;
import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class FlowParaElement extends SVGOMTextPositioningElement implements BatikExtConstants {
   protected FlowParaElement() {
   }

   public FlowParaElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowPara";
   }

   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   protected Node newNode() {
      return new FlowParaElement();
   }
}
