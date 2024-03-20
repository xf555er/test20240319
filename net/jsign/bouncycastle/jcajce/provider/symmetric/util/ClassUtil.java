package net.jsign.bouncycastle.jcajce.provider.symmetric.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class ClassUtil {
   public static Class loadClass(Class var0, final String var1) {
      try {
         ClassLoader var2 = var0.getClassLoader();
         return var2 != null ? var2.loadClass(var1) : (Class)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
               try {
                  return Class.forName(var1);
               } catch (Exception var2) {
                  return null;
               }
            }
         });
      } catch (ClassNotFoundException var3) {
         return null;
      }
   }
}
