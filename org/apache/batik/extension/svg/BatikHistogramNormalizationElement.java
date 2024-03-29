package org.apache.batik.extension.svg;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.extension.PrefixableStylableExtensionElement;
import org.w3c.dom.Node;

public class BatikHistogramNormalizationElement extends PrefixableStylableExtensionElement implements BatikExtConstants {
   protected BatikHistogramNormalizationElement() {
   }

   public BatikHistogramNormalizationElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
   }

   public String getLocalName() {
      return "histogramNormalization";
   }

   public String getNamespaceURI() {
      return "http://xml.apache.org/batik/ext";
   }

   protected Node newNode() {
      return new BatikHistogramNormalizationElement();
   }
}
