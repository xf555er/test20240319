package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableNumberValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedNumber;

public class SVGOMAnimatedNumber extends AbstractSVGAnimatedValue implements SVGAnimatedNumber {
   protected float defaultValue;
   protected boolean allowPercentage;
   protected boolean valid;
   protected float baseVal;
   protected float animVal;
   protected boolean changing;

   public SVGOMAnimatedNumber(AbstractElement elt, String ns, String ln, float val) {
      this(elt, ns, ln, val, false);
   }

   public SVGOMAnimatedNumber(AbstractElement elt, String ns, String ln, float val, boolean allowPercentage) {
      super(elt, ns, ln);
      this.defaultValue = val;
      this.allowPercentage = allowPercentage;
   }

   public float getBaseVal() {
      if (!this.valid) {
         this.update();
      }

      return this.baseVal;
   }

   protected void update() {
      Attr attr = this.element.getAttributeNodeNS(this.namespaceURI, this.localName);
      if (attr == null) {
         this.baseVal = this.defaultValue;
      } else {
         String v = attr.getValue();
         int len = v.length();
         if (this.allowPercentage && len > 1 && v.charAt(len - 1) == '%') {
            this.baseVal = 0.01F * Float.parseFloat(v.substring(0, len - 1));
         } else {
            this.baseVal = Float.parseFloat(v);
         }
      }

      this.valid = true;
   }

   public void setBaseVal(float baseVal) throws DOMException {
      try {
         this.baseVal = baseVal;
         this.valid = true;
         this.changing = true;
         this.element.setAttributeNS(this.namespaceURI, this.localName, String.valueOf(baseVal));
      } finally {
         this.changing = false;
      }

   }

   public float getAnimVal() {
      if (this.hasAnimVal) {
         return this.animVal;
      } else {
         if (!this.valid) {
            this.update();
         }

         return this.baseVal;
      }
   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      return new AnimatableNumberValue(target, this.getBaseVal());
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         this.animVal = ((AnimatableNumberValue)val).getValue();
      }

      this.fireAnimatedAttributeListeners();
   }

   public void attrAdded(Attr node, String newv) {
      if (!this.changing) {
         this.valid = false;
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrModified(Attr node, String oldv, String newv) {
      if (!this.changing) {
         this.valid = false;
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrRemoved(Attr node, String oldv) {
      if (!this.changing) {
         this.valid = false;
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }
}
