package org.apache.batik.css.engine.value;

import org.apache.batik.css.engine.CSSEngine;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public abstract class IdentifierManager extends AbstractValueManager {
   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 35:
            String s = lu.getStringValue().toLowerCase().intern();
            Object v = this.getIdentifiers().get(s);
            if (v == null) {
               throw this.createInvalidIdentifierDOMException(lu.getStringValue());
            }

            return (Value)v;
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else {
         Object v = this.getIdentifiers().get(value.toLowerCase().intern());
         if (v == null) {
            throw this.createInvalidIdentifierDOMException(value);
         } else {
            return (Value)v;
         }
      }
   }

   public abstract StringMap getIdentifiers();
}
