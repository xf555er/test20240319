package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGRadialGradientElement;

public class SVGOMRadialGradientElement extends SVGOMGradientElement implements SVGRadialGradientElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedLength cx;
   protected SVGOMAnimatedLength cy;
   protected AbstractSVGAnimatedLength fx;
   protected AbstractSVGAnimatedLength fy;
   protected SVGOMAnimatedLength r;

   protected SVGOMRadialGradientElement() {
   }

   public SVGOMRadialGradientElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.cx = this.createLiveAnimatedLength((String)null, "cx", "50%", (short)2, false);
      this.cy = this.createLiveAnimatedLength((String)null, "cy", "50%", (short)1, false);
      this.r = this.createLiveAnimatedLength((String)null, "r", "50%", (short)0, false);
      this.fx = new AbstractSVGAnimatedLength(this, (String)null, "fx", 2, false) {
         protected String getDefaultValue() {
            Attr attr = SVGOMRadialGradientElement.this.getAttributeNodeNS((String)null, "cx");
            return attr == null ? "50%" : attr.getValue();
         }
      };
      this.fy = new AbstractSVGAnimatedLength(this, (String)null, "fy", 1, false) {
         protected String getDefaultValue() {
            Attr attr = SVGOMRadialGradientElement.this.getAttributeNodeNS((String)null, "cy");
            return attr == null ? "50%" : attr.getValue();
         }
      };
      this.liveAttributeValues.put((Object)null, "fx", this.fx);
      this.liveAttributeValues.put((Object)null, "fy", this.fy);
      AnimatedAttributeListener l = ((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener();
      this.fx.addAnimatedAttributeListener(l);
      this.fy.addAnimatedAttributeListener(l);
   }

   public String getLocalName() {
      return "radialGradient";
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

   public SVGAnimatedLength getFx() {
      return this.fx;
   }

   public SVGAnimatedLength getFy() {
      return this.fy;
   }

   protected Node newNode() {
      return new SVGOMRadialGradientElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMGradientElement.xmlTraitInformation);
      t.put((Object)null, "cx", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "cy", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "fx", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "fy", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "r", new TraitInformation(true, 3, (short)3));
      xmlTraitInformation = t;
   }
}
