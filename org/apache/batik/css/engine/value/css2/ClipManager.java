package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.RectManager;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class ClipManager extends RectManager {
   public boolean isInheritedProperty() {
      return false;
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public int getPropertyType() {
      return 19;
   }

   public String getPropertyName() {
      return "clip";
   }

   public Value getDefaultValue() {
      return ValueConstants.AUTO_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return InheritValue.INSTANCE;
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("auto")) {
               return ValueConstants.AUTO_VALUE;
            }
         default:
            return super.createValue(lu, engine);
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (!value.equalsIgnoreCase("auto")) {
         throw this.createInvalidIdentifierDOMException(value);
      } else {
         return ValueConstants.AUTO_VALUE;
      }
   }
}
