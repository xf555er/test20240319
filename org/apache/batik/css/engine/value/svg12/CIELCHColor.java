package org.apache.batik.css.engine.value.svg12;

public class CIELCHColor extends AbstractCIEColor {
   public static final String CIE_LCH_COLOR_FUNCTION = "cielch";

   public CIELCHColor(float l, float c, float h, float[] whitepoint) {
      super(new float[]{l, c, h}, whitepoint);
   }

   public CIELCHColor(float l, float c, float h) {
      this(l, c, h, (float[])null);
   }

   public String getFunctionName() {
      return "cielch";
   }
}
