package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEGaussianBlurElement;

public class SVGOMFEGaussianBlurElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEGaussianBlurElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString in;

   protected SVGOMFEGaussianBlurElement() {
   }

   public SVGOMFEGaussianBlurElement(String prefix, AbstractDocument owner) {
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
      return "feGaussianBlur";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedNumber getStdDeviationX() {
      throw new UnsupportedOperationException("SVGFEGaussianBlurElement.getStdDeviationX is not implemented");
   }

   public SVGAnimatedNumber getStdDeviationY() {
      throw new UnsupportedOperationException("SVGFEGaussianBlurElement.getStdDeviationY is not implemented");
   }

   public void setStdDeviation(float devX, float devY) {
      this.setAttributeNS((String)null, "stdDeviation", Float.toString(devX) + " " + Float.toString(devY));
   }

   protected Node newNode() {
      return new SVGOMFEGaussianBlurElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      t.put((Object)null, "stdDeviation", new TraitInformation(true, 4));
      xmlTraitInformation = t;
   }
}
