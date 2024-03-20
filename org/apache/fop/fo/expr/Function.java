package org.apache.fop.fo.expr;

import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.properties.Property;

public interface Function {
   int getRequiredArgsCount();

   int getOptionalArgsCount();

   Property getOptionalArgDefault(int var1, PropertyInfo var2) throws PropertyException;

   boolean hasVariableArgs();

   PercentBase getPercentBase();

   Property eval(Property[] var1, PropertyInfo var2) throws PropertyException;
}
