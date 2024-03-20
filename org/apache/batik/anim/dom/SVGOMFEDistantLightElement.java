package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGFEDistantLightElement;

public class SVGOMFEDistantLightElement extends SVGOMElement implements SVGFEDistantLightElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedNumber azimuth;
   protected SVGOMAnimatedNumber elevation;

   protected SVGOMFEDistantLightElement() {
   }

   public SVGOMFEDistantLightElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.azimuth = this.createLiveAnimatedNumber((String)null, "azimuth", 0.0F);
      this.elevation = this.createLiveAnimatedNumber((String)null, "elevation", 0.0F);
   }

   public String getLocalName() {
      return "feDistantLight";
   }

   public SVGAnimatedNumber getAzimuth() {
      return this.azimuth;
   }

   public SVGAnimatedNumber getElevation() {
      return this.elevation;
   }

   protected Node newNode() {
      return new SVGOMFEDistantLightElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
      t.put((Object)null, "azimuth", new TraitInformation(true, 2));
      t.put((Object)null, "elevation", new TraitInformation(true, 2));
      xmlTraitInformation = t;
   }
}
