package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumberList;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEColorMatrixElement;

public class SVGOMFEColorMatrixElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEColorMatrixElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final String[] TYPE_VALUES;
   protected SVGOMAnimatedString in;
   protected SVGOMAnimatedEnumeration type;

   protected SVGOMFEColorMatrixElement() {
   }

   public SVGOMFEColorMatrixElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.in = this.createLiveAnimatedString((String)null, "in");
      this.type = this.createLiveAnimatedEnumeration((String)null, "type", TYPE_VALUES, (short)1);
   }

   public String getLocalName() {
      return "feColorMatrix";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedEnumeration getType() {
      return this.type;
   }

   public SVGAnimatedNumberList getValues() {
      throw new UnsupportedOperationException("SVGFEColorMatrixElement.getValues is not implemented");
   }

   protected Node newNode() {
      return new SVGOMFEColorMatrixElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      t.put((Object)null, "type", new TraitInformation(true, 15));
      t.put((Object)null, "values", new TraitInformation(true, 13));
      xmlTraitInformation = t;
      TYPE_VALUES = new String[]{"", "matrix", "saturate", "hueRotate", "luminanceToAlpha"};
   }
}
