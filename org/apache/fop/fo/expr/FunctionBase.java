package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.StringProperty;

public abstract class FunctionBase implements Function {
   public int getOptionalArgsCount() {
      return 0;
   }

   public Property getOptionalArgDefault(int index, PropertyInfo pi) throws PropertyException {
      if (index >= this.getOptionalArgsCount()) {
         PropertyException e = new PropertyException(new IndexOutOfBoundsException("illegal optional argument index"));
         e.setPropertyInfo(pi);
         throw e;
      } else {
         return null;
      }
   }

   public boolean hasVariableArgs() {
      return false;
   }

   public PercentBase getPercentBase() {
      return null;
   }

   protected final Property getPropertyName(PropertyInfo pi) {
      return StringProperty.getInstance(pi.getPropertyMaker().getName());
   }
}
