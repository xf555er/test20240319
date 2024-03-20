package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGURIReference;

public abstract class SVGOMURIReferenceElement extends SVGOMElement implements SVGURIReference {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString href;

   protected SVGOMURIReferenceElement() {
   }

   protected SVGOMURIReferenceElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.href = this.createLiveAnimatedString("http://www.w3.org/1999/xlink", "href");
   }

   public SVGAnimatedString getHref() {
      return this.href;
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
      t.put("http://www.w3.org/1999/xlink", "href", new TraitInformation(true, 10));
      xmlTraitInformation = t;
   }
}
