package org.apache.batik.util;

import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.util.resources.ResourceManager;

public class Messages {
   protected static final String RESOURCES = "org.apache.batik.util.resources.Messages";
   protected static LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.util.resources.Messages", Messages.class.getClassLoader());
   protected static ResourceManager resourceManager;

   protected Messages() {
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

   public static int getCharacter(String key) throws MissingResourceException {
      return resourceManager.getCharacter(key);
   }

   static {
      resourceManager = new ResourceManager(localizableSupport.getResourceBundle());
   }
}
