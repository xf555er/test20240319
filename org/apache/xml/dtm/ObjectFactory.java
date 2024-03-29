package org.apache.xml.dtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

final class ObjectFactory {
   private static final String DEFAULT_PROPERTIES_FILENAME = "xalan.properties";
   private static final String SERVICES_PATH = "META-INF/services/";
   private static final boolean DEBUG = false;
   private static Properties fXalanProperties = null;
   private static long fLastModified = -1L;
   // $FF: synthetic field
   static Class class$org$apache$xml$dtm$ObjectFactory;

   static Object createObject(String factoryId, String fallbackClassName) throws ConfigurationError {
      return createObject(factoryId, (String)null, fallbackClassName);
   }

   static Object createObject(String factoryId, String propertiesFilename, String fallbackClassName) throws ConfigurationError {
      Class factoryClass = lookUpFactoryClass(factoryId, propertiesFilename, fallbackClassName);
      if (factoryClass == null) {
         throw new ConfigurationError("Provider for " + factoryId + " cannot be found", (Exception)null);
      } else {
         try {
            Object instance = factoryClass.newInstance();
            debugPrintln("created new instance of factory " + factoryId);
            return instance;
         } catch (Exception var5) {
            throw new ConfigurationError("Provider for factory " + factoryId + " could not be instantiated: " + var5, var5);
         }
      }
   }

   static Class lookUpFactoryClass(String factoryId) throws ConfigurationError {
      return lookUpFactoryClass(factoryId, (String)null, (String)null);
   }

   static Class lookUpFactoryClass(String factoryId, String propertiesFilename, String fallbackClassName) throws ConfigurationError {
      String factoryClassName = lookUpFactoryClassName(factoryId, propertiesFilename, fallbackClassName);
      ClassLoader cl = findClassLoader();
      if (factoryClassName == null) {
         factoryClassName = fallbackClassName;
      }

      try {
         Class providerClass = findProviderClass(factoryClassName, cl, true);
         debugPrintln("created new instance of " + providerClass + " using ClassLoader: " + cl);
         return providerClass;
      } catch (ClassNotFoundException var6) {
         throw new ConfigurationError("Provider " + factoryClassName + " not found", var6);
      } catch (Exception var7) {
         throw new ConfigurationError("Provider " + factoryClassName + " could not be instantiated: " + var7, var7);
      }
   }

   static String lookUpFactoryClassName(String factoryId, String propertiesFilename, String fallbackClassName) {
      String factoryClassName;
      try {
         factoryClassName = SecuritySupport.getSystemProperty(factoryId);
         if (factoryClassName != null) {
            debugPrintln("found system property, value=" + factoryClassName);
            return factoryClassName;
         }
      } catch (SecurityException var43) {
      }

      factoryClassName = null;
      if (propertiesFilename == null) {
         File propertiesFile = null;
         boolean propertiesFileExists = false;

         try {
            String javah = SecuritySupport.getSystemProperty("java.home");
            propertiesFilename = javah + File.separator + "lib" + File.separator + "xalan.properties";
            propertiesFile = new File(propertiesFilename);
            propertiesFileExists = SecuritySupport.getFileExists(propertiesFile);
         } catch (SecurityException var42) {
            fLastModified = -1L;
            fXalanProperties = null;
         }

         synchronized(class$org$apache$xml$dtm$ObjectFactory == null ? (class$org$apache$xml$dtm$ObjectFactory = class$("org.apache.xml.dtm.ObjectFactory")) : class$org$apache$xml$dtm$ObjectFactory) {
            boolean loadProperties = false;
            FileInputStream fis = null;

            try {
               if (fLastModified >= 0L) {
                  if (propertiesFileExists && fLastModified < (fLastModified = SecuritySupport.getLastModified(propertiesFile))) {
                     loadProperties = true;
                  } else if (!propertiesFileExists) {
                     fLastModified = -1L;
                     fXalanProperties = null;
                  }
               } else if (propertiesFileExists) {
                  loadProperties = true;
                  fLastModified = SecuritySupport.getLastModified(propertiesFile);
               }

               if (loadProperties) {
                  fXalanProperties = new Properties();
                  fis = SecuritySupport.getFileInputStream(propertiesFile);
                  fXalanProperties.load(fis);
               }
            } catch (Exception var45) {
               fXalanProperties = null;
               fLastModified = -1L;
            } finally {
               if (fis != null) {
                  try {
                     fis.close();
                  } catch (IOException var39) {
                  }
               }

            }
         }

         if (fXalanProperties != null) {
            factoryClassName = fXalanProperties.getProperty(factoryId);
         }
      } else {
         FileInputStream fis = null;

         try {
            fis = SecuritySupport.getFileInputStream(new File(propertiesFilename));
            Properties props = new Properties();
            props.load(fis);
            factoryClassName = props.getProperty(factoryId);
         } catch (Exception var41) {
         } finally {
            if (fis != null) {
               try {
                  fis.close();
               } catch (IOException var40) {
               }
            }

         }
      }

      if (factoryClassName != null) {
         debugPrintln("found in " + propertiesFilename + ", value=" + factoryClassName);
         return factoryClassName;
      } else {
         return findJarServiceProviderName(factoryId);
      }
   }

   private static void debugPrintln(String msg) {
   }

   static ClassLoader findClassLoader() throws ConfigurationError {
      ClassLoader context = SecuritySupport.getContextClassLoader();
      ClassLoader system = SecuritySupport.getSystemClassLoader();

      ClassLoader chain;
      for(chain = system; context != chain; chain = SecuritySupport.getParentClassLoader(chain)) {
         if (chain == null) {
            return context;
         }
      }

      ClassLoader current = (class$org$apache$xml$dtm$ObjectFactory == null ? (class$org$apache$xml$dtm$ObjectFactory = class$("org.apache.xml.dtm.ObjectFactory")) : class$org$apache$xml$dtm$ObjectFactory).getClassLoader();

      for(chain = system; current != chain; chain = SecuritySupport.getParentClassLoader(chain)) {
         if (chain == null) {
            return current;
         }
      }

      return system;
   }

   static Object newInstance(String className, ClassLoader cl, boolean doFallback) throws ConfigurationError {
      try {
         Class providerClass = findProviderClass(className, cl, doFallback);
         Object instance = providerClass.newInstance();
         debugPrintln("created new instance of " + providerClass + " using ClassLoader: " + cl);
         return instance;
      } catch (ClassNotFoundException var5) {
         throw new ConfigurationError("Provider " + className + " not found", var5);
      } catch (Exception var6) {
         throw new ConfigurationError("Provider " + className + " could not be instantiated: " + var6, var6);
      }
   }

   static Class findProviderClass(String className, ClassLoader cl, boolean doFallback) throws ClassNotFoundException, ConfigurationError {
      SecurityManager security = System.getSecurityManager();

      try {
         if (security != null) {
            int lastDot = className.lastIndexOf(46);
            String packageName = className;
            if (lastDot != -1) {
               packageName = className.substring(0, lastDot);
            }

            security.checkPackageAccess(packageName);
         }
      } catch (SecurityException var7) {
         throw var7;
      }

      Class providerClass;
      if (cl == null) {
         providerClass = Class.forName(className);
      } else {
         try {
            providerClass = cl.loadClass(className);
         } catch (ClassNotFoundException var8) {
            if (!doFallback) {
               throw var8;
            }

            ClassLoader current = (class$org$apache$xml$dtm$ObjectFactory == null ? (class$org$apache$xml$dtm$ObjectFactory = class$("org.apache.xml.dtm.ObjectFactory")) : class$org$apache$xml$dtm$ObjectFactory).getClassLoader();
            if (current == null) {
               providerClass = Class.forName(className);
            } else {
               if (cl == current) {
                  throw var8;
               }

               providerClass = current.loadClass(className);
            }
         }
      }

      return providerClass;
   }

   private static String findJarServiceProviderName(String factoryId) {
      String serviceId = "META-INF/services/" + factoryId;
      InputStream is = null;
      ClassLoader cl = findClassLoader();
      is = SecuritySupport.getResourceAsStream(cl, serviceId);
      if (is == null) {
         ClassLoader current = (class$org$apache$xml$dtm$ObjectFactory == null ? (class$org$apache$xml$dtm$ObjectFactory = class$("org.apache.xml.dtm.ObjectFactory")) : class$org$apache$xml$dtm$ObjectFactory).getClassLoader();
         if (cl != current) {
            cl = current;
            is = SecuritySupport.getResourceAsStream(current, serviceId);
         }
      }

      if (is == null) {
         return null;
      } else {
         debugPrintln("found jar resource=" + serviceId + " using ClassLoader: " + cl);

         BufferedReader rd;
         try {
            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
         } catch (UnsupportedEncodingException var18) {
            rd = new BufferedReader(new InputStreamReader(is));
         }

         String factoryClassName = null;

         label116: {
            Object var7;
            try {
               factoryClassName = rd.readLine();
               break label116;
            } catch (IOException var19) {
               var7 = null;
            } finally {
               try {
                  rd.close();
               } catch (IOException var17) {
               }

            }

            return (String)var7;
         }

         if (factoryClassName != null && !"".equals(factoryClassName)) {
            debugPrintln("found in resource, value=" + factoryClassName);
            return factoryClassName;
         } else {
            return null;
         }
      }
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

   static class ConfigurationError extends Error {
      static final long serialVersionUID = 5122054096615067992L;
      private Exception exception;

      ConfigurationError(String msg, Exception x) {
         super(msg);
         this.exception = x;
      }

      Exception getException() {
         return this.exception;
      }
   }
}
