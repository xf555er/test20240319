package org.apache.batik.i18n;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LocalizableSupport implements Localizable {
   protected LocaleGroup localeGroup;
   protected String bundleName;
   protected ClassLoader classLoader;
   protected Locale locale;
   protected Locale usedLocale;
   List resourceBundles;
   Class lastResourceClass;
   Class cls;

   public LocalizableSupport(String s, Class cls) {
      this(s, cls, (ClassLoader)null);
   }

   public LocalizableSupport(String s, Class cls, ClassLoader cl) {
      this.localeGroup = LocaleGroup.DEFAULT;
      this.resourceBundles = new ArrayList();
      this.bundleName = s;
      this.cls = cls;
      this.classLoader = cl;
   }

   public LocalizableSupport(String s) {
      this(s, (ClassLoader)null);
   }

   public LocalizableSupport(String s, ClassLoader cl) {
      this.localeGroup = LocaleGroup.DEFAULT;
      this.resourceBundles = new ArrayList();
      this.bundleName = s;
      this.classLoader = cl;
   }

   public void setLocale(Locale l) {
      if (this.locale != l) {
         this.locale = l;
         this.resourceBundles.clear();
         this.lastResourceClass = null;
      }

   }

   public Locale getLocale() {
      return this.locale;
   }

   public void setLocaleGroup(LocaleGroup lg) {
      this.localeGroup = lg;
   }

   public LocaleGroup getLocaleGroup() {
      return this.localeGroup;
   }

   public void setDefaultLocale(Locale l) {
      this.localeGroup.setLocale(l);
   }

   public Locale getDefaultLocale() {
      return this.localeGroup.getLocale();
   }

   public String formatMessage(String key, Object[] args) {
      return MessageFormat.format(this.getString(key), args);
   }

   protected Locale getCurrentLocale() {
      if (this.locale != null) {
         return this.locale;
      } else {
         Locale l = this.localeGroup.getLocale();
         return l != null ? l : Locale.getDefault();
      }
   }

   protected boolean setUsedLocale() {
      Locale l = this.getCurrentLocale();
      if (this.usedLocale == l) {
         return false;
      } else {
         this.usedLocale = l;
         this.resourceBundles.clear();
         this.lastResourceClass = null;
         return true;
      }
   }

   public ResourceBundle getResourceBundle() {
      return this.getResourceBundle(0);
   }

   protected boolean hasNextResourceBundle(int i) {
      if (i == 0) {
         return true;
      } else if (i < this.resourceBundles.size()) {
         return true;
      } else if (this.lastResourceClass == null) {
         return false;
      } else {
         return this.lastResourceClass != Object.class;
      }
   }

   protected ResourceBundle lookupResourceBundle(String bundle, Class theClass) {
      ClassLoader cl = this.classLoader;
      ResourceBundle rb = null;
      if (cl != null) {
         try {
            rb = ResourceBundle.getBundle(bundle, this.usedLocale, cl);
         } catch (MissingResourceException var8) {
         }

         if (rb != null) {
            return rb;
         }
      }

      if (theClass != null) {
         try {
            cl = theClass.getClassLoader();
         } catch (SecurityException var7) {
         }
      }

      if (cl == null) {
         cl = this.getClass().getClassLoader();
      }

      try {
         rb = ResourceBundle.getBundle(bundle, this.usedLocale, cl);
      } catch (MissingResourceException var6) {
      }

      return rb;
   }

   protected ResourceBundle getResourceBundle(int i) {
      this.setUsedLocale();
      ResourceBundle rb = null;
      if (this.cls == null) {
         if (this.resourceBundles.size() == 0) {
            rb = this.lookupResourceBundle(this.bundleName, (Class)null);
            this.resourceBundles.add(rb);
         }

         return (ResourceBundle)this.resourceBundles.get(0);
      } else {
         while(i >= this.resourceBundles.size()) {
            if (this.lastResourceClass == Object.class) {
               return null;
            }

            if (this.lastResourceClass == null) {
               this.lastResourceClass = this.cls;
            } else {
               this.lastResourceClass = this.lastResourceClass.getSuperclass();
            }

            Class cl = this.lastResourceClass;
            String bundle = cl.getPackage().getName() + "." + this.bundleName;
            this.resourceBundles.add(this.lookupResourceBundle(bundle, cl));
         }

         return (ResourceBundle)this.resourceBundles.get(i);
      }
   }

   public String getString(String key) throws MissingResourceException {
      this.setUsedLocale();

      for(int i = 0; this.hasNextResourceBundle(i); ++i) {
         ResourceBundle rb = this.getResourceBundle(i);
         if (rb != null) {
            try {
               String ret = rb.getString(key);
               if (ret != null) {
                  return ret;
               }
            } catch (MissingResourceException var5) {
            }
         }
      }

      String classStr = this.cls != null ? this.cls.toString() : this.bundleName;
      throw new MissingResourceException("Unable to find resource: " + key, classStr, key);
   }

   public int getInteger(String key) throws MissingResourceException {
      String i = this.getString(key);

      try {
         return Integer.parseInt(i);
      } catch (NumberFormatException var4) {
         throw new MissingResourceException("Malformed integer", this.bundleName, key);
      }
   }

   public int getCharacter(String key) throws MissingResourceException {
      String s = this.getString(key);
      if (s != null && s.length() != 0) {
         return s.charAt(0);
      } else {
         throw new MissingResourceException("Malformed character", this.bundleName, key);
      }
   }
}
