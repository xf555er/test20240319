package org.apache.fop.tools.anttasks;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class RunTest extends Task {
   private String basedir;
   private String testsuite = "";
   private String referenceJar = "";
   private String refVersion = "";

   public void setTestSuite(String str) {
      this.testsuite = str;
   }

   public void setBasedir(String str) {
      this.basedir = str;
   }

   public void setReference(String str) {
      this.referenceJar = str;
   }

   public void setRefVersion(String str) {
      this.refVersion = str;
   }

   public void execute() throws BuildException {
      this.runReference();
      this.testNewBuild();
   }

   protected void testNewBuild() {
      try {
         ClassLoader loader = new URLClassLoader(this.createUrls("build/fop.jar"));
         Map diff = this.runConverter(loader, "areatree", "reference/output/");
         if (diff != null && !diff.isEmpty()) {
            System.out.println("====================================");
            System.out.println("The following files differ:");
            boolean broke = false;
            Iterator var4 = diff.entrySet().iterator();

            while(var4.hasNext()) {
               Map.Entry e = (Map.Entry)var4.next();
               Object fname = e.getKey();
               Boolean pass = (Boolean)e.getValue();
               System.out.println("file: " + fname + " - reference success: " + pass);
               if (pass) {
                  broke = true;
               }
            }

            if (broke) {
               throw new BuildException("Working tests have been changed.");
            }
         }
      } catch (MalformedURLException var8) {
         var8.printStackTrace();
      }

   }

   protected void runReference() throws BuildException {
      File f = new File(this.basedir + "/reference/output/");

      try {
         final URL[] urls = this.createUrls(this.referenceJar);
         ClassLoader loader = (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
               return new URLClassLoader(urls);
            }
         });
         boolean failed = false;

         try {
            Class cla = Class.forName("org.apache.fop.apps.Fop", true, loader);
            Method get = cla.getMethod("getVersion");
            if (!get.invoke((Object)null).equals(this.refVersion)) {
               throw new BuildException("Reference jar is not correct version it must be: " + this.refVersion);
            }
         } catch (IllegalAccessException var7) {
            failed = true;
         } catch (IllegalArgumentException var8) {
            failed = true;
         } catch (InvocationTargetException var9) {
            failed = true;
         } catch (ClassNotFoundException var10) {
            failed = true;
         } catch (NoSuchMethodException var11) {
            failed = true;
         }

         if (failed) {
            throw new BuildException("Reference jar could not be found in: " + this.basedir + "/reference/");
         }

         f.mkdirs();
         this.runConverter(loader, "reference/output/", (String)null);
      } catch (MalformedURLException var12) {
         var12.printStackTrace();
      }

   }

   protected Map runConverter(ClassLoader loader, String dest, String compDir) {
      String converter = "org.apache.fop.tools.TestConverter";
      Map diff = null;

      try {
         Class cla = Class.forName(converter, true, loader);
         Object tc = cla.getDeclaredConstructor().newInstance();
         Method meth = cla.getMethod("setBaseDir", String.class);
         meth.invoke(tc, this.basedir);
         meth = cla.getMethod("runTests", String.class, String.class, String.class);
         diff = (Map)meth.invoke(tc, this.testsuite, dest, compDir);
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      return diff;
   }

   private URL[] createUrls(String mainJar) throws MalformedURLException {
      List urls = new ArrayList();
      urls.add((new File(mainJar)).toURI().toURL());
      File[] libFiles = (new File("lib")).listFiles();
      if (libFiles != null) {
         File[] var4 = libFiles;
         int var5 = libFiles.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File libFile = var4[var6];
            if (libFile.getPath().endsWith(".jar")) {
               urls.add(libFile.toURI().toURL());
            }
         }
      }

      return (URL[])urls.toArray(new URL[urls.size()]);
   }
}
