package org.apache.fop.render.afp.extensions;

import java.util.Locale;

public enum ExtensionPlacement {
   DEFAULT,
   BEFORE_END;

   public String getXMLValue() {
      String xmlName = this.name().toLowerCase(Locale.ENGLISH);
      xmlName = xmlName.replace('_', '-');
      return xmlName;
   }

   public static ExtensionPlacement fromXMLValue(String value) {
      String name = value.toUpperCase(Locale.ENGLISH);
      name = name.replace('-', '_');
      return valueOf(name);
   }
}
