package org.apache.batik.anim.dom;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGTestsSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGCursorElement;
import org.w3c.dom.svg.SVGStringList;

public class SVGOMCursorElement extends SVGOMURIReferenceElement implements SVGCursorElement {
   protected static final AttributeInitializer attributeInitializer = new AttributeInitializer(4);
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedLength x;
   protected SVGOMAnimatedLength y;
   protected SVGOMAnimatedBoolean externalResourcesRequired;

   protected SVGOMCursorElement() {
   }

   public SVGOMCursorElement(String prefix, AbstractDocument owner) {
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
      this.externalResourcesRequired = this.createLiveAnimatedBoolean((String)null, "externalResourcesRequired", false);
   }

   public String getLocalName() {
      return "cursor";
   }

   public SVGAnimatedLength getX() {
      return this.x;
   }

   public SVGAnimatedLength getY() {
      return this.y;
   }

   public SVGAnimatedBoolean getExternalResourcesRequired() {
      return this.externalResourcesRequired;
   }

   public SVGStringList getRequiredFeatures() {
      return SVGTestsSupport.getRequiredFeatures(this);
   }

   public SVGStringList getRequiredExtensions() {
      return SVGTestsSupport.getRequiredExtensions(this);
   }

   public SVGStringList getSystemLanguage() {
      return SVGTestsSupport.getSystemLanguage(this);
   }

   public boolean hasExtension(String extension) {
      return SVGTestsSupport.hasExtension(this, extension);
   }

   protected AttributeInitializer getAttributeInitializer() {
      return attributeInitializer;
   }

   protected Node newNode() {
      return new SVGOMCursorElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
   }

   static {
      attributeInitializer.addAttribute("http://www.w3.org/2000/xmlns/", (String)null, "xmlns:xlink", "http://www.w3.org/1999/xlink");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "type", "simple");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "show", "other");
      attributeInitializer.addAttribute("http://www.w3.org/1999/xlink", "xlink", "actuate", "onLoad");
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMURIReferenceElement.xmlTraitInformation);
      t.put((Object)null, "externalResourcesRequired", new TraitInformation(true, 49));
      t.put((Object)null, "x", new TraitInformation(true, 3, (short)1));
      t.put((Object)null, "y", new TraitInformation(true, 3, (short)2));
      xmlTraitInformation = t;
   }
}
