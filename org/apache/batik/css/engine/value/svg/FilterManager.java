package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class FilterManager extends AbstractValueManager {
   public boolean isInheritedProperty() {
      return false;
   }

   public String getPropertyName() {
      return "filter";
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public int getPropertyType() {
      return 20;
   }

   public Value getDefaultValue() {
      return SVGValueConstants.NONE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 24:
            return new URIValue(lu.getStringValue(), resolveURI(engine.getCSSBaseURI(), lu.getStringValue()));
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("none")) {
               return SVGValueConstants.NONE_VALUE;
            }

            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type == 21) {
         if (value.equalsIgnoreCase("none")) {
            return SVGValueConstants.NONE_VALUE;
         } else {
            throw this.createInvalidIdentifierDOMException(value);
         }
      } else if (type == 20) {
         return new URIValue(value, resolveURI(engine.getCSSBaseURI(), value));
      } else {
         throw this.createInvalidStringTypeDOMException(type);
      }
   }
}
