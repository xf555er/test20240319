package org.apache.fop.fo.expr;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.properties.ColorProperty;
import org.apache.fop.fo.properties.Property;

public class OCAColorFunction extends FunctionBase {
   public int getRequiredArgsCount() {
      return 1;
   }

   public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
      StringBuffer sb = new StringBuffer();
      sb.append("oca(" + args[0] + ")");
      FOUserAgent ua = pInfo == null ? null : (pInfo.getFO() == null ? null : pInfo.getFO().getUserAgent());
      return ColorProperty.getInstance(ua, sb.toString());
   }
}
