package org.apache.batik.css.engine.value;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public abstract class RectManager extends LengthManager {
   protected int orientation;

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 38:
            break;
         case 41:
            if (lu.getFunctionName().equalsIgnoreCase("rect")) {
               break;
            }
         default:
            throw this.createMalformedRectDOMException();
      }

      lu = lu.getParameters();
      Value top = this.createRectComponent(lu);
      lu = lu.getNextLexicalUnit();
      if (lu != null && lu.getLexicalUnitType() == 0) {
         lu = lu.getNextLexicalUnit();
         Value right = this.createRectComponent(lu);
         lu = lu.getNextLexicalUnit();
         if (lu != null && lu.getLexicalUnitType() == 0) {
            lu = lu.getNextLexicalUnit();
            Value bottom = this.createRectComponent(lu);
            lu = lu.getNextLexicalUnit();
            if (lu != null && lu.getLexicalUnitType() == 0) {
               lu = lu.getNextLexicalUnit();
               Value left = this.createRectComponent(lu);
               return new RectValue(top, right, bottom, left);
            } else {
               throw this.createMalformedRectDOMException();
            }
         } else {
            throw this.createMalformedRectDOMException();
         }
      } else {
         throw this.createMalformedRectDOMException();
      }
   }

   private Value createRectComponent(LexicalUnit lu) throws DOMException {
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
         case 35:
            if (lu.getStringValue().equalsIgnoreCase("auto")) {
               return ValueConstants.AUTO_VALUE;
            }
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         default:
            throw this.createMalformedRectDOMException();
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value.getCssValueType() != 1) {
         return value;
      } else if (value.getPrimitiveType() != 24) {
         return value;
      } else {
         RectValue rect = (RectValue)value;
         this.orientation = 1;
         Value top = super.computeValue(elt, pseudo, engine, idx, sm, rect.getTop());
         Value bottom = super.computeValue(elt, pseudo, engine, idx, sm, rect.getBottom());
         this.orientation = 0;
         Value left = super.computeValue(elt, pseudo, engine, idx, sm, rect.getLeft());
         Value right = super.computeValue(elt, pseudo, engine, idx, sm, rect.getRight());
         return (Value)(top == rect.getTop() && right == rect.getRight() && bottom == rect.getBottom() && left == rect.getLeft() ? value : new RectValue(top, right, bottom, left));
      }
   }

   protected int getOrientation() {
      return this.orientation;
   }

   private DOMException createMalformedRectDOMException() {
      Object[] p = new Object[]{this.getPropertyName()};
      String s = Messages.formatMessage("malformed.rect", p);
      return new DOMException((short)12, s);
   }
}
