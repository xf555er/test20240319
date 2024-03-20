package org.apache.batik.css.engine.value;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public abstract class LengthManager extends AbstractValueManager {
   static final double SQRT2 = Math.sqrt(2.0);
   protected static final int HORIZONTAL_ORIENTATION = 0;
   protected static final int VERTICAL_ORIENTATION = 1;
   protected static final int BOTH_ORIENTATION = 2;

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 13:
            return new FloatValue((short)1, (float)lu.getIntegerValue());
         case 14:
            return new FloatValue((short)1, lu.getFloatValue());
         case 15:
            return new FloatValue((short)3, lu.getFloatValue());
         case 16:
            return new FloatValue((short)4, lu.getFloatValue());
         case 17:
            return new FloatValue((short)5, lu.getFloatValue());
         case 18:
            return new FloatValue((short)8, lu.getFloatValue());
         case 19:
            return new FloatValue((short)6, lu.getFloatValue());
         case 20:
            return new FloatValue((short)7, lu.getFloatValue());
         case 21:
            return new FloatValue((short)9, lu.getFloatValue());
         case 22:
            return new FloatValue((short)10, lu.getFloatValue());
         case 23:
            return new FloatValue((short)2, lu.getFloatValue());
         default:
            throw this.createInvalidLexicalUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public Value createFloatValue(short type, float floatValue) throws DOMException {
      switch (type) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
            return new FloatValue(type, floatValue);
         default:
            throw this.createInvalidFloatTypeDOMException(type);
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value.getCssValueType() != 1) {
         return value;
      } else {
         CSSContext ctx;
         float v;
         int fsidx;
         float fs;
         switch (value.getPrimitiveType()) {
            case 1:
            case 5:
               return value;
            case 2:
               ctx = engine.getCSSContext();
               switch (this.getOrientation()) {
                  case 0:
                     sm.putBlockWidthRelative(idx, true);
                     fs = value.getFloatValue() * ctx.getBlockWidth(elt) / 100.0F;
                     break;
                  case 1:
                     sm.putBlockHeightRelative(idx, true);
                     fs = value.getFloatValue() * ctx.getBlockHeight(elt) / 100.0F;
                     break;
                  default:
                     sm.putBlockWidthRelative(idx, true);
                     sm.putBlockHeightRelative(idx, true);
                     double w = (double)ctx.getBlockWidth(elt);
                     double h = (double)ctx.getBlockHeight(elt);
                     fs = (float)((double)value.getFloatValue() * (Math.sqrt(w * w + h * h) / SQRT2) / 100.0);
               }

               return new FloatValue((short)1, fs);
            case 3:
               sm.putFontSizeRelative(idx, true);
               v = value.getFloatValue();
               fsidx = engine.getFontSizeIndex();
               fs = engine.getComputedStyle(elt, pseudo, fsidx).getFloatValue();
               return new FloatValue((short)1, v * fs);
            case 4:
               sm.putFontSizeRelative(idx, true);
               v = value.getFloatValue();
               fsidx = engine.getFontSizeIndex();
               fs = engine.getComputedStyle(elt, pseudo, fsidx).getFloatValue();
               return new FloatValue((short)1, v * fs * 0.5F);
            case 6:
               ctx = engine.getCSSContext();
               v = value.getFloatValue();
               return new FloatValue((short)1, v * 10.0F / ctx.getPixelUnitToMillimeter());
            case 7:
               ctx = engine.getCSSContext();
               v = value.getFloatValue();
               return new FloatValue((short)1, v / ctx.getPixelUnitToMillimeter());
            case 8:
               ctx = engine.getCSSContext();
               v = value.getFloatValue();
               return new FloatValue((short)1, v * 25.4F / ctx.getPixelUnitToMillimeter());
            case 9:
               ctx = engine.getCSSContext();
               v = value.getFloatValue();
               return new FloatValue((short)1, v * 25.4F / (72.0F * ctx.getPixelUnitToMillimeter()));
            case 10:
               ctx = engine.getCSSContext();
               v = value.getFloatValue();
               return new FloatValue((short)1, v * 25.4F / (6.0F * ctx.getPixelUnitToMillimeter()));
            default:
               return value;
         }
      }
   }

   protected abstract int getOrientation();
}
