package org.apache.fop.fo.expr;

import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.properties.Property;

public class InheritedPropFunction extends FunctionBase {
   public int getRequiredArgsCount() {
      return 0;
   }

   public int getOptionalArgsCount() {
      return 1;
   }

   public Property getOptionalArgDefault(int index, PropertyInfo pi) throws PropertyException {
      return index == 0 ? this.getPropertyName(pi) : super.getOptionalArgDefault(index, pi);
   }

   public Property eval(Property[] args, PropertyInfo pInfo) throws PropertyException {
      String propName = args[0].getString();
      if (propName == null) {
         throw new PropertyException("Incorrect parameter to inherited-property-value function");
      } else {
         int propId = FOPropertyMapping.getPropertyId(propName);
         if (propId < 0) {
            throw new PropertyException("Unknown property name used with inherited-property-value function: " + propName);
         } else {
            return pInfo.getPropertyList().getInherited(propId);
         }
      }
   }
}
