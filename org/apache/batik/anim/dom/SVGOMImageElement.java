package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGImageElement;

public class SVGOMImageElement extends SVGURIReferenceGraphicsElement implements SVGImageElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected static final AttributeInitializer attributeInitializer;
   protected SVGOMAnimatedLength x;
   protected SVGOMAnimatedLength y;
   protected SVGOMAnimatedLength width;
   protected SVGOMAnimatedLength height;
   protected SVGOMAnimatedPreserveAspectRatio preserveAspectRatio;

   protected SVGOMImageElement() {
   }

   public SVGOMImageElement(String prefix, AbstractDocument owner) {
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
      this.preserveAspectRatio = this.createLiveAnimatedPreserveAspectRatio();
   }

   public String getLocalName() {
      return "image";
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

   public SVGAnimatedPreserveAspectRatio getPreserveAspectRatio() {
      return this.preserveAspectRatio;
   }

   protected AttributeInitializer getAttributeInitializer() {
      return attributeInitializer;
   }

   protected Node newNode() {
      return new SVGOMImageElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGURIReferenceGraphicsElement.xmlTraitInformation);
      t.put((Object)null, "x", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "y", new TraitInformation(true, 3, (short)2));
      t.put((Object)null, "width", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "height", new TraitInformation(true, 3, (short)2));
      xmlTraitInformation = t;
      attributeInitializer = new AttributeInitializer(5);
      attributeInitializer.addAttribute((String)null, (String)null, "preserveAspectRatio", "xMidYMid meet");
      attributeInitializer.addAttribute("http://www.w3.org/2000/xmlns/", (String)null, "xmlns:xlink", "http://www.w3.org/1999/xlink");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "type", "simple");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "show", "embed");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "actuate", "onLoad");
   }
}
