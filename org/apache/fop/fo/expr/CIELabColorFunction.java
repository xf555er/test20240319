package org.apache.fop.fo.expr;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.Property;

class CIELabColorFunction extends FunctionBase {
   public int getRequiredArgsCount() {
      return 6;
   }

   public PercentBase getPercentBase() {
      return new CIELabPercentBase();
   }

   public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
      float red = args[0].getNumber().floatValue();
      float green = args[1].getNumber().floatValue();
      float blue = args[2].getNumber().floatValue();
      if (!(red < 0.0F) && !(red > 255.0F) && !(green < 0.0F) && !(green > 255.0F) && !(blue < 0.0F) && !(blue > 255.0F)) {
         float l = args[3].getNumber().floatValue();
         float a = args[4].getNumber().floatValue();
         float b = args[5].getNumber().floatValue();
         if (!(l < 0.0F) && !(l > 100.0F)) {
            if (!(a < -127.0F) && !(a > 127.0F) && !(b < -127.0F) && !(b > 127.0F)) {
               StringBuffer sb = new StringBuffer();
               sb.append("cie-lab-color(" + red + "," + green + "," + blue + "," + l + "," + a + "," + b + ")");
               FOUserAgent ua = pInfo == null ? null : (pInfo.getFO() == null ? null : pInfo.getFO().getUserAgent());
               return ColorProperty.getInstance(ua, sb.toString());
            } else {
               throw new PropertyException("a* and b* values out of range. Valid range: [-127..+127]");
            }
         } else {
            throw new PropertyException("L* value out of range. Valid range: [0..100]");
         }
      } else {
         throw new PropertyException("sRGB color values out of range. Arguments to cie-lab-color() must be [0..255] or [0%..100%]");
      }
   }

   private static class CIELabPercentBase implements PercentBase {
      private CIELabPercentBase() {
      }

      public int getDimension() {
         return 0;
      }

      public double getBaseValue() {
         return 1.0;
      }

      public int getBaseLength(PercentBaseContext context) {
         return 0;
      }

      // $FF: synthetic method
      CIELabPercentBase(Object x0) {
         this();
      }
   }
}
