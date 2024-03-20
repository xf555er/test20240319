package org.apache.batik.util;

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

public class Service {
   static HashMap providerMap = new HashMap();

   public static synchronized Iterator providers(Class cls) {
      String serviceFile = "META-INF/services/" + cls.getName();
      List l = (List)providerMap.get(serviceFile);
      if (l != null) {
         return l.iterator();
      } else {
         List l = new ArrayList();
         providerMap.put(serviceFile, l);
         ClassLoader cl = null;

         try {
            cl = cls.getClassLoader();
         } catch (SecurityException var36) {
         }

         if (cl == null) {
            cl = Service.class.getClassLoader();
         }

         if (cl == null) {
            return l.iterator();
         } else {
            Enumeration e;
            try {
               e = cl.getResources(serviceFile);
            } catch (IOException var35) {
               return l.iterator();
            }

            label293:
            while(e.hasMoreElements()) {
               InputStream is = null;
               Reader r = null;
               BufferedReader br = null;

               try {
                  URL u = (URL)e.nextElement();
                  is = u.openStream();
                  r = new InputStreamReader(is, "UTF-8");
                  br = new BufferedReader(r);
                  String line = br.readLine();

                  while(true) {
                     while(true) {
                        if (line == null) {
                           continue label293;
                        }

                        try {
                           int idx = line.indexOf(35);
                           if (idx != -1) {
                              line = line.substring(0, idx);
                           }

                           line = line.trim();
                           if (line.length() != 0) {
                              Object obj = cl.loadClass(line).getDeclaredConstructor().newInstance();
                              l.add(obj);
                              break;
                           }

                           line = br.readLine();
                        } catch (Exception var37) {
                           break;
                        }
                     }

                     line = br.readLine();
                  }
               } catch (Exception var38) {
               } catch (LinkageError var39) {
               } finally {
                  if (is != null) {
                     try {
                        is.close();
                     } catch (IOException var34) {
                     }

                     is = null;
                  }

                  if (r != null) {
                     try {
                        r.close();
                     } catch (IOException var33) {
                     }

                     r = null;
                  }

                  if (br != null) {
                     try {
                        br.close();
                     } catch (IOException var32) {
                     }

                     br = null;
                  }

               }
            }

            return l.iterator();
         }
      }
   }
}
