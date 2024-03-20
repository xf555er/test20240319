package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.SVGOMAngle;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGAngle;
import org.w3c.dom.svg.SVGAnimatedAngle;
import org.w3c.dom.svg.SVGAnimatedEnumeration;

public class SVGOMAnimatedMarkerOrientValue extends AbstractSVGAnimatedValue {
   protected boolean valid;
   protected AnimatedAngle animatedAngle = new AnimatedAngle();
   protected AnimatedEnumeration animatedEnumeration = new AnimatedEnumeration();
   protected BaseSVGAngle baseAngleVal;
   protected short baseEnumerationVal;
   protected AnimSVGAngle animAngleVal;
   protected short animEnumerationVal;
   protected boolean changing;

   public SVGOMAnimatedMarkerOrientValue(AbstractElement elt, String ns, String ln) {
      super(elt, ns, ln);
   }

   protected void updateAnimatedValue(AnimatableValue val) {
      throw new UnsupportedOperationException("Animation of marker orient value is not implemented");
   }

   public AnimatableValue getUnderlyingValue(AnimationTarget target) {
      throw new UnsupportedOperationException("Animation of marker orient value is not implemented");
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

   public void setAnimatedValueToAngle(short unitType, float value) {
      this.hasAnimVal = true;
      this.animAngleVal.setAnimatedValue(unitType, value);
      this.animEnumerationVal = 2;
      this.fireAnimatedAttributeListeners();
   }

   public void setAnimatedValueToAuto() {
      this.hasAnimVal = true;
      this.animAngleVal.setAnimatedValue(1, 0.0F);
      this.animEnumerationVal = 1;
      this.fireAnimatedAttributeListeners();
   }

   public void resetAnimatedValue() {
      this.hasAnimVal = false;
      this.fireAnimatedAttributeListeners();
   }

   public SVGAnimatedAngle getAnimatedAngle() {
      return this.animatedAngle;
   }

   public SVGAnimatedEnumeration getAnimatedEnumeration() {
      return this.animatedEnumeration;
   }

   protected class AnimatedEnumeration implements SVGAnimatedEnumeration {
      public short getBaseVal() {
         if (SVGOMAnimatedMarkerOrientValue.this.baseAngleVal == null) {
            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal = SVGOMAnimatedMarkerOrientValue.this.new BaseSVGAngle();
         }

         SVGOMAnimatedMarkerOrientValue.this.baseAngleVal.revalidate();
         return SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal;
      }

      public void setBaseVal(short baseVal) throws DOMException {
         if (baseVal == 1) {
            SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal = baseVal;
            if (SVGOMAnimatedMarkerOrientValue.this.baseAngleVal == null) {
               SVGOMAnimatedMarkerOrientValue.this.baseAngleVal = SVGOMAnimatedMarkerOrientValue.this.new BaseSVGAngle();
            }

            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal.setUnitType((short)1);
            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal.setValue(0.0F);
            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal.reset();
         } else if (baseVal == 2) {
            SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal = baseVal;
            if (SVGOMAnimatedMarkerOrientValue.this.baseAngleVal == null) {
               SVGOMAnimatedMarkerOrientValue.this.baseAngleVal = SVGOMAnimatedMarkerOrientValue.this.new BaseSVGAngle();
            }

            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal.reset();
         }

      }

      public short getAnimVal() {
         if (SVGOMAnimatedMarkerOrientValue.this.hasAnimVal) {
            return SVGOMAnimatedMarkerOrientValue.this.animEnumerationVal;
         } else {
            if (SVGOMAnimatedMarkerOrientValue.this.baseAngleVal == null) {
               SVGOMAnimatedMarkerOrientValue.this.baseAngleVal = SVGOMAnimatedMarkerOrientValue.this.new BaseSVGAngle();
            }

            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal.revalidate();
            return SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal;
         }
      }
   }

   protected class AnimatedAngle implements SVGAnimatedAngle {
      public SVGAngle getBaseVal() {
         if (SVGOMAnimatedMarkerOrientValue.this.baseAngleVal == null) {
            SVGOMAnimatedMarkerOrientValue.this.baseAngleVal = SVGOMAnimatedMarkerOrientValue.this.new BaseSVGAngle();
         }

         return SVGOMAnimatedMarkerOrientValue.this.baseAngleVal;
      }

      public SVGAngle getAnimVal() {
         if (SVGOMAnimatedMarkerOrientValue.this.animAngleVal == null) {
            SVGOMAnimatedMarkerOrientValue.this.animAngleVal = SVGOMAnimatedMarkerOrientValue.this.new AnimSVGAngle();
         }

         return SVGOMAnimatedMarkerOrientValue.this.animAngleVal;
      }
   }

   protected class AnimSVGAngle extends SVGOMAngle {
      public short getUnitType() {
         return SVGOMAnimatedMarkerOrientValue.this.hasAnimVal ? super.getUnitType() : SVGOMAnimatedMarkerOrientValue.this.animatedAngle.getBaseVal().getUnitType();
      }

      public float getValue() {
         return SVGOMAnimatedMarkerOrientValue.this.hasAnimVal ? super.getValue() : SVGOMAnimatedMarkerOrientValue.this.animatedAngle.getBaseVal().getValue();
      }

      public float getValueInSpecifiedUnits() {
         return SVGOMAnimatedMarkerOrientValue.this.hasAnimVal ? super.getValueInSpecifiedUnits() : SVGOMAnimatedMarkerOrientValue.this.animatedAngle.getBaseVal().getValueInSpecifiedUnits();
      }

      public String getValueAsString() {
         return SVGOMAnimatedMarkerOrientValue.this.hasAnimVal ? super.getValueAsString() : SVGOMAnimatedMarkerOrientValue.this.animatedAngle.getBaseVal().getValueAsString();
      }

      public void setValue(float value) throws DOMException {
         throw SVGOMAnimatedMarkerOrientValue.this.element.createDOMException((short)7, "readonly.angle", (Object[])null);
      }

      public void setValueInSpecifiedUnits(float value) throws DOMException {
         throw SVGOMAnimatedMarkerOrientValue.this.element.createDOMException((short)7, "readonly.angle", (Object[])null);
      }

      public void setValueAsString(String value) throws DOMException {
         throw SVGOMAnimatedMarkerOrientValue.this.element.createDOMException((short)7, "readonly.angle", (Object[])null);
      }

      public void newValueSpecifiedUnits(short unit, float value) {
         throw SVGOMAnimatedMarkerOrientValue.this.element.createDOMException((short)7, "readonly.angle", (Object[])null);
      }

      public void convertToSpecifiedUnits(short unit) {
         throw SVGOMAnimatedMarkerOrientValue.this.element.createDOMException((short)7, "readonly.angle", (Object[])null);
      }

      protected void setAnimatedValue(int type, float val) {
         super.newValueSpecifiedUnits((short)type, val);
      }
   }

   protected class BaseSVGAngle extends SVGOMAngle {
      public void invalidate() {
         SVGOMAnimatedMarkerOrientValue.this.valid = false;
      }

      protected void reset() {
         try {
            SVGOMAnimatedMarkerOrientValue.this.changing = true;
            SVGOMAnimatedMarkerOrientValue.this.valid = true;
            String value;
            if (SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal == 2) {
               value = this.getValueAsString();
            } else {
               if (SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal != 1) {
                  return;
               }

               value = "auto";
            }

            SVGOMAnimatedMarkerOrientValue.this.element.setAttributeNS(SVGOMAnimatedMarkerOrientValue.this.namespaceURI, SVGOMAnimatedMarkerOrientValue.this.localName, value);
         } finally {
            SVGOMAnimatedMarkerOrientValue.this.changing = false;
         }

      }

      protected void revalidate() {
         if (!SVGOMAnimatedMarkerOrientValue.this.valid) {
            Attr attr = SVGOMAnimatedMarkerOrientValue.this.element.getAttributeNodeNS(SVGOMAnimatedMarkerOrientValue.this.namespaceURI, SVGOMAnimatedMarkerOrientValue.this.localName);
            if (attr == null) {
               this.setUnitType((short)1);
               this.value = 0.0F;
            } else {
               this.parse(attr.getValue());
            }

            SVGOMAnimatedMarkerOrientValue.this.valid = true;
         }

      }

      protected void parse(String s) {
         if (s.equals("auto")) {
            this.setUnitType((short)1);
            this.value = 0.0F;
            SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal = 1;
         } else {
            super.parse(s);
            if (this.getUnitType() == 0) {
               SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal = 0;
            } else {
               SVGOMAnimatedMarkerOrientValue.this.baseEnumerationVal = 2;
            }
         }

      }
   }
}
