package org.apache.batik.extension;

import org.apache.batik.anim.dom.SVGLocatableSupport;
import org.apache.batik.anim.dom.SVGOMAnimatedBoolean;
import org.apache.batik.anim.dom.SVGOMAnimatedTransformList;
import org.apache.batik.anim.dom.TraitInformation;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGTestsSupport;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.svg.SVGAnimatedBoolean;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGStringList;
import org.w3c.dom.svg.SVGTransformable;

public abstract class GraphicsExtensionElement extends StylableExtensionElement implements SVGTransformable {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedTransformList transform = this.createLiveAnimatedTransformList((String)null, "transform", "");
   protected SVGOMAnimatedBoolean externalResourcesRequired = this.createLiveAnimatedBoolean((String)null, "externalResourcesRequired", false);

   protected GraphicsExtensionElement() {
   }

   protected GraphicsExtensionElement(String name, AbstractDocument owner) {
      super(name, owner);
   }

   public SVGElement getNearestViewportElement() {
      return SVGLocatableSupport.getNearestViewportElement(this);
   }

   public SVGElement getFarthestViewportElement() {
      return SVGLocatableSupport.getFarthestViewportElement(this);
   }

   public SVGRect getBBox() {
      return SVGLocatableSupport.getBBox(this);
   }

   public SVGMatrix getCTM() {
      return SVGLocatableSupport.getCTM(this);
   }

   public SVGMatrix getScreenCTM() {
      return SVGLocatableSupport.getScreenCTM(this);
   }

   public SVGMatrix getTransformToElement(SVGElement element) throws SVGException {
      return SVGLocatableSupport.getTransformToElement(this, element);
   }

   public SVGAnimatedTransformList getTransform() {
      return this.transform;
   }

   public SVGAnimatedBoolean getExternalResourcesRequired() {
      return this.externalResourcesRequired;
   }

   public String getXMLlang() {
      return XMLSupport.getXMLLang(this);
   }

   public void setXMLlang(String lang) {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:lang", lang);
   }

   public String getXMLspace() {
      return XMLSupport.getXMLSpace(this);
   }

   public void setXMLspace(String space) {
      this.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:space", space);
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

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(StylableExtensionElement.xmlTraitInformation);
      t.put((Object)null, "transform", new TraitInformation(true, 9));
      t.put((Object)null, "externalResourcesRequired", new TraitInformation(true, 49));
      t.put((Object)null, "requiredExtensions", new TraitInformation(false, 33));
      t.put((Object)null, "requiredFeatures", new TraitInformation(false, 33));
      t.put((Object)null, "systemLanguage", new TraitInformation(false, 46));
      xmlTraitInformation = t;
   }
}
