package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class StrokeDasharrayManager extends LengthManager {
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
      return 34;
   }

   public String getPropertyName() {
      return "stroke-dasharray";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NONE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("none")) {
               return SVGValueConstants.NONE_VALUE;
            }

            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         default:
            ListValue lv = new ListValue(' ');

            do {
               Value v = super.createValue(lu, engine);
               lv.append(v);
               lu = lu.getNextLexicalUnit();
               if (lu != null && lu.getLexicalUnitType() == 0) {
                  lu = lu.getNextLexicalUnit();
               }
            } while(lu != null);

            return lv;
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (value.equalsIgnoreCase("none")) {
         return SVGValueConstants.NONE_VALUE;
      } else {
         throw this.createInvalidIdentifierDOMException(value);
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      switch (value.getCssValueType()) {
         case 1:
            return value;
         default:
            ListValue lv = (ListValue)value;
            ListValue result = new ListValue(' ');

            for(int i = 0; i < lv.getLength(); ++i) {
               result.append(super.computeValue(elt, pseudo, engine, idx, sm, lv.item(i)));
            }

            return result;
      }
   }

   protected int getOrientation() {
      return 2;
   }
}
