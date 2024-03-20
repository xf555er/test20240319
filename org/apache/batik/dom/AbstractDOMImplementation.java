package org.apache.batik.dom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.dom.events.DocumentEventSupport;
import org.apache.batik.dom.events.EventSupport;
import org.apache.batik.i18n.Localizable;
import org.apache.batik.i18n.LocalizableSupport;
import org.w3c.dom.DOMImplementation;

public abstract class AbstractDOMImplementation implements DOMImplementation, Localizable, Serializable {
   protected static final String RESOURCES = "org.apache.batik.dom.resources.Messages";
   protected LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.dom.resources.Messages", this.getClass().getClassLoader());
   protected final HashMap features = new HashMap();

   protected void registerFeature(String name, Object value) {
      this.features.put(name.toLowerCase(), value);
   }

   protected AbstractDOMImplementation() {
      this.registerFeature("Core", new String[]{"2.0", "3.0"});
      this.registerFeature("XML", new String[]{"1.0", "2.0", "3.0"});
      this.registerFeature("Events", new String[]{"2.0", "3.0"});
      this.registerFeature("UIEvents", new String[]{"2.0", "3.0"});
      this.registerFeature("MouseEvents", new String[]{"2.0", "3.0"});
      this.registerFeature("TextEvents", "3.0");
      this.registerFeature("KeyboardEvents", "3.0");
      this.registerFeature("MutationEvents", new String[]{"2.0", "3.0"});
      this.registerFeature("MutationNameEvents", "3.0");
      this.registerFeature("Traversal", "2.0");
      this.registerFeature("XPath", "3.0");
   }

   public boolean hasFeature(String feature, String version) {
      if (feature != null && feature.length() != 0) {
         if (feature.charAt(0) == '+') {
            feature = feature.substring(1);
         }

         Object v = this.features.get(feature.toLowerCase());
         if (v == null) {
            return false;
         } else if (version != null && version.length() != 0) {
            if (v instanceof String) {
               return version.equals(v);
            } else {
               String[] va = (String[])((String[])v);
               String[] var5 = va;
               int var6 = va.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  String aVa = var5[var7];
                  if (version.equals(aVa)) {
                     return true;
                  }
               }

               return false;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public Object getFeature(String feature, String version) {
      return this.hasFeature(feature, version) ? this : null;
   }

   public DocumentEventSupport createDocumentEventSupport() {
      return new DocumentEventSupport();
   }

   public EventSupport createEventSupport(AbstractNode n) {
      return new EventSupport(n);
   }

   public void setLocale(Locale l) {
      this.localizableSupport.setLocale(l);
   }

   public Locale getLocale() {
      return this.localizableSupport.getLocale();
   }

   protected void initLocalizable() {
   }

   public String formatMessage(String key, Object[] args) throws MissingResourceException {
      return this.localizableSupport.formatMessage(key, args);
   }
}
