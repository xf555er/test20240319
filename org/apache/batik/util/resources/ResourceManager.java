package org.apache.batik.util.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class ResourceManager {
   protected ResourceBundle bundle;

   public ResourceManager(ResourceBundle rb) {
      this.bundle = rb;
   }

   public String getString(String key) throws MissingResourceException {
      return this.bundle.getString(key);
   }

   public List getStringList(String key) throws MissingResourceException {
      return this.getStringList(key, " \t\n\r\f", false);
   }

   public List getStringList(String key, String delim) throws MissingResourceException {
      return this.getStringList(key, delim, false);
   }

   public List getStringList(String key, String delim, boolean returnDelims) throws MissingResourceException {
      List result = new ArrayList();
      StringTokenizer st = new StringTokenizer(this.getString(key), delim, returnDelims);

      while(st.hasMoreTokens()) {
         result.add(st.nextToken());
      }

      return result;
   }

   public boolean getBoolean(String key) throws MissingResourceException, ResourceFormatException {
      String b = this.getString(key);
      if (b.equals("true")) {
         return true;
      } else if (b.equals("false")) {
         return false;
      } else {
         throw new ResourceFormatException("Malformed boolean", this.bundle.getClass().getName(), key);
      }
   }

   public int getInteger(String key) throws MissingResourceException, ResourceFormatException {
      String i = this.getString(key);

      try {
         return Integer.parseInt(i);
      } catch (NumberFormatException var4) {
         throw new ResourceFormatException("Malformed integer", this.bundle.getClass().getName(), key);
      }
   }

   public int getCharacter(String key) throws MissingResourceException, ResourceFormatException {
      String s = this.getString(key);
      if (s != null && s.length() != 0) {
         return s.charAt(0);
      } else {
         throw new ResourceFormatException("Malformed character", this.bundle.getClass().getName(), key);
      }
   }
}
