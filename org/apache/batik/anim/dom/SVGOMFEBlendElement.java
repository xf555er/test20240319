package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEBlendElement;

public class SVGOMFEBlendElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEBlendElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final String[] MODE_VALUES;
   protected SVGOMAnimatedString in;
   protected SVGOMAnimatedString in2;
   protected SVGOMAnimatedEnumeration mode;

   protected SVGOMFEBlendElement() {
   }

   public SVGOMFEBlendElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.in = this.createLiveAnimatedString((String)null, "in");
      this.in2 = this.createLiveAnimatedString((String)null, "in2");
      this.mode = this.createLiveAnimatedEnumeration((String)null, "mode", MODE_VALUES, (short)1);
   }

   public String getLocalName() {
      return "feBlend";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedString getIn2() {
      return this.in2;
   }

   public SVGAnimatedEnumeration getMode() {
      return this.mode;
   }

   protected Node newNode() {
      return new SVGOMFEBlendElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      t.put((Object)null, "surfaceScale", new TraitInformation(true, 2));
      t.put((Object)null, "diffuseConstant", new TraitInformation(true, 2));
      t.put((Object)null, "kernelUnitLength", new TraitInformation(true, 4));
      xmlTraitInformation = t;
      MODE_VALUES = new String[]{"", "normal", "multiply", "screen", "darken", "lighten"};
   }
}
