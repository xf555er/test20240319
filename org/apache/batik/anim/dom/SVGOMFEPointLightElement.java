package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGFEPointLightElement;

public class SVGOMFEPointLightElement extends SVGOMElement implements SVGFEPointLightElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedNumber x;
   protected SVGOMAnimatedNumber y;
   protected SVGOMAnimatedNumber z;

   protected SVGOMFEPointLightElement() {
   }

   public SVGOMFEPointLightElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.x = this.createLiveAnimatedNumber((String)null, "x", 0.0F);
      this.y = this.createLiveAnimatedNumber((String)null, "y", 0.0F);
      this.z = this.createLiveAnimatedNumber((String)null, "z", 0.0F);
   }

   public String getLocalName() {
      return "fePointLight";
   }

   public SVGAnimatedNumber getX() {
      return this.x;
   }

   public SVGAnimatedNumber getY() {
      return this.y;
   }

   public SVGAnimatedNumber getZ() {
      return this.z;
   }

   protected Node newNode() {
      return new SVGOMFEPointLightElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
      t.put((Object)null, "x", new TraitInformation(true, 2));
      t.put((Object)null, "y", new TraitInformation(true, 2));
      t.put((Object)null, "z", new TraitInformation(true, 2));
      xmlTraitInformation = t;
   }
}
