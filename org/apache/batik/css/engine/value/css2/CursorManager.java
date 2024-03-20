package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class CursorManager extends AbstractValueManager {
   protected static final StringMap values = new StringMap();

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
      return 21;
   }

   public String getPropertyName() {
      return "cursor";
   }

   public Value getDefaultValue() {
      return ValueConstants.AUTO_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      ListValue result = new ListValue();
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 24:
            do {
               result.append(new URIValue(lu.getStringValue(), resolveURI(engine.getCSSBaseURI(), lu.getStringValue())));
               lu = lu.getNextLexicalUnit();
               if (lu == null) {
                  throw this.createMalformedLexicalUnitDOMException();
               }

               if (lu.getLexicalUnitType() != 0) {
                  throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
               }

               lu = lu.getNextLexicalUnit();
               if (lu == null) {
                  throw this.createMalformedLexicalUnitDOMException();
               }
            } while(lu.getLexicalUnitType() == 24);

            if (lu.getLexicalUnitType() != 35) {
               throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
            }
         case 35:
            String s = lu.getStringValue().toLowerCase().intern();
            Object v = values.get(s);
            if (v == null) {
               throw this.createInvalidIdentifierDOMException(lu.getStringValue());
            }

            result.append((Value)v);
            lu = lu.getNextLexicalUnit();
      }

      if (lu != null) {
         throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      } else {
         return result;
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value.getCssValueType() == 2) {
         ListValue lv = (ListValue)value;
         int len = lv.getLength();
         ListValue result = new ListValue(' ');

         for(int i = 0; i < len; ++i) {
            Value v = lv.item(0);
            if (v.getPrimitiveType() == 20) {
               result.append(new URIValue(v.getStringValue(), v.getStringValue()));
            } else {
               result.append(v);
            }
         }

         return result;
      } else {
         return super.computeValue(elt, pseudo, engine, idx, sm, value);
      }
   }

   static {
      values.put("auto", ValueConstants.AUTO_VALUE);
      values.put("crosshair", ValueConstants.CROSSHAIR_VALUE);
      values.put("default", ValueConstants.DEFAULT_VALUE);
      values.put("e-resize", ValueConstants.E_RESIZE_VALUE);
      values.put("help", ValueConstants.HELP_VALUE);
      values.put("move", ValueConstants.MOVE_VALUE);
      values.put("n-resize", ValueConstants.N_RESIZE_VALUE);
      values.put("ne-resize", ValueConstants.NE_RESIZE_VALUE);
      values.put("nw-resize", ValueConstants.NW_RESIZE_VALUE);
      values.put("pointer", ValueConstants.POINTER_VALUE);
      values.put("s-resize", ValueConstants.S_RESIZE_VALUE);
      values.put("se-resize", ValueConstants.SE_RESIZE_VALUE);
      values.put("sw-resize", ValueConstants.SW_RESIZE_VALUE);
      values.put("text", ValueConstants.TEXT_VALUE);
      values.put("w-resize", ValueConstants.W_RESIZE_VALUE);
      values.put("wait", ValueConstants.WAIT_VALUE);
   }
}
