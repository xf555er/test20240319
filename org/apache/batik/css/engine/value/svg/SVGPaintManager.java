package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class SVGPaintManager extends SVGColorManager {
   public SVGPaintManager(String prop) {
      super(prop);
   }

   public SVGPaintManager(String prop, Value v) {
      super(prop, v);
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
      return 7;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 24:
            String value = lu.getStringValue();
            String uri = resolveURI(engine.getCSSBaseURI(), value);
            lu = lu.getNextLexicalUnit();
            if (lu == null) {
               return new URIValue(value, uri);
            } else {
               ListValue result = new ListValue(' ');
               result.append(new URIValue(value, uri));
               if (lu.getLexicalUnitType() == 35 && lu.getStringValue().equalsIgnoreCase("none")) {
                  result.append(SVGValueConstants.NONE_VALUE);
                  return result;
               } else {
                  Value v = super.createValue(lu, engine);
                  if (v.getCssValueType() == 3) {
                     ListValue lv = (ListValue)v;

                     for(int i = 0; i < lv.getLength(); ++i) {
                        result.append(lv.item(i));
                     }
                  } else {
                     result.append(v);
                  }

                  return result;
               }
            }
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("none")) {
               return SVGValueConstants.NONE_VALUE;
            }
         default:
            return super.createValue(lu, engine);
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value == SVGValueConstants.NONE_VALUE) {
         return value;
      } else {
         if (value.getCssValueType() == 2) {
            ListValue lv = (ListValue)value;
            Value v = lv.item(0);
            if (v.getPrimitiveType() == 20) {
               v = lv.item(1);
               if (v == SVGValueConstants.NONE_VALUE) {
                  return value;
               }

               Value t = super.computeValue(elt, pseudo, engine, idx, sm, v);
               if (t != v) {
                  ListValue result = new ListValue(' ');
                  result.append(lv.item(0));
                  result.append(t);
                  if (lv.getLength() == 3) {
                     result.append(lv.item(1));
                  }

                  return result;
               }

               return value;
            }
         }

         return super.computeValue(elt, pseudo, engine, idx, sm, value);
      }
   }
}
