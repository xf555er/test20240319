package org.apache.xmlgraphics.image.codec.util;

import java.util.MissingResourceException;
import org.apache.xmlgraphics.util.i18n.LocalizableSupport;

public final class PropertyUtil {
   private static final String RESOURCES = "org.apache.xmlgraphics.image.codec.Messages";
   private static final LocalizableSupport LOCALIZABLESUPPORT = new LocalizableSupport("org.apache.xmlgraphics.image.codec.Messages", PropertyUtil.class.getClassLoader());

   private PropertyUtil() {
   }

   public static String getString(String key) {
      try {
         return LOCALIZABLESUPPORT.formatMessage(key, (Object[])null);
      } catch (MissingResourceException var2) {
         return key;
      }
   }
}
