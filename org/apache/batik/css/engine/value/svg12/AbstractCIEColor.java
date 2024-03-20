package org.apache.batik.css.engine.value.svg12;

import org.apache.batik.css.engine.value.AbstractValue;
import org.apache.xmlgraphics.java2d.color.ColorSpaces;

public abstract class AbstractCIEColor extends AbstractValue {
   protected float[] values = new float[3];
   protected float[] whitepoint = ColorSpaces.getCIELabColorSpaceD50().getWhitePoint();

   protected AbstractCIEColor(float[] components, float[] whitepoint) {
      System.arraycopy(components, 0, this.values, 0, this.values.length);
      if (whitepoint != null) {
         System.arraycopy(whitepoint, 0, this.whitepoint, 0, this.whitepoint.length);
      }

   }

   public float[] getColorValues() {
      float[] copy = new float[3];
      System.arraycopy(this.values, 0, copy, 0, copy.length);
      return copy;
   }

   public float[] getWhitePoint() {
      float[] copy = new float[3];
      System.arraycopy(this.whitepoint, 0, copy, 0, copy.length);
      return copy;
   }

   public abstract String getFunctionName();

   public short getCssValueType() {
      return 3;
   }

   public String getCssText() {
      StringBuffer sb = new StringBuffer(this.getFunctionName());
      sb.append('(');
      sb.append(this.values[0]);
      sb.append(", ");
      sb.append(this.values[1]);
      sb.append(", ");
      sb.append(this.values[2]);
      sb.append(')');
      return sb.toString();
   }

   public String toString() {
      return this.getCssText();
   }
}
