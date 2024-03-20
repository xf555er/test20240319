package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class GlyphOrientationVerticalManager extends GlyphOrientationManager {
   public String getPropertyName() {
      return "glyph-orientation-vertical";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.AUTO_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      if (lu.getLexicalUnitType() == 35) {
         if (lu.getStringValue().equalsIgnoreCase("auto")) {
            return SVGValueConstants.AUTO_VALUE;
         } else {
            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         }
      } else {
         return super.createValue(lu, engine);
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else if (value.equalsIgnoreCase("auto")) {
         return SVGValueConstants.AUTO_VALUE;
      } else {
         throw this.createInvalidIdentifierDOMException(value);
      }
   }
}
