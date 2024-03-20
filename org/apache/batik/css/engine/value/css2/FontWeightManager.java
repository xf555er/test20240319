package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class FontWeightManager extends IdentifierManager {
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
      return 28;
   }

   public String getPropertyName() {
      return "font-weight";
   }

   public Value getDefaultValue() {
      return ValueConstants.NORMAL_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      if (lu.getLexicalUnitType() == 13) {
         int i = lu.getIntegerValue();
         switch (i) {
            case 100:
               return ValueConstants.NUMBER_100;
            case 200:
               return ValueConstants.NUMBER_200;
            case 300:
               return ValueConstants.NUMBER_300;
            case 400:
               return ValueConstants.NUMBER_400;
            case 500:
               return ValueConstants.NUMBER_500;
            case 600:
               return ValueConstants.NUMBER_600;
            case 700:
               return ValueConstants.NUMBER_700;
            case 800:
               return ValueConstants.NUMBER_800;
            case 900:
               return ValueConstants.NUMBER_900;
            default:
               throw this.createInvalidFloatValueDOMException((float)i);
         }
      } else {
         return super.createValue(lu, engine);
      }
   }

   public Value createFloatValue(short type, float floatValue) throws DOMException {
      if (type == 1) {
         int i = (int)floatValue;
         if (floatValue == (float)i) {
            switch (i) {
               case 100:
                  return ValueConstants.NUMBER_100;
               case 200:
                  return ValueConstants.NUMBER_200;
               case 300:
                  return ValueConstants.NUMBER_300;
               case 400:
                  return ValueConstants.NUMBER_400;
               case 500:
                  return ValueConstants.NUMBER_500;
               case 600:
                  return ValueConstants.NUMBER_600;
               case 700:
                  return ValueConstants.NUMBER_700;
               case 800:
                  return ValueConstants.NUMBER_800;
               case 900:
                  return ValueConstants.NUMBER_900;
            }
         }
      }

      throw this.createInvalidFloatValueDOMException(floatValue);
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      CSSContext ctx;
      CSSStylableElement p;
      float fw;
      Value v;
      if (value == ValueConstants.BOLDER_VALUE) {
         sm.putParentRelative(idx, true);
         ctx = engine.getCSSContext();
         p = CSSEngine.getParentCSSStylableElement(elt);
         if (p == null) {
            fw = 400.0F;
         } else {
            v = engine.getComputedStyle(p, pseudo, idx);
            fw = v.getFloatValue();
         }

         return this.createFontWeight(ctx.getBolderFontWeight(fw));
      } else if (value == ValueConstants.LIGHTER_VALUE) {
         sm.putParentRelative(idx, true);
         ctx = engine.getCSSContext();
         p = CSSEngine.getParentCSSStylableElement(elt);
         if (p == null) {
            fw = 400.0F;
         } else {
            v = engine.getComputedStyle(p, pseudo, idx);
            fw = v.getFloatValue();
         }

         return this.createFontWeight(ctx.getLighterFontWeight(fw));
      } else if (value == ValueConstants.NORMAL_VALUE) {
         return ValueConstants.NUMBER_400;
      } else {
         return value == ValueConstants.BOLD_VALUE ? ValueConstants.NUMBER_700 : value;
      }
   }

   protected Value createFontWeight(float f) {
      switch ((int)f) {
         case 100:
            return ValueConstants.NUMBER_100;
         case 200:
            return ValueConstants.NUMBER_200;
         case 300:
            return ValueConstants.NUMBER_300;
         case 400:
            return ValueConstants.NUMBER_400;
         case 500:
            return ValueConstants.NUMBER_500;
         case 600:
            return ValueConstants.NUMBER_600;
         case 700:
            return ValueConstants.NUMBER_700;
         case 800:
            return ValueConstants.NUMBER_800;
         default:
            return ValueConstants.NUMBER_900;
      }
   }

   public StringMap getIdentifiers() {
      return values;
   }

   static {
      values.put("all", ValueConstants.ALL_VALUE);
      values.put("bold", ValueConstants.BOLD_VALUE);
      values.put("bolder", ValueConstants.BOLDER_VALUE);
      values.put("lighter", ValueConstants.LIGHTER_VALUE);
      values.put("normal", ValueConstants.NORMAL_VALUE);
   }
}
