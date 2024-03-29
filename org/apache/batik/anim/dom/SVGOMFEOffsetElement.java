package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEOffsetElement;

public class SVGOMFEOffsetElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEOffsetElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString in;
   protected SVGOMAnimatedNumber dx;
   protected SVGOMAnimatedNumber dy;

   protected SVGOMFEOffsetElement() {
   }

   public SVGOMFEOffsetElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.in = this.createLiveAnimatedString((String)null, "in");
      this.dx = this.createLiveAnimatedNumber((String)null, "dx", 0.0F);
      this.dy = this.createLiveAnimatedNumber((String)null, "dy", 0.0F);
   }

   public String getLocalName() {
      return "feOffset";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedNumber getDx() {
      return this.dx;
   }

   public SVGAnimatedNumber getDy() {
      return this.dy;
   }

   protected Node newNode() {
      return new SVGOMFEOffsetElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
      t.put((Object)null, "in", new TraitInformation(true, 16));
      t.put((Object)null, "dx", new TraitInformation(true, 2));
      t.put((Object)null, "dy", new TraitInformation(true, 2));
      xmlTraitInformation = t;
   }
}
