package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEDiffuseLightingElement;

public class SVGOMFEDiffuseLightingElement extends SVGOMFilterPrimitiveStandardAttributes implements SVGFEDiffuseLightingElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString in;
   protected SVGOMAnimatedNumber surfaceScale;
   protected SVGOMAnimatedNumber diffuseConstant;

   protected SVGOMFEDiffuseLightingElement() {
   }

   public SVGOMFEDiffuseLightingElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.in = this.createLiveAnimatedString((String)null, "in");
      this.surfaceScale = this.createLiveAnimatedNumber((String)null, "surfaceScale", 1.0F);
      this.diffuseConstant = this.createLiveAnimatedNumber((String)null, "diffuseConstant", 1.0F);
   }

   public String getLocalName() {
      return "feDiffuseLighting";
   }

   public SVGAnimatedString getIn1() {
      return this.in;
   }

   public SVGAnimatedNumber getSurfaceScale() {
      return this.surfaceScale;
   }

   public SVGAnimatedNumber getDiffuseConstant() {
      return this.diffuseConstant;
   }

   public SVGAnimatedNumber getKernelUnitLengthX() {
      throw new UnsupportedOperationException("SVGFEDiffuseLightingElement.getKernelUnitLengthX is not implemented");
   }

   public SVGAnimatedNumber getKernelUnitLengthY() {
      throw new UnsupportedOperationException("SVGFEDiffuseLightingElement.getKernelUnitLengthY is not implemented");
   }

   protected Node newNode() {
      return new SVGOMFEDiffuseLightingElement();
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
   }
}
