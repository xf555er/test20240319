package org.apache.batik.extension.svg;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.extension.GraphicsExtensionElement;
import org.w3c.dom.Node;

public class BatikStarElement extends GraphicsExtensionElement implements BatikExtConstants {
   protected BatikStarElement() {
   }

   public BatikStarElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "star";
   }

   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   protected Node newNode() {
      return new BatikStarElement();
   }
}
