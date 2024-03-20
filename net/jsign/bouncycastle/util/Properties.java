package net.jsign.bouncycastle.util;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Map;

public class Properties {
   private static final ThreadLocal threadProperties = new ThreadLocal();

   public static boolean isOverrideSet(String var0) {
      try {
         return isSetTrue(getPropertyValue(var0));
      } catch (AccessControlException var2) {
         return false;
      }
   }

   public static String getPropertyValue(final String var0) {
      String var1 = (String)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return Security.getProperty(var0);
         }
      });
      if (var1 != null) {
         return var1;
      } else {
         Map var2 = (Map)threadProperties.get();
         if (var2 != null) {
            String var3 = (String)var2.get(var0);
            if (var3 != null) {
               return var3;
            }
         }

         return (String)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
               return System.getProperty(var0);
            }
         });
      }
   }

   private static boolean isSetTrue(String var0) {
      if (var0 != null && var0.length() == 4) {
         return (var0.charAt(0) == 't' || var0.charAt(0) == 'T') && (var0.charAt(1) == 'r' || var0.charAt(1) == 'R') && (var0.charAt(2) == 'u' || var0.charAt(2) == 'U') && (var0.charAt(3) == 'e' || var0.charAt(3) == 'E');
      } else {
         return false;
      }
   }
}
