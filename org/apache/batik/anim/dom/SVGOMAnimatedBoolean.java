package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableBooleanValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedBoolean;

public class SVGOMAnimatedBoolean extends AbstractSVGAnimatedValue implements SVGAnimatedBoolean {
   protected boolean defaultValue;
   protected boolean valid;
   protected boolean baseVal;
   protected boolean animVal;
   protected boolean changing;

   public SVGOMAnimatedBoolean(AbstractElement elt, String ns, String ln, boolean val) {
      super(elt, ns, ln);
      this.defaultValue = val;
   }

   public boolean getBaseVal() {
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
         this.baseVal = attr.getValue().equals("true");
      }

      this.valid = true;
   }

   public void setBaseVal(boolean baseVal) throws DOMException {
      try {
         this.baseVal = baseVal;
         this.valid = true;
         this.changing = true;
         this.element.setAttributeNS(this.namespaceURI, this.localName, String.valueOf(baseVal));
      } finally {
         this.changing = false;
      }

   }

   public boolean getAnimVal() {
      if (this.hasAnimVal) {
         return this.animVal;
      } else {
         if (!this.valid) {
            this.update();
         }

         return this.baseVal;
      }
   }

   public void setAnimatedValue(boolean animVal) {
      this.hasAnimVal = true;
      this.animVal = animVal;
      this.fireAnimatedAttributeListeners();
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         this.animVal = ((AnimatableBooleanValue)val).getValue();
      }

      this.fireAnimatedAttributeListeners();
   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      return new AnimatableBooleanValue(target, this.getBaseVal());
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
