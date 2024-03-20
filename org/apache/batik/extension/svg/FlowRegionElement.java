package org.apache.batik.extension.svg;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.extension.PrefixableStylableExtensionElement;
import org.w3c.dom.Node;

public class FlowRegionElement extends PrefixableStylableExtensionElement implements BatikExtConstants {
   protected FlowRegionElement() {
   }

   public FlowRegionElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "flowRegion";
   }

   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   protected Node newNode() {
      return new FlowRegionElement();
   }
}
