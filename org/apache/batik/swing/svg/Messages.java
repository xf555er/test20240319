package org.apache.batik.swing.svg;

import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;

public class Messages {
   protected static final String RESOURCES = "org.apache.batik.swing.svg.resources.Messages";
   protected static LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.swing.svg.resources.Messages", Messages.class.getClassLoader());

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
}
