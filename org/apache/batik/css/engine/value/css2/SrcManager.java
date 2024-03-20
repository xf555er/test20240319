package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class SrcManager extends IdentifierManager {
   protected static final StringMap values = new StringMap();

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
      return 38;
   }

   public String getPropertyName() {
      return "src";
   }

   public Value getDefaultValue() {
      return ValueConstants.NONE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 24:
         case 35:
         case 36:
            ListValue result = new ListValue();

            do {
               switch (lu.getLexicalUnitType()) {
                  case 24:
                     String uri = resolveURI(engine.getCSSBaseURI(), lu.getStringValue());
                     result.append(new URIValue(lu.getStringValue(), uri));
                     lu = lu.getNextLexicalUnit();
                     if (lu != null && lu.getLexicalUnitType() == 41 && lu.getFunctionName().equalsIgnoreCase("format")) {
                        lu = lu.getNextLexicalUnit();
                     }
                     break;
                  case 35:
                     StringBuffer sb = new StringBuffer(lu.getStringValue());
                     lu = lu.getNextLexicalUnit();
                     if (lu != null && lu.getLexicalUnitType() == 35) {
                        do {
                           sb.append(' ');
                           sb.append(lu.getStringValue());
                           lu = lu.getNextLexicalUnit();
                        } while(lu != null && lu.getLexicalUnitType() == 35);

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

   public StringMap getIdentifiers() {
      return values;
   }

   static {
      values.put("none", ValueConstants.NONE_VALUE);
   }
}
