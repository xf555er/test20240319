package org.apache.batik.css.engine.value;

import org.w3c.dom.DOMException;

public class FloatValue extends AbstractValue {
   protected static final String[] UNITS = new String[]{"", "%", "em", "ex", "px", "cm", "mm", "in", "pt", "pc", "deg", "rad", "grad", "ms", "s", "Hz", "kHz", ""};
   protected float floatValue;
   protected short unitType;

   public static String getCssText(short unit, float value) {
      if (unit >= 0 && unit < UNITS.length) {
         String s = String.valueOf(value);
         if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
         }

         return s + UNITS[unit - 1];
      } else {
         throw new DOMException((short)12, "");
      }
   }

   public FloatValue(short unitType, float floatValue) {
      this.unitType = unitType;
      this.floatValue = floatValue;
   }

   public short getPrimitiveType() {
      return this.unitType;
   }

   public float getFloatValue() {
      return this.floatValue;
   }

   public String getCssText() {
      return getCssText(this.unitType, this.floatValue);
   }

   public String toString() {
      return this.getCssText();
   }
}
