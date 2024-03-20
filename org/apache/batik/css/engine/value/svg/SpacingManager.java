package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class SpacingManager extends LengthManager {
   protected String property;

   public SpacingManager(String prop) {
      this.property = prop;
   }

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
      return 42;
   }

   public String getPropertyName() {
      return this.property;
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NORMAL_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("normal")) {
               return SVGValueConstants.NORMAL_VALUE;
            }

            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         default:
            return super.createValue(lu, engine);
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (value.equalsIgnoreCase("normal")) {
         return SVGValueConstants.NORMAL_VALUE;
      } else {
         throw this.createInvalidIdentifierDOMException(value);
      }
   }

   protected int getOrientation() {
      return 2;
   }
}
