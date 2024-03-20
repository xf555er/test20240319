package org.apache.fop.fo.expr;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.Property;

class CMYKColorFunction extends FunctionBase {
   public int getRequiredArgsCount() {
      return 4;
   }

   public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
      StringBuffer sb = new StringBuffer();
      sb.append("cmyk(" + args[0] + "," + args[1] + "," + args[2] + "," + args[3] + ")");
      FOUserAgent ua = pInfo == null ? null : (pInfo.getFO() == null ? null : pInfo.getFO().getUserAgent());
      return ColorProperty.getInstance(ua, sb.toString());
   }
}
