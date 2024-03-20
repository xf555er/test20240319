package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class OpacityManager extends AbstractValueManager {
   protected boolean inherited;
   protected String property;

   public OpacityManager(String prop, boolean inherit) {
      this.property = prop;
      this.inherited = inherit;
   }

   public boolean isInheritedProperty() {
      return this.inherited;
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return true;
   }

   public int getPropertyType() {
      return 25;
   }

   public String getPropertyName() {
      return this.property;
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NUMBER_1;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 13:
            return new FloatValue((short)1, (float)lu.getIntegerValue());
         case 14:
            return new FloatValue((short)1, lu.getFloatValue());
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createFloatValue(short type, float floatValue) throws DOMException {
      if (type == 1) {
         return new FloatValue(type, floatValue);
      } else {
         throw this.createInvalidFloatTypeDOMException(type);
      }
   }
}
