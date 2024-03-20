package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedEnumeration;
import org.w3c.dom.svg.SVGClipPathElement;

public class SVGOMClipPathElement extends SVGGraphicsElement implements SVGClipPathElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final String[] CLIP_PATH_UNITS_VALUES;
   protected SVGOMAnimatedEnumeration clipPathUnits;

   protected SVGOMClipPathElement() {
   }

   public SVGOMClipPathElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.clipPathUnits = this.createLiveAnimatedEnumeration((String)null, "clipPathUnits", CLIP_PATH_UNITS_VALUES, (short)1);
   }

   public String getLocalName() {
      return "clipPath";
   }

   public SVGAnimatedEnumeration getClipPathUnits() {
      return this.clipPathUnits;
   }

   protected Node newNode() {
      return new SVGOMClipPathElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
      t.put((Object)null, "clipPathUnits", new TraitInformation(true, 15));
      xmlTraitInformation = t;
      CLIP_PATH_UNITS_VALUES = new String[]{"", "userSpaceOnUse", "objectBoundingBox"};
   }
}
