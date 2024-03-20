package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAElement;
import org.w3c.dom.svg.SVGAnimatedString;

public class SVGOMAElement extends SVGURIReferenceGraphicsElement implements SVGAElement {
   protected static final AttributeInitializer attributeInitializer = new AttributeInitializer(4);
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedString target;

   protected SVGOMAElement() {
   }

   public SVGOMAElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.target = this.createLiveAnimatedString((String)null, "target");
   }

   public String getLocalName() {
      return "a";
   }

   public SVGAnimatedString getTarget() {
      return this.target;
   }

   protected AttributeInitializer getAttributeInitializer() {
      return attributeInitializer;
   }

   protected Node newNode() {
      return new SVGOMAElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      attributeInitializer.addAttribute("http://www.w3.org/2000/xmlns/", (String)null, "xmlns:xlink", "http://www.w3.org/1999/xlink");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "type", "simple");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "show", "replace");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "actuate", "onRequest");
      DoublyIndexedTable t = new DoublyIndexedTable(SVGURIReferenceGraphicsElement.xmlTraitInformation);
      t.put((Object)null, "target", new TraitInformation(true, 16));
      xmlTraitInformation = t;
   }
}
