package org.apache.batik.css.engine.value.svg12;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.Value;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class LineHeightManager extends LengthManager {
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
      return 43;
   }

   public String getPropertyName() {
      return "line-height";
   }

   public Value getDefaultValue() {
      return SVG12ValueConstants.NORMAL_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return SVG12ValueConstants.INHERIT_VALUE;
         case 35:
            String s = lu.getStringValue().toLowerCase();
            if ("normal".equals(s)) {
               return SVG12ValueConstants.NORMAL_VALUE;
            }

            throw this.createInvalidIdentifierDOMException(lu.getStringValue());
         default:
            return super.createValue(lu, engine);
      }
   }

   protected int getOrientation() {
      return 1;
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value.getCssValueType() != 1) {
         return value;
      } else {
         switch (value.getPrimitiveType()) {
            case 1:
               return new LineHeightValue((short)1, value.getFloatValue(), true);
            case 2:
               float v = value.getFloatValue();
               int fsidx = engine.getFontSizeIndex();
               float fs = engine.getComputedStyle(elt, pseudo, fsidx).getFloatValue();
               return new FloatValue((short)1, v * fs * 0.01F);
            default:
               return super.computeValue(elt, pseudo, engine, idx, sm, value);
         }
      }
   }
}
