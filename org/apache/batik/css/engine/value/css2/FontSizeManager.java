package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.LengthManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class FontSizeManager extends LengthManager {
   protected static final StringMap values = new StringMap();

   public StringMap getIdentifiers() {
      return values;
   }

   public boolean isInheritedProperty() {
      return true;
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return true;
   }

   public String getPropertyName() {
      return "font-size";
   }

   public int getPropertyType() {
      return 39;
   }

   public Value getDefaultValue() {
      return ValueConstants.MEDIUM_VALUE;
   }

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 12:
            return ValueConstants.INHERIT_VALUE;
         case 35:
            String s = lu.getStringValue().toLowerCase().intern();
            Object v = values.get(s);
            if (v == null) {
               throw this.createInvalidIdentifierDOMException(s);
            }

            return (Value)v;
         default:
            return super.createValue(lu, engine);
      }
   }

   public Value createStringValue(short type, String value, CSSEngine engine) throws DOMException {
      if (type != 21) {
         throw this.createInvalidStringTypeDOMException(type);
      } else {
         Object v = values.get(value.toLowerCase().intern());
         if (v == null) {
            throw this.createInvalidIdentifierDOMException(value);
         } else {
            return (Value)v;
         }
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      float scale = 1.0F;
      boolean doParentRelative = false;
      CSSContext ctx;
      float fs;
      switch (value.getPrimitiveType()) {
         case 1:
         case 5:
            return value;
         case 2:
            doParentRelative = true;
            scale = value.getFloatValue() * 0.01F;
            break;
         case 3:
            doParentRelative = true;
            scale = value.getFloatValue();
            break;
         case 4:
            doParentRelative = true;
            scale = value.getFloatValue() * 0.5F;
            break;
         case 6:
            ctx = engine.getCSSContext();
            fs = value.getFloatValue();
            return new FloatValue((short)1, fs * 10.0F / ctx.getPixelUnitToMillimeter());
         case 7:
            ctx = engine.getCSSContext();
            fs = value.getFloatValue();
            return new FloatValue((short)1, fs / ctx.getPixelUnitToMillimeter());
         case 8:
            ctx = engine.getCSSContext();
            fs = value.getFloatValue();
            return new FloatValue((short)1, fs * 25.4F / ctx.getPixelUnitToMillimeter());
         case 9:
            ctx = engine.getCSSContext();
            fs = value.getFloatValue();
            return new FloatValue((short)1, fs * 25.4F / (72.0F * ctx.getPixelUnitToMillimeter()));
         case 10:
            ctx = engine.getCSSContext();
            fs = value.getFloatValue();
            return new FloatValue((short)1, fs * 25.4F / (6.0F * ctx.getPixelUnitToMillimeter()));
      }

      if (value == ValueConstants.LARGER_VALUE) {
         doParentRelative = true;
         scale = 1.2F;
      } else if (value == ValueConstants.SMALLER_VALUE) {
         doParentRelative = true;
         scale = 0.8333333F;
      }

      if (doParentRelative) {
         sm.putParentRelative(idx, true);
         CSSStylableElement p = CSSEngine.getParentCSSStylableElement(elt);
         if (p == null) {
            CSSContext ctx = engine.getCSSContext();
            fs = ctx.getMediumFontSize();
         } else {
            fs = engine.getComputedStyle(p, (String)null, idx).getFloatValue();
         }

         return new FloatValue((short)1, fs * scale);
      } else {
         ctx = engine.getCSSContext();
         fs = ctx.getMediumFontSize();
         String s = value.getStringValue();
         switch (s.charAt(0)) {
            case 'l':
               fs = (float)((double)fs * 1.2);
            case 'm':
               break;
            case 's':
               fs = (float)((double)fs / 1.2);
               break;
            default:
               switch (s.charAt(1)) {
                  case 'x':
                     switch (s.charAt(3)) {
                        case 's':
                           fs = (float)((double)fs / 1.2 / 1.2 / 1.2);
                           return new FloatValue((short)1, fs);
                        default:
                           fs = (float)((double)fs * 1.2 * 1.2 * 1.2);
                           return new FloatValue((short)1, fs);
                     }
                  default:
                     switch (s.charAt(2)) {
                        case 's':
                           fs = (float)((double)fs / 1.2 / 1.2);
                           break;
                        default:
                           fs = (float)((double)fs * 1.2 * 1.2);
                     }
               }
         }

         return new FloatValue((short)1, fs);
      }
   }

   protected int getOrientation() {
      return 1;
   }

   static {
      values.put("all", ValueConstants.ALL_VALUE);
      values.put("large", ValueConstants.LARGE_VALUE);
      values.put("larger", ValueConstants.LARGER_VALUE);
      values.put("medium", ValueConstants.MEDIUM_VALUE);
      values.put("small", ValueConstants.SMALL_VALUE);
      values.put("smaller", ValueConstants.SMALLER_VALUE);
      values.put("x-large", ValueConstants.X_LARGE_VALUE);
      values.put("x-small", ValueConstants.X_SMALL_VALUE);
      values.put("xx-large", ValueConstants.XX_LARGE_VALUE);
      values.put("xx-small", ValueConstants.XX_SMALL_VALUE);
   }
}
