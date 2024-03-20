package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.pagination.ColorProfile;
import org.apache.fop.fo.pagination.Declarations;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.Property;

class RGBNamedColorFunction extends FunctionBase {
   public int getRequiredArgsCount() {
      return 5;
   }

   public PercentBase getPercentBase() {
      return new RGBNamedPercentBase();
   }

   public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
      String colorProfileName = args[3].getString();
      String colorName = args[4].getString();
      Declarations decls = pInfo.getFO() != null ? pInfo.getFO().getRoot().getDeclarations() : null;
      ColorProfile cp = null;
      if (decls != null) {
         cp = decls.getColorProfile(colorProfileName);
      }

      if (cp == null) {
         PropertyException pe = new PropertyException("The " + colorProfileName + " color profile was not declared");
         pe.setPropertyInfo(pInfo);
         throw pe;
      } else {
         float red = 0.0F;
         float green = 0.0F;
         float blue = 0.0F;
         red = args[0].getNumber().floatValue();
         green = args[1].getNumber().floatValue();
         blue = args[2].getNumber().floatValue();
         if (!(red < 0.0F) && !(red > 255.0F) && !(green < 0.0F) && !(green > 255.0F) && !(blue < 0.0F) && !(blue > 255.0F)) {
            StringBuffer sb = new StringBuffer();
            sb.append("fop-rgb-named-color(");
            sb.append(red / 255.0F);
            sb.append(',').append(green / 255.0F);
            sb.append(',').append(blue / 255.0F);
            sb.append(',').append(colorProfileName);
            sb.append(',').append(cp.getSrc());
            sb.append(", '").append(colorName).append('\'');
            sb.append(")");
            return ColorProperty.getInstance(pInfo.getUserAgent(), sb.toString());
         } else {
            throw new PropertyException("sRGB color values out of range. Arguments to rgb-named-color() must be [0..255] or [0%..100%]");
         }
      }
   }

   private static final class RGBNamedPercentBase implements PercentBase {
      private RGBNamedPercentBase() {
      }

      public int getBaseLength(PercentBaseContext context) throws PropertyException {
         return 0;
      }

      public double getBaseValue() {
         return 255.0;
      }

      public int getDimension() {
         return 0;
      }

      // $FF: synthetic method
      RGBNamedPercentBase(Object x0) {
         this();
      }
   }
}
