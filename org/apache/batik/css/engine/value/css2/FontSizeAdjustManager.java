package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class FontSizeAdjustManager extends AbstractValueManager {
   public boolean isInheritedProperty() {
      return true;
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public int getPropertyType() {
      return 44;
   }

   public String getPropertyName() {
      return "font-size-adjust";
   }

   public Value getDefaultValue() {
      return ValueConstants.NONE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 13:
            return new FloatValue((short)1, (float)lu.getIntegerValue());
         case 14:
            return new FloatValue((short)1, lu.getFloatValue());
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("none")) {
               return ValueConstants.NONE_VALUE;
            }

            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (value.equalsIgnoreCase("none")) {
         return ValueConstants.NONE_VALUE;
      } else {
         throw this.createInvalidIdentifierDOMException(value);
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
