package org.apache.batik.anim.dom;

import java.awt.geom.AffineTransform;
import org.apache.batik.anim.values.AnimatableMotionPointValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGMotionAnimatableElement;
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

public abstract class SVGGraphicsElement extends SVGStylableElement implements SVGMotionAnimatableElement {
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedTransformList transform;
   protected SVGOMAnimatedBoolean externalResourcesRequired;
   protected AffineTransform motionTransform;

   protected SVGGraphicsElement() {
   }

   protected SVGGraphicsElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.transform = this.createLiveAnimatedTransformList((String)null, "transform", "");
      this.externalResourcesRequired = this.createLiveAnimatedBoolean((String)null, "externalResourcesRequired", false);
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
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

   public AffineTransform getMotionTransform() {
      return this.motionTransform;
   }

   public void updateOtherValue(String type, AnimatableValue val) {
      if (type.equals("motion")) {
         if (this.motionTransform == null) {
            this.motionTransform = new AffineTransform();
         }

         if (val == null) {
            this.motionTransform.setToIdentity();
         } else {
            AnimatableMotionPointValue p = (AnimatableMotionPointValue)val;
            this.motionTransform.setToTranslation((double)p.getX(), (double)p.getY());
            this.motionTransform.rotate((double)p.getAngle());
         }

         SVGOMDocument d = (SVGOMDocument)this.ownerDocument;
         d.getAnimatedAttributeListener().otherAnimationChanged(this, type);
      } else {
         super.updateOtherValue(type, val);
      }

   }

   static {
      DoublyIndexedTable t = new DoublyIndexedTable(SVGStylableElement.xmlTraitInformation);
      t.put((Object)null, "transform", new TraitInformation(true, 9));
      t.put((Object)null, "externalResourcesRequired", new TraitInformation(true, 49));
      xmlTraitInformation = t;
   }
}
