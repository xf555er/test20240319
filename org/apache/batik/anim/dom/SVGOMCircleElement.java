package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGCircleElement;

public class SVGOMCircleElement extends SVGGraphicsElement implements SVGCircleElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedLength cx;
   protected SVGOMAnimatedLength cy;
   protected SVGOMAnimatedLength r;

   protected SVGOMCircleElement() {
   }

   public SVGOMCircleElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.cx = this.createLiveAnimatedLength((String)null, "cx", "0", (short)2, false);
      this.cy = this.createLiveAnimatedLength((String)null, "cy", "0", (short)1, false);
      this.r = this.createLiveAnimatedLength((String)null, "r", (String)null, (short)0, true);
   }

   public String getLocalName() {
      return "circle";
   }

   public SVGAnimatedLength getCx() {
      return this.cx;
   }

   public SVGAnimatedLength getCy() {
      return this.cy;
   }

   public SVGAnimatedLength getR() {
      return this.r;
   }

   protected Node newNode() {
      return new SVGOMCircleElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
      t.put((Object)null, "cx", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "cy", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "r", new TraitInformation(true, 3, (short)3));
      xmlTraitInformation = t;
   }
}
