package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public abstract class GlyphOrientationManager extends AbstractValueManager {
   public boolean isInheritedProperty() {
      return true;
   }

   public boolean isAnimatableProperty() {
      return false;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public int getPropertyType() {
      return 5;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVGValueConstants.INHERIT_VALUE;
         case 13:
            int n = lu.getIntegerValue();
            return new FloatValue((short)11, (float)n);
         case 14:
            float n = lu.getFloatValue();
            return new FloatValue((short)11, n);
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
         case 28:
            return new FloatValue((short)11, lu.getFloatValue());
         case 29:
            return new FloatValue((short)13, lu.getFloatValue());
         case 30:
            return new FloatValue((short)12, lu.getFloatValue());
      }
   }

   public Value createFloatValue(short type, float floatValue) throws DOMException {
      switch (type) {
         case 11:
         case 12:
         case 13:
            return new FloatValue(type, floatValue);
         default:
            throw this.createInvalidFloatValueDOMException(floatValue);
      }
   }
}
