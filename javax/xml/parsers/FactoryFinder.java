package javax.xml.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

class FactoryFinder {
   private static final boolean debug = false;
   // $FF: synthetic field
   static Class class$java$lang$Thread;
   // $FF: synthetic field
   static Class class$javax$xml$parsers$FactoryFinder;

   private static void debugPrintln(String var0) {
   }

   private static ClassLoader findClassLoader() throws ConfigurationError {
      Method var0 = null;

      try {
         var0 = (class$java$lang$Thread == null ? (class$java$lang$Thread = class$("java.lang.Thread")) : class$java$lang$Thread).getMethod("getContextClassLoader", (Class[])null);
      } catch (NoSuchMethodException var5) {
         debugPrintln("assuming JDK 1.1");
         return (class$javax$xml$parsers$FactoryFinder == null ? (class$javax$xml$parsers$FactoryFinder = class$("javax.xml.parsers.FactoryFinder")) : class$javax$xml$parsers$FactoryFinder).getClassLoader();
      }

      try {
         return (ClassLoader)var0.invoke(Thread.currentThread(), (Object[])null);
      } catch (IllegalAccessException var3) {
         throw new ConfigurationError("Unexpected IllegalAccessException", var3);
      } catch (InvocationTargetException var4) {
         throw new ConfigurationError("Unexpected InvocationTargetException", var4);
      }
   }

   private static Object newInstance(String var0, ClassLoader var1) throws ConfigurationError {
      try {
         Class var2;
         if (var1 == null) {
            var2 = Class.forName(var0);
         } else {
            var2 = var1.loadClass(var0);
         }

         return var2.newInstance();
      } catch (ClassNotFoundException var4) {
         throw new ConfigurationError("Provider " + var0 + " not found", var4);
      } catch (Exception var5) {
         throw new ConfigurationError("Provider " + var0 + " could not be instantiated: " + var5, var5);
      }
   }

   static Object find(String var0, String var1) throws ConfigurationError {
      debugPrintln("debug is on");
      ClassLoader var2 = findClassLoader();

      String var3;
      try {
         var3 = System.getProperty(var0);
         if (var3 != null) {
            debugPrintln("found system property " + var3);
            return newInstance(var3, var2);
         }
      } catch (SecurityException var11) {
      }

      String var4;
      try {
         var3 = System.getProperty("java.home");
         var4 = var3 + File.separator + "lib" + File.separator + "jaxp.properties";
         File var5 = new File(var4);
         if (var5.exists()) {
            Properties var14 = new Properties();
            var14.load(new FileInputStream(var5));
            String var7 = var14.getProperty(var0);
            debugPrintln("found java.home property " + var7);
            return newInstance(var7, var2);
         }
      } catch (Exception var10) {
      }

      var3 = "META-INF/services/" + var0;

      try {
         var4 = null;
         InputStream var12;
         if (var2 == null) {
            var12 = ClassLoader.getSystemResourceAsStream(var3);
         } else {
            var12 = var2.getResourceAsStream(var3);
         }

         if (var12 != null) {
            debugPrintln("found " + var3);

            BufferedReader var13;
            try {
               var13 = new BufferedReader(new InputStreamReader(var12, "UTF-8"));
            } catch (UnsupportedEncodingException var8) {
               var13 = new BufferedReader(new InputStreamReader(var12));
            }

            String var6 = var13.readLine();
            var13.close();
            if (var6 != null && !"".equals(var6)) {
               debugPrintln("loaded from services: " + var6);
               return newInstance(var6, var2);
            }
         }
      } catch (Exception var9) {
      }

      if (var1 == null) {
         throw new ConfigurationError("Provider for " + var0 + " cannot be found", (Exception)null);
      } else {
         debugPrintln("loaded from fallback value: " + var1);
         return newInstance(var1, var2);
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

   static class ConfigurationError extends Error {
      private Exception exception;

      ConfigurationError(String var1, Exception var2) {
         super(var1);
         this.exception = var2;
      }

      Exception getException() {
         return this.exception;
      }
   }
}
