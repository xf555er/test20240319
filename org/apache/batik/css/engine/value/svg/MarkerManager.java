package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class MarkerManager extends AbstractValueManager {
   protected String property;

   public MarkerManager(String prop) {
      this.property = prop;
   }

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
      return 20;
   }

   public String getPropertyName() {
      return this.property;
   }

   public Value getDefaultValue() {
      return ValueConstants.NONE_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 24:
            return new URIValue(lu.getStringValue(), resolveURI(engine.getCSSBaseURI(), lu.getStringValue()));
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("none")) {
               return ValueConstants.NONE_VALUE;
            }
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      switch (type) {
         case 20:
            return new URIValue(value, resolveURI(engine.getCSSBaseURI(), value));
         case 21:
            if (value.equalsIgnoreCase("none")) {
               return ValueConstants.NONE_VALUE;
            }
         default:
            throw this.createInvalidStringTypeDOMException(type);
      }
   }
}
