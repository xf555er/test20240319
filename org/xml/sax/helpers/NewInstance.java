package org.xml.sax.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class NewInstance {
   // $FF: synthetic field
   static Class class$java$lang$Thread;
   // $FF: synthetic field
   static Class class$org$xml$sax$helpers$NewInstance;

   static Object newInstance(ClassLoader var0, String var1) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
      Class var2;
      if (var0 == null) {
         var2 = Class.forName(var1);
      } else {
         var2 = var0.loadClass(var1);
      }

      return var2.newInstance();
   }

   static ClassLoader getClassLoader() {
      Method var0 = null;

      try {
         var0 = (class$java$lang$Thread == null ? (class$java$lang$Thread = class$("java.lang.Thread")) : class$java$lang$Thread).getMethod("getContextClassLoader", (Class[])null);
      } catch (NoSuchMethodException var5) {
         return (class$org$xml$sax$helpers$NewInstance == null ? (class$org$xml$sax$helpers$NewInstance = class$("org.xml.sax.helpers.NewInstance")) : class$org$xml$sax$helpers$NewInstance).getClassLoader();
      }

      try {
         return (ClassLoader)var0.invoke(Thread.currentThread(), (Object[])null);
      } catch (IllegalAccessException var3) {
         throw new UnknownError(var3.getMessage());
      } catch (InvocationTargetException var4) {
         throw new UnknownError(var4.getMessage());
      }
   }

   // $FF: synthetic method
   static Class class$(String var0) {
      try {
         return Class.forName(var0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }
}
