package org.apache.batik.anim.dom;

import java.awt.geom.AffineTransform;
import org.apache.batik.anim.values.AnimatableMotionPointValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGMotionAnimatableElement;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedTransformList;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGMatrix;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTextElement;

public class SVGOMTextElement extends SVGOMTextPositioningElement implements SVGTextElement, SVGMotionAnimatableElement {
   protected static final String X_DEFAULT_VALUE = "0";
   protected static final String Y_DEFAULT_VALUE = "0";
   protected static DoublyIndexedTable xmlTraitInformation;
   protected SVGOMAnimatedTransformList transform;
   protected AffineTransform motionTransform;

   protected SVGOMTextElement() {
   }

   public SVGOMTextElement(String prefix, AbstractDocument owner) {
      super(prefix, owner);
      this.initializeLiveAttributes();
   }

   protected void initializeAllLiveAttributes() {
      super.initializeAllLiveAttributes();
      this.initializeLiveAttributes();
   }

   private void initializeLiveAttributes() {
      this.transform = this.createLiveAnimatedTransformList((String)null, "transform", "");
   }

   public String getLocalName() {
      return "text";
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

   protected String getDefaultXValue() {
      return "0";
   }

   protected String getDefaultYValue() {
      return "0";
   }

   protected Node newNode() {
      return new SVGOMTextElement();
   }

   protected DoublyIndexedTable getTraitInformationTable() {
      return xmlTraitInformation;
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
      DoublyIndexedTable t = new DoublyIndexedTable(SVGOMTextPositioningElement.xmlTraitInformation);
      t.put((Object)null, "transform", new TraitInformation(true, 9));
      xmlTraitInformation = t;
   }
}
