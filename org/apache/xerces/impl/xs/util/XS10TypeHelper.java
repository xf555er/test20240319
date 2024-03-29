package org.apache.xerces.impl.xs.util;

import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;

public class XS10TypeHelper {
   private XS10TypeHelper() {
   }

   public static String getSchemaTypeName(XSTypeDefinition var0) {
      String var1 = "";
      if (var0 instanceof XSSimpleTypeDefinition) {
         var1 = ((XSSimpleTypeDecl)var0).getTypeName();
      } else {
         var1 = ((XSComplexTypeDecl)var0).getTypeName();
      }

      return var1;
   }
}
