package org.apache.batik.ext.awt.image.codec.util;

import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;

public class PropertyUtil {
   protected static final String RESOURCES = "org.apache.batik.bridge.resources.properties";
   protected static LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.bridge.resources.properties", PropertyUtil.class.getClassLoader());

   public static String getString(String key) {
      try {
         return localizableSupport.formatMessage(key, (Object[])null);
      } catch (MissingResourceException var2) {
         return key;
      }
   }
}
