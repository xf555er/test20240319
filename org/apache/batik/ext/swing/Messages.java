package org.apache.batik.ext.swing;

import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;

public class Messages {
   protected static final String RESOURCES = "org.apache.batik.ext.swing.resources.Messages";
   protected static LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.ext.swing.resources.Messages", Messages.class.getClassLoader());

   protected Messages() {
   }

   public static void setLocale(Locale l) {
      localizableSupport.setLocale(l);
   }

   public static Locale getLocale() {
      return localizableSupport.getLocale();
   }

   public static String formatMessage(String key, Object[] args) throws MissingResourceException {
      return localizableSupport.formatMessage(key, args);
   }

   public static String getString(String key) throws MissingResourceException {
      return formatMessage(key, (Object[])null);
   }
}
