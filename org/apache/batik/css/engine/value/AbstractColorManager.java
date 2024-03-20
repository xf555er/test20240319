package org.apache.batik.css.engine.value;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public abstract class AbstractColorManager extends IdentifierManager {
   protected static final StringMap values = new StringMap();
   protected static final StringMap computedValues;

   public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
      if (lu.getLexicalUnitType() == 27) {
         lu = lu.getParameters();
         Value red = this.createColorComponent(lu);
         lu = lu.getNextLexicalUnit().getNextLexicalUnit();
         Value green = this.createColorComponent(lu);
         lu = lu.getNextLexicalUnit().getNextLexicalUnit();
         Value blue = this.createColorComponent(lu);
         return this.createRGBColor(red, green, blue);
      } else {
         return super.createValue(lu, engine);
      }
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      if (value.getPrimitiveType() == 21) {
         String ident = value.getStringValue();
         Value v = (Value)computedValues.get(ident);
         if (v != null) {
            return v;
         } else if (values.get(ident) == null) {
            throw new IllegalStateException("Not a system-color:" + ident);
         } else {
            return engine.getCSSContext().getSystemColor(ident);
         }
      } else {
         return super.computeValue(elt, pseudo, engine, idx, sm, value);
      }
   }

   protected Value createRGBColor(Value r, Value g, Value b) {
      return new RGBColorValue(r, g, b);
   }

   protected Value createColorComponent(LexicalUnit lu) throws DOMException {
      switch (lu.getLexicalUnitType()) {
         case 13:
            return new FloatValue((short)1, (float)lu.getIntegerValue());
         case 14:
            return new FloatValue((short)1, lu.getFloatValue());
         case 23:
            return new FloatValue((short)2, lu.getFloatValue());
         default:
            throw this.createInvalidRGBComponentUnitDOMException(lu.getLexicalUnitType());
      }
   }

   public StringMap getIdentifiers() {
      return values;
   }

   private DOMException createInvalidRGBComponentUnitDOMException(short type) {
      Object[] p = new Object[]{this.getPropertyName(), Integer.valueOf(type)};
      String s = Messages.formatMessage("invalid.rgb.component.unit", p);
      return new DOMException((short)9, s);
   }

   static {
      values.put("aqua", ValueConstants.AQUA_VALUE);
      values.put("black", ValueConstants.BLACK_VALUE);
      values.put("blue", ValueConstants.BLUE_VALUE);
      values.put("fuchsia", ValueConstants.FUCHSIA_VALUE);
      values.put("gray", ValueConstants.GRAY_VALUE);
      values.put("green", ValueConstants.GREEN_VALUE);
      values.put("lime", ValueConstants.LIME_VALUE);
      values.put("maroon", ValueConstants.MAROON_VALUE);
      values.put("navy", ValueConstants.NAVY_VALUE);
      values.put("olive", ValueConstants.OLIVE_VALUE);
      values.put("purple", ValueConstants.PURPLE_VALUE);
      values.put("red", ValueConstants.RED_VALUE);
      values.put("silver", ValueConstants.SILVER_VALUE);
      values.put("teal", ValueConstants.TEAL_VALUE);
      values.put("white", ValueConstants.WHITE_VALUE);
      values.put("yellow", ValueConstants.YELLOW_VALUE);
      values.put("activeborder", ValueConstants.ACTIVEBORDER_VALUE);
      values.put("activecaption", ValueConstants.ACTIVECAPTION_VALUE);
      values.put("appworkspace", ValueConstants.APPWORKSPACE_VALUE);
      values.put("background", ValueConstants.BACKGROUND_VALUE);
      values.put("buttonface", ValueConstants.BUTTONFACE_VALUE);
      values.put("buttonhighlight", ValueConstants.BUTTONHIGHLIGHT_VALUE);
      values.put("buttonshadow", ValueConstants.BUTTONSHADOW_VALUE);
      values.put("buttontext", ValueConstants.BUTTONTEXT_VALUE);
      values.put("captiontext", ValueConstants.CAPTIONTEXT_VALUE);
      values.put("graytext", ValueConstants.GRAYTEXT_VALUE);
      values.put("highlight", ValueConstants.HIGHLIGHT_VALUE);
      values.put("highlighttext", ValueConstants.HIGHLIGHTTEXT_VALUE);
      values.put("inactiveborder", ValueConstants.INACTIVEBORDER_VALUE);
      values.put("inactivecaption", ValueConstants.INACTIVECAPTION_VALUE);
      values.put("inactivecaptiontext", ValueConstants.INACTIVECAPTIONTEXT_VALUE);
      values.put("infobackground", ValueConstants.INFOBACKGROUND_VALUE);
      values.put("infotext", ValueConstants.INFOTEXT_VALUE);
      values.put("menu", ValueConstants.MENU_VALUE);
      values.put("menutext", ValueConstants.MENUTEXT_VALUE);
      values.put("scrollbar", ValueConstants.SCROLLBAR_VALUE);
      values.put("threeddarkshadow", ValueConstants.THREEDDARKSHADOW_VALUE);
      values.put("threedface", ValueConstants.THREEDFACE_VALUE);
      values.put("threedhighlight", ValueConstants.THREEDHIGHLIGHT_VALUE);
      values.put("threedlightshadow", ValueConstants.THREEDLIGHTSHADOW_VALUE);
      values.put("threedshadow", ValueConstants.THREEDSHADOW_VALUE);
      values.put("window", ValueConstants.WINDOW_VALUE);
      values.put("windowframe", ValueConstants.WINDOWFRAME_VALUE);
      values.put("windowtext", ValueConstants.WINDOWTEXT_VALUE);
      computedValues = new StringMap();
      computedValues.put("black", ValueConstants.BLACK_RGB_VALUE);
      computedValues.put("silver", ValueConstants.SILVER_RGB_VALUE);
      computedValues.put("gray", ValueConstants.GRAY_RGB_VALUE);
      computedValues.put("white", ValueConstants.WHITE_RGB_VALUE);
      computedValues.put("maroon", ValueConstants.MAROON_RGB_VALUE);
      computedValues.put("red", ValueConstants.RED_RGB_VALUE);
      computedValues.put("purple", ValueConstants.PURPLE_RGB_VALUE);
      computedValues.put("fuchsia", ValueConstants.FUCHSIA_RGB_VALUE);
      computedValues.put("green", ValueConstants.GREEN_RGB_VALUE);
      computedValues.put("lime", ValueConstants.LIME_RGB_VALUE);
      computedValues.put("olive", ValueConstants.OLIVE_RGB_VALUE);
      computedValues.put("yellow", ValueConstants.YELLOW_RGB_VALUE);
      computedValues.put("navy", ValueConstants.NAVY_RGB_VALUE);
      computedValues.put("blue", ValueConstants.BLUE_RGB_VALUE);
      computedValues.put("teal", ValueConstants.TEAL_RGB_VALUE);
      computedValues.put("aqua", ValueConstants.AQUA_RGB_VALUE);
   }
}
