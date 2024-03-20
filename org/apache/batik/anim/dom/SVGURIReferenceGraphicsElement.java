package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.svg.SVGAnimatedString;

public abstract class SVGURIReferenceGraphicsElement extends SVGGraphicsElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString href;

   protected SVGURIReferenceGraphicsElement() {
   }

   protected SVGURIReferenceGraphicsElement(String prefix, AbstractDocument owner) {
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
      DoublyIndexedTable t = new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
      t.put("http://www.w3.org/1999/xlink", "href", new TraitInformation(true, 10));
      xmlTraitInformation = t;
   }
}
