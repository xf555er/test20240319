package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class TextDecorationManager extends AbstractValueManager {
   protected static final StringMap values = new StringMap();

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
      return 18;
   }

   public String getPropertyName() {
      return "text-decoration";
   }

   public Value getDefaultValue() {
      return ValueConstants.NONE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("none")) {
               return ValueConstants.NONE_VALUE;
            } else {
               ListValue lv = new ListValue(' ');

               do {
                  if (lu.getLexicalUnitType() != 35) {
                     throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
                  }

                  String s = lu.getStringValue().toLowerCase().intern();
                  Object obj = values.get(s);
                  if (obj == null) {
                     throw this.createInvalidIdentifierDOMException(lu.getStringValue());
                  }

                  lv.append((Value)obj);
                  lu = lu.getNextLexicalUnit();
               } while(lu != null);

               return lv;
            }
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (!value.equalsIgnoreCase("none")) {
         throw this.createInvalidIdentifierDOMException(value);
      } else {
         return ValueConstants.NONE_VALUE;
      }
   }

   static {
      values.put("blink", ValueConstants.BLINK_VALUE);
      values.put("line-through", ValueConstants.LINE_THROUGH_VALUE);
      values.put("overline", ValueConstants.OVERLINE_VALUE);
      values.put("underline", ValueConstants.UNDERLINE_VALUE);
   }
}
