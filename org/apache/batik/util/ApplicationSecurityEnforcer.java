package org.apache.batik.util;

import java.net.URL;
import java.security.Policy;

public class ApplicationSecurityEnforcer {
   public static final String EXCEPTION_ALIEN_SECURITY_MANAGER = "ApplicationSecurityEnforcer.message.security.exception.alien.security.manager";
   public static final String EXCEPTION_NO_POLICY_FILE = "ApplicationSecurityEnforcer.message.null.pointer.exception.no.policy.file";
   public static final String PROPERTY_JAVA_SECURITY_POLICY = "java.security.policy";
   public static final String JAR_PROTOCOL = "jar:";
   public static final String JAR_URL_FILE_SEPARATOR = "!/";
   public static final String PROPERTY_APP_DEV_BASE = "app.dev.base";
   public static final String PROPERTY_APP_JAR_BASE = "app.jar.base";
   public static final String APP_MAIN_CLASS_DIR = "classes/";
   protected Class appMainClass;
   protected String securityPolicy;
   protected String appMainClassRelativeURL;
   protected BatikSecurityManager lastSecurityManagerInstalled;

   /** @deprecated */
   public ApplicationSecurityEnforcer(Class appMainClass, String securityPolicy, String appJarFile) {
      this(appMainClass, securityPolicy);
   }

   public ApplicationSecurityEnforcer(Class appMainClass, String securityPolicy) {
      this.appMainClass = appMainClass;
      this.securityPolicy = securityPolicy;
      this.appMainClassRelativeURL = appMainClass.getName().replace('.', '/') + ".class";
   }

   public void enforceSecurity(boolean enforce) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null && sm != this.lastSecurityManagerInstalled) {
         throw new SecurityException(Messages.getString("ApplicationSecurityEnforcer.message.security.exception.alien.security.manager"));
      } else {
         if (enforce) {
            System.setSecurityManager((SecurityManager)null);
            this.installSecurityManager();
         } else if (sm != null) {
            System.setSecurityManager((SecurityManager)null);
            this.lastSecurityManagerInstalled = null;
         }

      }
   }

   public URL getPolicyURL() {
      ClassLoader cl = this.appMainClass.getClassLoader();
      URL policyURL = cl.getResource(this.securityPolicy);
      if (policyURL == null) {
         throw new NullPointerException(Messages.formatMessage("ApplicationSecurityEnforcer.message.null.pointer.exception.no.policy.file", new Object[]{this.securityPolicy}));
      } else {
         return policyURL;
      }
   }

   public void installSecurityManager() {
      Policy policy = Policy.getPolicy();
      BatikSecurityManager securityManager = new BatikSecurityManager();
      ClassLoader cl = this.appMainClass.getClassLoader();
      String securityPolicyProperty = System.getProperty("java.security.policy");
      URL mainClassURL;
      if (securityPolicyProperty == null || securityPolicyProperty.equals("")) {
         mainClassURL = this.getPolicyURL();
         System.setProperty("java.security.policy", mainClassURL.toString());
      }

      mainClassURL = cl.getResource(this.appMainClassRelativeURL);
      if (mainClassURL == null) {
         throw new RuntimeException(this.appMainClassRelativeURL);
      } else {
         String expandedMainClassName = mainClassURL.toString();
         if (expandedMainClassName.startsWith("jar:")) {
            this.setJarBase(expandedMainClassName);
         } else {
            this.setDevBase(expandedMainClassName);
         }

         System.setSecurityManager(securityManager);
         this.lastSecurityManagerInstalled = securityManager;
         policy.refresh();
         if (securityPolicyProperty == null || securityPolicyProperty.equals("")) {
            System.setProperty("java.security.policy", "");
         }

      }
   }

   private void setJarBase(String expandedMainClassName) {
      String curAppJarBase = System.getProperty("app.jar.base");
      if (curAppJarBase == null) {
         expandedMainClassName = expandedMainClassName.substring("jar:".length());
         int codeBaseEnd = expandedMainClassName.indexOf("!/" + this.appMainClassRelativeURL);
         if (codeBaseEnd == -1) {
            throw new RuntimeException();
         }

         String appCodeBase = expandedMainClassName.substring(0, codeBaseEnd);
         codeBaseEnd = appCodeBase.lastIndexOf(47);
         if (codeBaseEnd == -1) {
            appCodeBase = "";
         } else {
            appCodeBase = appCodeBase.substring(0, codeBaseEnd);
         }

         System.setProperty("app.jar.base", appCodeBase);
      }

   }

   private void setDevBase(String expandedMainClassName) {
      String curAppCodeBase = System.getProperty("app.dev.base");
      if (curAppCodeBase == null) {
         int codeBaseEnd = expandedMainClassName.indexOf("classes/" + this.appMainClassRelativeURL);
         if (codeBaseEnd == -1) {
            throw new RuntimeException();
         }

         String appCodeBase = expandedMainClassName.substring(0, codeBaseEnd);
         System.setProperty("app.dev.base", appCodeBase);
      }

   }
}
