package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatablePreserveAspectRatioValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.AbstractSVGPreserveAspectRatio;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedPreserveAspectRatio;
import org.w3c.dom.svg.SVGPreserveAspectRatio;

public class SVGOMAnimatedPreserveAspectRatio extends AbstractSVGAnimatedValue implements SVGAnimatedPreserveAspectRatio {
   protected BaseSVGPARValue baseVal;
   protected AnimSVGPARValue animVal;
   protected boolean changing;

   public SVGOMAnimatedPreserveAspectRatio(AbstractElement elt) {
      super(elt, (String)null, "preserveAspectRatio");
   }

   public SVGPreserveAspectRatio getBaseVal() {
      if (this.baseVal == null) {
         this.baseVal = new BaseSVGPARValue();
      }

      return this.baseVal;
   }

   public SVGPreserveAspectRatio getAnimVal() {
      if (this.animVal == null) {
         this.animVal = new AnimSVGPARValue();
      }

      return this.animVal;
   }

   public void check() {
      if (!this.hasAnimVal) {
         if (this.baseVal == null) {
            this.baseVal = new BaseSVGPARValue();
         }

         if (this.baseVal.malformed) {
            throw new LiveAttributeException(this.element, this.localName, (short)1, this.baseVal.getValueAsString());
         }
      }

   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      SVGPreserveAspectRatio par = this.getBaseVal();
      return new AnimatablePreserveAspectRatioValue(target, par.getAlign(), par.getMeetOrSlice());
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         if (this.animVal == null) {
            this.animVal = new AnimSVGPARValue();
         }

         AnimatablePreserveAspectRatioValue animPAR = (AnimatablePreserveAspectRatioValue)val;
         this.animVal.setAnimatedValue(animPAR.getAlign(), animPAR.getMeetOrSlice());
      }

      this.fireAnimatedAttributeListeners();
   }

   public void attrAdded(Attr node, String newv) {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrModified(Attr node, String oldv, String newv) {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrRemoved(Attr node, String oldv) {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public class AnimSVGPARValue extends AbstractSVGPreserveAspectRatio {
      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedPreserveAspectRatio.this.element.createDOMException(type, key, args);
      }

      protected void setAttributeValue(String value) throws DOMException {
      }

      public short getAlign() {
         return SVGOMAnimatedPreserveAspectRatio.this.hasAnimVal ? super.getAlign() : SVGOMAnimatedPreserveAspectRatio.this.getBaseVal().getAlign();
      }

      public short getMeetOrSlice() {
         return SVGOMAnimatedPreserveAspectRatio.this.hasAnimVal ? super.getMeetOrSlice() : SVGOMAnimatedPreserveAspectRatio.this.getBaseVal().getMeetOrSlice();
      }

      public void setAlign(short align) {
         throw SVGOMAnimatedPreserveAspectRatio.this.element.createDOMException((short)7, "readonly.preserve.aspect.ratio", (Object[])null);
      }

      public void setMeetOrSlice(short meetOrSlice) {
         throw SVGOMAnimatedPreserveAspectRatio.this.element.createDOMException((short)7, "readonly.preserve.aspect.ratio", (Object[])null);
      }

      protected void setAnimatedValue(short align, short meetOrSlice) {
         this.align = align;
         this.meetOrSlice = meetOrSlice;
      }
   }

   public class BaseSVGPARValue extends AbstractSVGPreserveAspectRatio {
      protected boolean malformed;

      public BaseSVGPARValue() {
         this.invalidate();
      }

      protected DOMException createDOMException(short type, String key, Object[] args) {
         return SVGOMAnimatedPreserveAspectRatio.this.element.createDOMException(type, key, args);
      }

      protected void setAttributeValue(String value) throws DOMException {
         try {
            SVGOMAnimatedPreserveAspectRatio.this.changing = true;
            SVGOMAnimatedPreserveAspectRatio.this.element.setAttributeNS((String)null, "preserveAspectRatio", value);
            this.malformed = false;
         } finally {
            SVGOMAnimatedPreserveAspectRatio.this.changing = false;
         }

      }

      protected void invalidate() {
         String s = SVGOMAnimatedPreserveAspectRatio.this.element.getAttributeNS((String)null, "preserveAspectRatio");
         this.setValueAsString(s);
      }
   }
}
