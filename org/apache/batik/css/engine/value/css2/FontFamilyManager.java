package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class FontFamilyManager extends AbstractValueManager {
   protected static final ListValue DEFAULT_VALUE = new ListValue();
   protected static final StringMap values;

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
      return 26;
   }

   public String getPropertyName() {
      return "font-family";
   }

   public Value getDefaultValue() {
      return DEFAULT_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 35:
         case 36:
            ListValue result = new ListValue();

            do {
               switch (lu.getLexicalUnitType()) {
                  case 35:
                     StringBuffer sb = new StringBuffer(lu.getStringValue());
                     lu = lu.getNextLexicalUnit();
                     if (lu != null && this.isIdentOrNumber(lu)) {
                        do {
                           sb.append(' ');
                           switch (lu.getLexicalUnitType()) {
                              case 13:
                                 sb.append(Integer.toString(lu.getIntegerValue()));
                                 break;
                              case 35:
                                 sb.append(lu.getStringValue());
                           }

                           lu = lu.getNextLexicalUnit();
                        } while(lu != null && this.isIdentOrNumber(lu));

                        result.append(new StringValue((short)19, sb.toString()));
                     } else {
                        String id = sb.toString();
                        String s = id.toLowerCase().intern();
                        Value v = (Value)values.get(s);
                        result.append((Value)(v != null ? v : new StringValue((short)19, id)));
                     }
                     break;
                  case 36:
                     result.append(new StringValue((short)19, lu.getStringValue()));
                     lu = lu.getNextLexicalUnit();
               }

               if (lu == null) {
                  return result;
               }

               if (lu.getLexicalUnitType() != 0) {
                  throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
               }

               lu = lu.getNextLexicalUnit();
            } while(lu != null);

            throw this.createMalformedLexicalUnitDOMException();
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   private boolean isIdentOrNumber(LexicalUnit lu) {
      short type = lu.getLexicalUnitType();
      switch (type) {
         case 13:
         case 35:
            return true;
         default:
            return false;
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value == DEFAULT_VALUE) {
         CSSContext ctx = engine.getCSSContext();
         value = ctx.getDefaultFontFamily();
      }

      return value;
   }

   static {
      DEFAULT_VALUE.append(new StringValue((short)19, "Arial"));
      DEFAULT_VALUE.append(new StringValue((short)19, "Helvetica"));
      DEFAULT_VALUE.append(new StringValue((short)21, "sans-serif"));
      values = new StringMap();
      values.put("cursive", ValueConstants.CURSIVE_VALUE);
      values.put("fantasy", ValueConstants.FANTASY_VALUE);
      values.put("monospace", ValueConstants.MONOSPACE_VALUE);
      values.put("serif", ValueConstants.SERIF_VALUE);
      values.put("sans-serif", ValueConstants.SANS_SERIF_VALUE);
   }
}
