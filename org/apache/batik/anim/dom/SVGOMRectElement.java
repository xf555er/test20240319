package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGRectElement;

public class SVGOMRectElement extends SVGGraphicsElement implements SVGRectElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedLength x;
   protected SVGOMAnimatedLength y;
   protected AbstractSVGAnimatedLength rx;
   protected AbstractSVGAnimatedLength ry;
   protected SVGOMAnimatedLength width;
   protected SVGOMAnimatedLength height;

   protected SVGOMRectElement() {
   }

   public SVGOMRectElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.x = this.createLiveAnimatedLength((String)null, "x", "0", (short)2, false);
      this.y = this.createLiveAnimatedLength((String)null, "y", "0", (short)1, false);
      this.width = this.createLiveAnimatedLength((String)null, "width", (String)null, (short)2, true);
      this.height = this.createLiveAnimatedLength((String)null, "height", (String)null, (short)1, true);
      this.rx = new AbstractSVGAnimatedLength(this, (String)null, "rx", 2, true) {
         protected String getDefaultValue() {
            Attr attr = SVGOMRectElement.this.getAttributeNodeNS((String)null, "ry");
            return attr == null ? "0" : attr.getValue();
         }

         protected void attrChanged() {
            super.attrChanged();
            AbstractSVGAnimatedLength ry = (AbstractSVGAnimatedLength)SVGOMRectElement.this.getRy();
            if (this.isSpecified() && !ry.isSpecified()) {
               ry.attrChanged();
            }

         }
      };
      this.ry = new AbstractSVGAnimatedLength(this, (String)null, "ry", 1, true) {
         protected String getDefaultValue() {
            Attr attr = SVGOMRectElement.this.getAttributeNodeNS((String)null, "rx");
            return attr == null ? "0" : attr.getValue();
         }

         protected void attrChanged() {
            super.attrChanged();
            AbstractSVGAnimatedLength rx = (AbstractSVGAnimatedLength)SVGOMRectElement.this.getRx();
            if (this.isSpecified() && !rx.isSpecified()) {
               rx.attrChanged();
            }

         }
      };
      this.liveAttributeValues.put((Object)null, "rx", this.rx);
      this.liveAttributeValues.put((Object)null, "ry", this.ry);
      AnimatedAttributeListener l = ((SVGOMDocument)this.ownerDocument).getAnimatedAttributeListener();
      this.rx.addAnimatedAttributeListener(l);
      this.ry.addAnimatedAttributeListener(l);
   }

   public String getLocalName() {
      return "rect";
   }

   public SVGAnimatedLength getX() {
      return this.x;
   }

   public SVGAnimatedLength getY() {
      return this.y;
   }

   public SVGAnimatedLength getWidth() {
      return this.width;
   }

   public SVGAnimatedLength getHeight() {
      return this.height;
   }

   public SVGAnimatedLength getRx() {
      return this.rx;
   }

   public SVGAnimatedLength getRy() {
      return this.ry;
   }

   protected Node newNode() {
      return new SVGOMRectElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   public void updateAttributeValue(String ns, String ln, AnimatableValue val) {
      if (ns == null) {
         AbstractSVGAnimatedLength rx;
         if (ln.equals("rx")) {
            super.updateAttributeValue(ns, ln, val);
            rx = (AbstractSVGAnimatedLength)this.getRy();
            if (!rx.isSpecified()) {
               super.updateAttributeValue(ns, "ry", val);
            }

            return;
         }

         if (ln.equals("ry")) {
            super.updateAttributeValue(ns, ln, val);
            rx = (AbstractSVGAnimatedLength)this.getRx();
            if (!rx.isSpecified()) {
               super.updateAttributeValue(ns, "rx", val);
            }

            return;
         }
      }

      super.updateAttributeValue(ns, ln, val);
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGGraphicsElement.xmlTraitInformation);
      t.put((Object)null, "x", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "y", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "rx", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "ry", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "width", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "height", new TraitInformation(true, 3, (short)2));
      xmlTraitInformation = t;
   }
}
