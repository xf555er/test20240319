package org.apache.batik.ext.swing;

import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.resources.ResourceManager;

public class Resources {
   protected static final String RESOURCES = "org.apache.batik.ext.swing.resources.Messages";
   protected static LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.ext.swing.resources.Messages", Resources.class.getClassLoader());
   protected static ResourceManager resourceManager;

   protected Resources() {
   }

   public static void setLocale(Locale l) {
      localizableSupport.setLocale(l);
      resourceManager = new ResourceManager(localizableSupport.getResourceBundle());
   }

   public static Locale getLocale() {
      return localizableSupport.getLocale();
   }

   public static String formatMessage(String key, Object[] args) throws MissingResourceException {
      return localizableSupport.formatMessage(key, args);
   }

   public static String getString(String key) throws MissingResourceException {
      return resourceManager.getString(key);
   }

   public static int getInteger(String key) throws MissingResourceException {
      return resourceManager.getInteger(key);
   }

   static {
      resourceManager = new ResourceManager(localizableSupport.getResourceBundle());
   }
}
