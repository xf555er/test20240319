package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.svg.SVGAnimatedPoints;
import org.w3c.dom.svg.SVGPointList;

public abstract class SVGPointShapeElement extends SVGGraphicsElement implements SVGAnimatedPoints {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedPoints points;

   protected SVGPointShapeElement() {
   }

   public SVGPointShapeElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.points = this.createLiveAnimatedPoints((String)null, "points", "");
   }

   public SVGOMAnimatedPoints getSVGOMAnimatedPoints() {
      return this.points;
   }

   public SVGPointList getPoints() {
      return this.points.getPoints();
   }

   public SVGPointList getAnimatedPoints() {
      return this.points.getAnimatedPoints();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
      t.put((Object)null, "points", new TraitInformation(true, 31));
      xmlTraitInformation = t;
   }
}
