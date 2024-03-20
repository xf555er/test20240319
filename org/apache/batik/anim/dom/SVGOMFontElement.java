package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGFontElement;

public class SVGOMFontElement extends SVGStylableElement implements SVGFontElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedBoolean externalResourcesRequired;

   protected SVGOMFontElement() {
   }

   public SVGOMFontElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.externalResourcesRequired = this.createLiveAnimatedBoolean((String)null, "externalResourcesRequired", false);
   }

   public String getLocalName() {
      return "font";
   }

   public SVGAnimatedBoolean getExternalResourcesRequired() {
      return this.externalResourcesRequired;
   }

   protected Node newNode() {
      return new SVGOMFontElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
      t.put((Object)null, "externalResourcesRequired", new TraitInformation(true, 49));
      xmlTraitInformation = t;
   }
}
