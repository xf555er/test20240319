package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEMorphologyElement;

public class SVGOMFEMorphologyElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEMorphologyElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final String[] OPERATOR_VALUES;
   protected SVGOMAnimatedString in;
   protected SVGOMAnimatedEnumeration operator;

   protected SVGOMFEMorphologyElement() {
   }

   public SVGOMFEMorphologyElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.in = this.createLiveAnimatedString((String)null, "in");
      this.operator = this.createLiveAnimatedEnumeration((String)null, "operator", OPERATOR_VALUES, (short)1);
   }

   public String getLocalName() {
      return "feMorphology";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedEnumeration getOperator() {
      return this.operator;
   }

   public SVGAnimatedNumber getRadiusX() {
      throw new UnsupportedOperationException("SVGFEMorphologyElement.getRadiusX is not implemented");
   }

   public SVGAnimatedNumber getRadiusY() {
      throw new UnsupportedOperationException("SVGFEMorphologyElement.getRadiusY is not implemented");
   }

   protected Node newNode() {
      return new SVGOMFEMorphologyElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      t.put((Object)null, "operator", new TraitInformation(true, 15));
      t.put((Object)null, "radius", new TraitInformation(true, 4));
      xmlTraitInformation = t;
      OPERATOR_VALUES = new String[]{"", "erode", "dilate"};
   }
}
