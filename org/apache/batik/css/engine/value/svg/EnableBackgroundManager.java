package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class EnableBackgroundManager extends LengthManager {
   protected int orientation;

   public boolean isInheritedProperty() {
      return false;
   }

   public boolean isAnimatableProperty() {
      return false;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public int getPropertyType() {
      return 23;
   }

   public String getPropertyName() {
      return "enable-background";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.ACCUMULATE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 35:
            String id = lu.getStringValue().toLowerCase().intern();
            if (id == "accumulate") {
               return SVGValueConstants.ACCUMULATE_VALUE;
            } else if (id != "new") {
               throw this.createInvalidIdentifierDOMException(id);
            } else {
               ListValue result = new ListValue(' ');
               result.append(SVGValueConstants.NEW_VALUE);
               lu = lu.getNextLexicalUnit();
               if (lu == null) {
                  return result;
               } else {
                  result.append(super.createValue(lu, engine));

                  for(int i = 1; i < 4; ++i) {
                     lu = lu.getNextLexicalUnit();
                     if (lu == null) {
                        throw this.createMalformedLexicalUnitDOMException();
                     }

                     result.append(super.createValue(lu, engine));
                  }

                  return result;
               }
            }
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (!value.equalsIgnoreCase("accumulate")) {
         throw this.createInvalidIdentifierDOMException(value);
      } else {
         return SVGValueConstants.ACCUMULATE_VALUE;
      }
   }

   public Value createFloatValue(short unitType, float floatValue) throws DOMException {
      throw this.createDOMException();
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value.getCssValueType() == 2) {
         ListValue lv = (ListValue)value;
         if (lv.getLength() == 5) {
            Value lv1 = lv.item(1);
            this.orientation = 0;
            Value v1 = super.computeValue(elt, pseudo, engine, idx, sm, lv1);
            Value lv2 = lv.item(2);
            this.orientation = 1;
            Value v2 = super.computeValue(elt, pseudo, engine, idx, sm, lv2);
            Value lv3 = lv.item(3);
            this.orientation = 0;
            Value v3 = super.computeValue(elt, pseudo, engine, idx, sm, lv3);
            Value lv4 = lv.item(4);
            this.orientation = 1;
            Value v4 = super.computeValue(elt, pseudo, engine, idx, sm, lv4);
            if (lv1 != v1 || lv2 != v2 || lv3 != v3 || lv4 != v4) {
               ListValue result = new ListValue(' ');
               result.append(lv.item(0));
               result.append(v1);
               result.append(v2);
               result.append(v3);
               result.append(v4);
               return result;
            }
         }
      }

      return value;
   }

   protected int getOrientation() {
      return this.orientation;
   }
}
