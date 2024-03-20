package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class KerningManager extends LengthManager {
   public boolean isInheritedProperty() {
      return true;
   }

   public String getPropertyName() {
      return "kerning";
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return true;
   }

   public int getPropertyType() {
      return 41;
   }

   public Value getDefaultValue() {
      return SVGValueConstants.AUTO_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("auto")) {
               return SVGValueConstants.AUTO_VALUE;
            }

            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         default:
            return super.createValue(lu, engine);
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (value.equalsIgnoreCase("auto")) {
         return SVGValueConstants.AUTO_VALUE;
      } else {
         throw this.createInvalidIdentifierDOMException(value);
      }
   }

   protected int getOrientation() {
      return 0;
   }
}
