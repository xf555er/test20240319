package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEMergeNodeElement;

public class SVGOMFEMergeNodeElement extends SVGOMElement implements SVGFEMergeNodeElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString in;

   protected SVGOMFEMergeNodeElement() {
   }

   public SVGOMFEMergeNodeElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.in = this.createLiveAnimatedString((String)null, "in");
   }

   public String getLocalName() {
      return "feMergeNode";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   protected Node newNode() {
      return new SVGOMFEMergeNodeElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      xmlTraitInformation = t;
   }
}
