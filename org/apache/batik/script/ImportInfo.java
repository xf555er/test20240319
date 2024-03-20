package org.apache.batik.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImportInfo {
   static final String defaultFile = "META-INF/imports/script.txt";
   static String importFile = "META-INF/imports/script.txt";
   static ImportInfo defaultImports;
   protected Set classes = new HashSet();
   protected Set packages = new HashSet();
   static final String classStr = "class";
   static final String packageStr = "package";

   public static ImportInfo getImports() {
      if (defaultImports == null) {
         defaultImports = readImports();
      }

      return defaultImports;
   }

   static ImportInfo readImports() {
      ImportInfo ret = new ImportInfo();
      ClassLoader cl = ImportInfo.class.getClassLoader();
      if (cl == null) {
         return ret;
      } else {
         Enumeration e;
         try {
            e = cl.getResources(importFile);
         } catch (IOException var5) {
            return ret;
         }

         while(e.hasMoreElements()) {
            try {
               URL url = (URL)e.nextElement();
               ret.addImports(url);
            } catch (Exception var4) {
            }
         }

         return ret;
      }
   }

   public Iterator getClasses() {
      return Collections.unmodifiableSet(this.classes).iterator();
   }

   public Iterator getPackages() {
      return Collections.unmodifiableSet(this.packages).iterator();
   }

   public void addClass(String cls) {
      this.classes.add(cls);
   }

   public void addPackage(String pkg) {
      this.packages.add(pkg);
   }

   public boolean removeClass(String cls) {
      return this.classes.remove(cls);
   }

   public boolean removePackage(String pkg) {
      return this.packages.remove(pkg);
   }

   public void addImports(URL src) throws IOException {
      InputStream is = null;
      Reader r = null;
      BufferedReader br = null;

      try {
         is = src.openStream();
         r = new InputStreamReader(is, "UTF-8");
         br = new BufferedReader(r);

         while(true) {
            String line;
            int idx;
            boolean isPackage;
            boolean isClass;
            do {
               do {
                  do {
                     if ((line = br.readLine()) == null) {
                        return;
                     }

                     idx = line.indexOf(35);
                     if (idx != -1) {
                        line = line.substring(0, idx);
                     }

                     line = line.trim();
                  } while(line.length() == 0);

                  idx = line.indexOf(32);
               } while(idx == -1);

               String prefix = line.substring(0, idx);
               line = line.substring(idx + 1);
               isPackage = "package".equals(prefix);
               isClass = "class".equals(prefix);
            } while(!isPackage && !isClass);

            while(line.length() != 0) {
               idx = line.indexOf(32);
               String id;
               if (idx == -1) {
                  id = line;
                  line = "";
               } else {
                  id = line.substring(0, idx);
                  line = line.substring(idx + 1);
               }

               if (id.length() != 0) {
                  if (isClass) {
                     this.addClass(id);
                  } else {
                     this.addPackage(id);
                  }
               }
            }
         }
      } finally {
         if (is != null) {
            try {
               is.close();
            } catch (IOException var22) {
            }

            is = null;
         }

         if (r != null) {
            try {
               r.close();
            } catch (IOException var21) {
            }

            r = null;
         }

         if (br != null) {
            try {
               br.close();
            } catch (IOException var20) {
            }

            br = null;
         }

      }
   }

   static {
      try {
         importFile = System.getProperty("org.apache.batik.script.imports", "META-INF/imports/script.txt");
      } catch (SecurityException var1) {
      } catch (NumberFormatException var2) {
      }

      defaultImports = null;
   }
}
