package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableIntegerValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedInteger;

public class SVGOMAnimatedInteger extends AbstractSVGAnimatedValue implements SVGAnimatedInteger {
   protected int defaultValue;
   protected boolean valid;
   protected int baseVal;
   protected int animVal;
   protected boolean changing;

   public SVGOMAnimatedInteger(AbstractElement elt, String ns, String ln, int val) {
      super(elt, ns, ln);
      this.defaultValue = val;
   }

   public int getBaseVal() {
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
         this.baseVal = Integer.parseInt(attr.getValue());
      }

      this.valid = true;
   }

   public void setBaseVal(int baseVal) throws DOMException {
      try {
         this.baseVal = baseVal;
         this.valid = true;
         this.changing = true;
         this.element.setAttributeNS(this.namespaceURI, this.localName, String.valueOf(baseVal));
      } finally {
         this.changing = false;
      }

   }

   public int getAnimVal() {
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
      return new AnimatableIntegerValue(target, this.getBaseVal());
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         this.animVal = ((AnimatableIntegerValue)val).getValue();
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
