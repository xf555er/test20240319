package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEDisplacementMapElement;

public class SVGOMFEDisplacementMapElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEDisplacementMapElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final String[] CHANNEL_SELECTOR_VALUES;
   protected SVGOMAnimatedString in;
   protected SVGOMAnimatedString in2;
   protected SVGOMAnimatedNumber scale;
   protected SVGOMAnimatedEnumeration xChannelSelector;
   protected SVGOMAnimatedEnumeration yChannelSelector;

   protected SVGOMFEDisplacementMapElement() {
   }

   public SVGOMFEDisplacementMapElement(String prefix, AbstractDocument owner) {
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
      this.scale = this.createLiveAnimatedNumber((String)null, "scale", 0.0F);
      this.xChannelSelector = this.createLiveAnimatedEnumeration((String)null, "xChannelSelector", CHANNEL_SELECTOR_VALUES, (short)4);
      this.yChannelSelector = this.createLiveAnimatedEnumeration((String)null, "yChannelSelector", CHANNEL_SELECTOR_VALUES, (short)4);
   }

   public String getLocalName() {
      return "feDisplacementMap";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedString getIn2() {
      return this.in2;
   }

   public SVGAnimatedNumber getScale() {
      return this.scale;
   }

   public SVGAnimatedEnumeration getXChannelSelector() {
      return this.xChannelSelector;
   }

   public SVGAnimatedEnumeration getYChannelSelector() {
      return this.yChannelSelector;
   }

   protected Node newNode() {
      return new SVGOMFEDisplacementMapElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      t.put((Object)null, "in2", new TraitInformation(true, 16));
      t.put((Object)null, "scale", new TraitInformation(true, 2));
      t.put((Object)null, "xChannelSelector", new TraitInformation(true, 15));
      t.put((Object)null, "yChannelSelector", new TraitInformation(true, 15));
      xmlTraitInformation = t;
      CHANNEL_SELECTOR_VALUES = new String[]{"", "R", "G", "B", "A"};
   }
}
