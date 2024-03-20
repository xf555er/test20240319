package org.apache.xmlgraphics.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public final class Service {
   static Map classMap = new HashMap();
   static Map instanceMap = new HashMap();

   private Service() {
   }

   public static synchronized Iterator providers(Class cls) {
      String serviceFile = getServiceFilename(cls);
      List l = (List)instanceMap.get(serviceFile);
      if (l != null) {
         return l.iterator();
      } else {
         List l = new ArrayList();
         instanceMap.put(serviceFile, l);
         ClassLoader cl = getClassLoader(cls);
         if (cl != null) {
            List names = getProviderNames(cls, cl);
            Iterator var5 = names.iterator();

            while(var5.hasNext()) {
               String name = (String)var5.next();

               try {
                  Object obj = cl.loadClass(name).getDeclaredConstructor().newInstance();
                  l.add(obj);
               } catch (Exception var8) {
               }
            }
         }

         return l.iterator();
      }
   }

   public static synchronized Iterator providerNames(Class cls) {
      String serviceFile = getServiceFilename(cls);
      List l = (List)classMap.get(serviceFile);
      if (l != null) {
         return l.iterator();
      } else {
         List l = new ArrayList();
         classMap.put(serviceFile, l);
         l.addAll(getProviderNames(cls));
         return l.iterator();
      }
   }

   /** @deprecated */
   public static Iterator providers(Class cls, boolean returnInstances) {
      return returnInstances ? providers(cls) : providerNames(cls);
   }

   private static List getProviderNames(Class cls) {
      return getProviderNames(cls, getClassLoader(cls));
   }

   private static List getProviderNames(Class cls, ClassLoader cl) {
      List l = new ArrayList();
      if (cl == null) {
         return l;
      } else {
         Enumeration e;
         try {
            e = cl.getResources(getServiceFilename(cls));
         } catch (IOException var14) {
            return l;
         }

         while(e.hasMoreElements()) {
            try {
               URL u = (URL)e.nextElement();
               InputStream is = u.openStream();
               Reader r = new InputStreamReader(is, "UTF-8");
               BufferedReader br = new BufferedReader(r);

               try {
                  for(String line = br.readLine(); line != null; line = br.readLine()) {
                     int idx = line.indexOf(35);
                     if (idx != -1) {
                        line = line.substring(0, idx);
                     }

                     line = line.trim();
                     if (line.length() != 0) {
                        l.add(line);
                     }
                  }
               } finally {
                  IOUtils.closeQuietly((Reader)br);
                  IOUtils.closeQuietly(is);
               }
            } catch (Exception var16) {
            }
         }

         return l;
      }
   }

   private static ClassLoader getClassLoader(Class cls) {
      ClassLoader cl = null;

      try {
         cl = cls.getClassLoader();
      } catch (SecurityException var3) {
      }

      if (cl == null) {
         cl = Service.class.getClassLoader();
      }

      if (cl == null) {
         cl = ClassLoader.getSystemClassLoader();
      }

      return cl;
   }

   private static String getServiceFilename(Class cls) {
      return "META-INF/services/" + cls.getName();
   }
}
