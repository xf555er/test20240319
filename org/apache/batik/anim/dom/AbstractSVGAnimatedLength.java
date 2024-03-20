package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableLengthValue;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.LiveAttributeException;
import org.apache.batik.dom.svg.LiveAttributeValue;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAnimatedLength;
import org.w3c.dom.svg.SVGLength;

public abstract class AbstractSVGAnimatedLength extends AbstractSVGAnimatedValue implements SVGAnimatedLength, LiveAttributeValue {
   public static final short HORIZONTAL_LENGTH = 2;
   public static final short VERTICAL_LENGTH = 1;
   public static final short OTHER_LENGTH = 0;
   protected short direction;
   protected BaseSVGLength baseVal;
   protected AnimSVGLength animVal;
   protected boolean changing;
   protected boolean nonNegative;

   public AbstractSVGAnimatedLength(AbstractElement elt, String ns, String ln, short dir, boolean nonneg) {
      super(elt, ns, ln);
      this.direction = dir;
      this.nonNegative = nonneg;
   }

   protected abstract String getDefaultValue();

   public SVGLength getBaseVal() {
      if (this.baseVal == null) {
         this.baseVal = new BaseSVGLength(this.direction);
      }

      return this.baseVal;
   }

   public SVGLength getAnimVal() {
      if (this.animVal == null) {
         this.animVal = new AnimSVGLength(this.direction);
      }

      return this.animVal;
   }

   public float getCheckedValue() {
      if (this.hasAnimVal) {
         if (this.animVal == null) {
            this.animVal = new AnimSVGLength(this.direction);
         }

         if (this.nonNegative && this.animVal.value < 0.0F) {
            throw new LiveAttributeException(this.element, this.localName, (short)2, this.animVal.getValueAsString());
         } else {
            return this.animVal.getValue();
         }
      } else {
         if (this.baseVal == null) {
            this.baseVal = new BaseSVGLength(this.direction);
         }

         this.baseVal.revalidate();
         if (this.baseVal.missing) {
            throw new LiveAttributeException(this.element, this.localName, (short)0, (String)null);
         } else if (this.baseVal.unitType == 0) {
            throw new LiveAttributeException(this.element, this.localName, (short)1, this.baseVal.getValueAsString());
         } else if (this.nonNegative && this.baseVal.value < 0.0F) {
            throw new LiveAttributeException(this.element, this.localName, (short)2, this.baseVal.getValueAsString());
         } else {
            return this.baseVal.getValue();
         }
      }
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      if (val == null) {
         this.hasAnimVal = false;
      } else {
         this.hasAnimVal = true;
         AnimatableLengthValue animLength = (AnimatableLengthValue)val;
         if (this.animVal == null) {
            this.animVal = new AnimSVGLength(this.direction);
         }

         this.animVal.setAnimatedValue(animLength.getLengthType(), animLength.getLengthValue());
      }

      this.fireAnimatedAttributeListeners();
   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      SVGLength base = this.getBaseVal();
      return new AnimatableLengthValue(target, base.getUnitType(), base.getValueInSpecifiedUnits(), target.getPercentageInterpretation(this.getNamespaceURI(), this.getLocalName(), false));
   }

   public void attrAdded(Attr node, String newv) {
      this.attrChanged();
   }

   public void attrModified(Attr node, String oldv, String newv) {
      this.attrChanged();
   }

   public void attrRemoved(Attr node, String oldv) {
      this.attrChanged();
   }

   protected void attrChanged() {
      if (!this.changing && this.baseVal != null) {
         this.baseVal.invalidate();
      }

      this.fireBaseAttributeListeners();
      if (!this.hasAnimVal) {
         this.fireAnimatedAttributeListeners();
      }

   }

   protected class AnimSVGLength extends AbstractSVGLength {
      public AnimSVGLength(short direction) {
         super(direction);
      }

      public short getUnitType() {
         return AbstractSVGAnimatedLength.this.hasAnimVal ? super.getUnitType() : AbstractSVGAnimatedLength.this.getBaseVal().getUnitType();
      }

      public float getValue() {
         return AbstractSVGAnimatedLength.this.hasAnimVal ? super.getValue() : AbstractSVGAnimatedLength.this.getBaseVal().getValue();
      }

      public float getValueInSpecifiedUnits() {
         return AbstractSVGAnimatedLength.this.hasAnimVal ? super.getValueInSpecifiedUnits() : AbstractSVGAnimatedLength.this.getBaseVal().getValueInSpecifiedUnits();
      }

      public String getValueAsString() {
         return AbstractSVGAnimatedLength.this.hasAnimVal ? super.getValueAsString() : AbstractSVGAnimatedLength.this.getBaseVal().getValueAsString();
      }

      public void setValue(float value) throws DOMException {
         throw AbstractSVGAnimatedLength.this.element.createDOMException((short)7, "readonly.length", (Object[])null);
      }

      public void setValueInSpecifiedUnits(float value) throws DOMException {
         throw AbstractSVGAnimatedLength.this.element.createDOMException((short)7, "readonly.length", (Object[])null);
      }

      public void setValueAsString(String value) throws DOMException {
         throw AbstractSVGAnimatedLength.this.element.createDOMException((short)7, "readonly.length", (Object[])null);
      }

      public void newValueSpecifiedUnits(short unit, float value) {
         throw AbstractSVGAnimatedLength.this.element.createDOMException((short)7, "readonly.length", (Object[])null);
      }

      public void convertToSpecifiedUnits(short unit) {
         throw AbstractSVGAnimatedLength.this.element.createDOMException((short)7, "readonly.length", (Object[])null);
      }

      protected SVGOMElement getAssociatedElement() {
         return (SVGOMElement)AbstractSVGAnimatedLength.this.element;
      }

      protected void setAnimatedValue(int type, float val) {
         super.newValueSpecifiedUnits((short)type, val);
      }
   }

   protected class BaseSVGLength extends AbstractSVGLength {
      protected boolean valid;
      protected boolean missing;

      public BaseSVGLength(short direction) {
         super(direction);
      }

      public void invalidate() {
         this.valid = false;
      }

      protected void reset() {
         try {
            AbstractSVGAnimatedLength.this.changing = true;
            this.valid = true;
            String value = this.getValueAsString();
            AbstractSVGAnimatedLength.this.element.setAttributeNS(AbstractSVGAnimatedLength.this.namespaceURI, AbstractSVGAnimatedLength.this.localName, value);
         } finally {
            AbstractSVGAnimatedLength.this.changing = false;
         }

      }

      protected void revalidate() {
         if (!this.valid) {
            this.missing = false;
            this.valid = true;
            Attr attr = AbstractSVGAnimatedLength.this.element.getAttributeNodeNS(AbstractSVGAnimatedLength.this.namespaceURI, AbstractSVGAnimatedLength.this.localName);
            String s;
            if (attr == null) {
               s = AbstractSVGAnimatedLength.this.getDefaultValue();
               if (s == null) {
                  this.missing = true;
                  return;
               }
            } else {
               s = attr.getValue();
            }

            this.parse(s);
         }
      }

      protected SVGOMElement getAssociatedElement() {
         return (SVGOMElement)AbstractSVGAnimatedLength.this.element;
      }
   }
}
