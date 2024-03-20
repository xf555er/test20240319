package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class ColorProfileManager extends AbstractValueManager {
   public boolean isInheritedProperty() {
      return true;
   }

   public String getPropertyName() {
      return "color-profile";
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
      return SVGValueConstants.AUTO_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 24:
            return new URIValue(lu.getStringValue(), resolveURI(engine.getCSSBaseURI(), lu.getStringValue()));
         case 35:
            String s = lu.getStringValue().toLowerCase();
            if (s.equals("auto")) {
               return SVGValueConstants.AUTO_VALUE;
            } else {
               if (s.equals("srgb")) {
                  return SVGValueConstants.SRGB_VALUE;
               }

               return new StringValue((short)21, s);
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
            String s = value.toLowerCase();
            if (s.equals("auto")) {
               return SVGValueConstants.AUTO_VALUE;
            } else {
               if (s.equals("srgb")) {
                  return SVGValueConstants.SRGB_VALUE;
               }

               return new StringValue((short)21, s);
            }
         default:
            throw this.createInvalidStringTypeDOMException(type);
      }
   }
}
