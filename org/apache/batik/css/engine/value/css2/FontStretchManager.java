package org.apache.batik.css.engine.value.css2;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;

public class FontStretchManager extends IdentifierManager {
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
      return 15;
   }

   public String getPropertyName() {
      return "font-stretch";
   }

   public Value getDefaultValue() {
      return ValueConstants.NORMAL_VALUE;
   }

   public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
      CSSStylableElement p;
      Value v;
      if (value == ValueConstants.NARROWER_VALUE) {
         sm.putParentRelative(idx, true);
         p = CSSEngine.getParentCSSStylableElement(elt);
         if (p == null) {
            return ValueConstants.SEMI_CONDENSED_VALUE;
         } else {
            v = engine.getComputedStyle(p, pseudo, idx);
            if (v == ValueConstants.NORMAL_VALUE) {
               return ValueConstants.SEMI_CONDENSED_VALUE;
            } else if (v == ValueConstants.CONDENSED_VALUE) {
               return ValueConstants.EXTRA_CONDENSED_VALUE;
            } else if (v == ValueConstants.EXPANDED_VALUE) {
               return ValueConstants.SEMI_EXPANDED_VALUE;
            } else if (v == ValueConstants.SEMI_EXPANDED_VALUE) {
               return ValueConstants.NORMAL_VALUE;
            } else if (v == ValueConstants.SEMI_CONDENSED_VALUE) {
               return ValueConstants.CONDENSED_VALUE;
            } else if (v == ValueConstants.EXTRA_CONDENSED_VALUE) {
               return ValueConstants.ULTRA_CONDENSED_VALUE;
            } else if (v == ValueConstants.EXTRA_EXPANDED_VALUE) {
               return ValueConstants.EXPANDED_VALUE;
            } else {
               return v == ValueConstants.ULTRA_CONDENSED_VALUE ? ValueConstants.ULTRA_CONDENSED_VALUE : ValueConstants.EXTRA_EXPANDED_VALUE;
            }
         }
      } else if (value == ValueConstants.WIDER_VALUE) {
         sm.putParentRelative(idx, true);
         p = CSSEngine.getParentCSSStylableElement(elt);
         if (p == null) {
            return ValueConstants.SEMI_CONDENSED_VALUE;
         } else {
            v = engine.getComputedStyle(p, pseudo, idx);
            if (v == ValueConstants.NORMAL_VALUE) {
               return ValueConstants.SEMI_EXPANDED_VALUE;
            } else if (v == ValueConstants.CONDENSED_VALUE) {
               return ValueConstants.SEMI_CONDENSED_VALUE;
            } else if (v == ValueConstants.EXPANDED_VALUE) {
               return ValueConstants.EXTRA_EXPANDED_VALUE;
            } else if (v == ValueConstants.SEMI_EXPANDED_VALUE) {
               return ValueConstants.EXPANDED_VALUE;
            } else if (v == ValueConstants.SEMI_CONDENSED_VALUE) {
               return ValueConstants.NORMAL_VALUE;
            } else if (v == ValueConstants.EXTRA_CONDENSED_VALUE) {
               return ValueConstants.CONDENSED_VALUE;
            } else if (v == ValueConstants.EXTRA_EXPANDED_VALUE) {
               return ValueConstants.ULTRA_EXPANDED_VALUE;
            } else {
               return v == ValueConstants.ULTRA_CONDENSED_VALUE ? ValueConstants.EXTRA_CONDENSED_VALUE : ValueConstants.ULTRA_EXPANDED_VALUE;
            }
         }
      } else {
         return value;
      }
   }

   public StringMap getIdentifiers() {
      return values;
   }

   static {
      values.put("all", ValueConstants.ALL_VALUE);
      values.put("condensed", ValueConstants.CONDENSED_VALUE);
      values.put("expanded", ValueConstants.EXPANDED_VALUE);
      values.put("extra-condensed", ValueConstants.EXTRA_CONDENSED_VALUE);
      values.put("extra-expanded", ValueConstants.EXTRA_EXPANDED_VALUE);
      values.put("narrower", ValueConstants.NARROWER_VALUE);
      values.put("normal", ValueConstants.NORMAL_VALUE);
      values.put("semi-condensed", ValueConstants.SEMI_CONDENSED_VALUE);
      values.put("semi-expanded", ValueConstants.SEMI_EXPANDED_VALUE);
      values.put("ultra-condensed", ValueConstants.ULTRA_CONDENSED_VALUE);
      values.put("ultra-expanded", ValueConstants.ULTRA_EXPANDED_VALUE);
      values.put("wider", ValueConstants.WIDER_VALUE);
   }
}
