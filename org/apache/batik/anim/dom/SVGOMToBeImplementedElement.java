package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.w3c.dom.Node;

public class SVGOMToBeImplementedElement extends SVGGraphicsElement {
   protected String localName;

   protected SVGOMToBeImplementedElement() {
   }

   public SVGOMToBeImplementedElement(String prefix, AbstractDocument owner, String localName) {
      super(prefix, owner);
      this.localName = localName;
   }

   public String getLocalName() {
      return this.localName;
   }

   protected Node newNode() {
      return new SVGOMToBeImplementedElement();
   }

   protected Node export(Node n, AbstractDocument d) {
      super.export(n, d);
      SVGOMToBeImplementedElement ae = (SVGOMToBeImplementedElement)n;
      ae.localName = this.localName;
      return n;
   }

   protected Node deepExport(Node n, AbstractDocument d) {
      super.deepExport(n, d);
      SVGOMToBeImplementedElement ae = (SVGOMToBeImplementedElement)n;
      ae.localName = this.localName;
      return n;
   }

   protected Node copyInto(Node n) {
      super.copyInto(n);
      SVGOMToBeImplementedElement ae = (SVGOMToBeImplementedElement)n;
      ae.localName = this.localName;
      return n;
   }

   protected Node deepCopyInto(Node n) {
      super.deepCopyInto(n);
      SVGOMToBeImplementedElement ae = (SVGOMToBeImplementedElement)n;
      ae.localName = this.localName;
      return n;
   }
}
