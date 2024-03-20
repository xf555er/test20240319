package org.apache.batik.css.engine.value.svg12;

import org.apache.batik.css.engine.value.FloatValue;

public class LineHeightValue extends FloatValue {
   protected boolean fontSizeRelative;

   public LineHeightValue(short unitType, float floatValue, boolean fontSizeRelative) {
      super(unitType, floatValue);
      this.fontSizeRelative = fontSizeRelative;
   }

   public boolean getFontSizeRelative() {
      return this.fontSizeRelative;
   }
}
