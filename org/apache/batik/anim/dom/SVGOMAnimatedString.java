package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableStringValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedString;

public class SVGOMAnimatedString extends AbstractSVGAnimatedValue implements SVGAnimatedString {
   protected String animVal;

   public SVGOMAnimatedString(AbstractElement elt, String ns, String ln) {
      super(elt, ns, ln);
   }

   public String getBaseVal() {
      return this.element.getAttributeNS(this.namespaceURI, this.localName);
   }

   public void setBaseVal(String baseVal) throws DOMException {
      this.element.setAttributeNS(this.namespaceURI, this.localName, baseVal);
   }

   public String getAnimVal() {
      return this.hasAnimVal ? this.animVal : this.element.getAttributeNS(this.namespaceURI, this.localName);
   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      return new AnimatableStringValue(target, this.getBaseVal());
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         this.animVal = ((AnimatableStringValue)val).getString();
      }

      this.fireAnimatedAttributeListeners();
   }

   public void attrAdded(Attr node, String newv) {
      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrModified(Attr node, String oldv, String newv) {
      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   public void attrRemoved(Attr node, String oldv) {
      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }
}
