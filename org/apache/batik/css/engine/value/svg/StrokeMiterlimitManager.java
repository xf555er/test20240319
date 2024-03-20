package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class StrokeMiterlimitManager extends AbstractValueManager {
   public boolean isInheritedProperty() {
      return true;
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
      return "stroke-miterlimit";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NUMBER_4;
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

   public Value createFloatValue(short unitType, float floatValue) throws DOMException {
      if (unitType == 1) {
         return new FloatValue(unitType, floatValue);
      } else {
         throw this.createInvalidFloatTypeDOMException(unitType);
      }
   }
}
