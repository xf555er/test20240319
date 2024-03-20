package org.apache.batik.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class PreferenceManager {
   protected Properties internal;
   protected Map defaults;
   protected String prefFileName;
   protected String fullName;
   protected static final String USER_HOME = getSystemProperty("user.home");
   protected static final String USER_DIR = getSystemProperty("user.dir");
   protected static final String FILE_SEP = getSystemProperty("file.separator");
   private static String PREF_DIR = null;

   protected static String getSystemProperty(String prop) {
      try {
         return System.getProperty(prop);
      } catch (AccessControlException var2) {
         return "";
      }
   }

   public PreferenceManager(String prefFileName) {
      this(prefFileName, (Map)null);
   }

   public PreferenceManager(String prefFileName, Map defaults) {
      this.internal = null;
      this.defaults = null;
      this.prefFileName = null;
      this.fullName = null;
      this.prefFileName = prefFileName;
      this.defaults = defaults;
      this.internal = new Properties();
   }

   public static void setPreferenceDirectory(String dir) {
      PREF_DIR = dir;
   }

   public static String getPreferenceDirectory() {
      return PREF_DIR;
   }

   public void load() throws IOException {
      FileInputStream fis = null;
      if (this.fullName != null) {
         try {
            fis = new FileInputStream(this.fullName);
         } catch (IOException var14) {
            this.fullName = null;
         }
      }

      if (this.fullName == null) {
         if (PREF_DIR != null) {
            try {
               fis = new FileInputStream(this.fullName = PREF_DIR + FILE_SEP + this.prefFileName);
            } catch (IOException var13) {
               this.fullName = null;
            }
         }

         if (this.fullName == null) {
            try {
               fis = new FileInputStream(this.fullName = USER_HOME + FILE_SEP + this.prefFileName);
            } catch (IOException var12) {
               try {
                  fis = new FileInputStream(this.fullName = USER_DIR + FILE_SEP + this.prefFileName);
               } catch (IOException var11) {
                  this.fullName = null;
               }
            }
         }
      }

      if (this.fullName != null) {
         try {
            this.internal.load(fis);
         } finally {
            fis.close();
         }
      }

   }

   public void save() throws IOException {
      FileOutputStream fos = null;
      if (this.fullName != null) {
         try {
            fos = new FileOutputStream(this.fullName);
         } catch (IOException var11) {
            this.fullName = null;
         }
      }

      if (this.fullName == null) {
         if (PREF_DIR != null) {
            try {
               fos = new FileOutputStream(this.fullName = PREF_DIR + FILE_SEP + this.prefFileName);
            } catch (IOException var10) {
               this.fullName = null;
            }
         }

         if (this.fullName == null) {
            try {
               fos = new FileOutputStream(this.fullName = USER_HOME + FILE_SEP + this.prefFileName);
            } catch (IOException var9) {
               this.fullName = null;
               throw var9;
            }
         }
      }

      try {
         this.internal.store(fos, this.prefFileName);
      } finally {
         fos.close();
      }

   }

   private Object getDefault(String key) {
      return this.defaults != null ? this.defaults.get(key) : null;
   }

   public Rectangle getRectangle(String key) {
      Rectangle defaultValue = (Rectangle)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         Rectangle result = new Rectangle();

         try {
            StringTokenizer st = new StringTokenizer(sp, " ", false);
            if (!st.hasMoreTokens()) {
               this.internal.remove(key);
               return defaultValue;
            } else {
               String token = st.nextToken();
               int x = Integer.parseInt(token);
               if (!st.hasMoreTokens()) {
                  this.internal.remove(key);
                  return defaultValue;
               } else {
                  token = st.nextToken();
                  int y = Integer.parseInt(token);
                  if (!st.hasMoreTokens()) {
                     this.internal.remove(key);
                     return defaultValue;
                  } else {
                     token = st.nextToken();
                     int w = Integer.parseInt(token);
                     if (!st.hasMoreTokens()) {
                        this.internal.remove(key);
                        return defaultValue;
                     } else {
                        token = st.nextToken();
                        int h = Integer.parseInt(token);
                        result.setBounds(x, y, w, h);
                        return result;
                     }
                  }
               }
            }
         } catch (NumberFormatException var11) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public Dimension getDimension(String key) {
      Dimension defaultValue = (Dimension)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         Dimension result = new Dimension();

         try {
            StringTokenizer st = new StringTokenizer(sp, " ", false);
            if (!st.hasMoreTokens()) {
               this.internal.remove(key);
               return defaultValue;
            } else {
               String token = st.nextToken();
               int w = Integer.parseInt(token);
               if (!st.hasMoreTokens()) {
                  this.internal.remove(key);
                  return defaultValue;
               } else {
                  token = st.nextToken();
                  int h = Integer.parseInt(token);
                  result.setSize(w, h);
                  return result;
               }
            }
         } catch (NumberFormatException var9) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public Point getPoint(String key) {
      Point defaultValue = (Point)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         Point result = new Point();

         try {
            StringTokenizer st = new StringTokenizer(sp, " ", false);
            if (!st.hasMoreTokens()) {
               this.internal.remove(key);
               return defaultValue;
            } else {
               String token = st.nextToken();
               int x = Integer.parseInt(token);
               if (!st.hasMoreTokens()) {
                  this.internal.remove(key);
                  return defaultValue;
               } else {
                  token = st.nextToken();
                  int y = Integer.parseInt(token);
                  if (!st.hasMoreTokens()) {
                     this.internal.remove(key);
                     return defaultValue;
                  } else {
                     result.setLocation(x, y);
                     return result;
                  }
               }
            }
         } catch (NumberFormatException var9) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public Color getColor(String key) {
      Color defaultValue = (Color)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         try {
            StringTokenizer st = new StringTokenizer(sp, " ", false);
            if (!st.hasMoreTokens()) {
               this.internal.remove(key);
               return defaultValue;
            } else {
               String token = st.nextToken();
               int r = Integer.parseInt(token);
               if (!st.hasMoreTokens()) {
                  this.internal.remove(key);
                  return defaultValue;
               } else {
                  token = st.nextToken();
                  int g = Integer.parseInt(token);
                  if (!st.hasMoreTokens()) {
                     this.internal.remove(key);
                     return defaultValue;
                  } else {
                     token = st.nextToken();
                     int b = Integer.parseInt(token);
                     if (!st.hasMoreTokens()) {
                        this.internal.remove(key);
                        return defaultValue;
                     } else {
                        token = st.nextToken();
                        int a = Integer.parseInt(token);
                        return new Color(r, g, b, a);
                     }
                  }
               }
            }
         } catch (NumberFormatException var10) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public Font getFont(String key) {
      Font defaultValue = (Font)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         try {
            StringTokenizer st = new StringTokenizer(sp, " ", false);
            if (!st.hasMoreTokens()) {
               this.internal.remove(key);
               return defaultValue;
            } else {
               String name = st.nextToken();
               if (!st.hasMoreTokens()) {
                  this.internal.remove(key);
                  return defaultValue;
               } else {
                  String token = st.nextToken();
                  int size = Integer.parseInt(token);
                  if (!st.hasMoreTokens()) {
                     this.internal.remove(key);
                     return defaultValue;
                  } else {
                     token = st.nextToken();
                     int type = Integer.parseInt(token);
                     return new Font(name, type, size);
                  }
               }
            }
         } catch (NumberFormatException var9) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public String getString(String key) {
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         sp = (String)this.getDefault(key);
      }

      return sp;
   }

   public String[] getStrings(String mkey) {
      int i = 0;
      ArrayList v = new ArrayList();

      while(true) {
         String last = this.getString(mkey + i);
         ++i;
         if (last == null) {
            if (v.size() != 0) {
               String[] str = new String[v.size()];
               return (String[])((String[])v.toArray(str));
            } else {
               return (String[])((String[])this.getDefault(mkey));
            }
         }

         v.add(last);
      }
   }

   public URL getURL(String key) {
      URL defaultValue = (URL)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         URL url = null;

         try {
            url = new URL(sp);
            return url;
         } catch (MalformedURLException var6) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public URL[] getURLs(String mkey) {
      int i = 0;
      ArrayList v = new ArrayList();

      while(true) {
         URL last = this.getURL(mkey + i);
         ++i;
         if (last == null) {
            if (v.size() != 0) {
               URL[] path = new URL[v.size()];
               return (URL[])((URL[])v.toArray(path));
            } else {
               return (URL[])((URL[])this.getDefault(mkey));
            }
         }

         v.add(last);
      }
   }

   public File getFile(String key) {
      File defaultValue = (File)this.getDefault(key);
      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         File file = new File(sp);
         if (file.exists()) {
            return file;
         } else {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public File[] getFiles(String mkey) {
      int i = 0;
      ArrayList v = new ArrayList();

      while(true) {
         File last = this.getFile(mkey + i);
         ++i;
         if (last == null) {
            if (v.size() != 0) {
               File[] path = new File[v.size()];
               return (File[])((File[])v.toArray(path));
            } else {
               return (File[])((File[])this.getDefault(mkey));
            }
         }

         v.add(last);
      }
   }

   public int getInteger(String key) {
      int defaultValue = 0;
      if (this.getDefault(key) != null) {
         defaultValue = (Integer)this.getDefault(key);
      }

      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         try {
            int value = Integer.parseInt(sp);
            return value;
         } catch (NumberFormatException var6) {
            this.internal.remove(key);
            return defaultValue;
         }
      }
   }

   public float getFloat(String key) {
      float defaultValue = 0.0F;
      if (this.getDefault(key) != null) {
         defaultValue = (Float)this.getDefault(key);
      }

      String sp = this.internal.getProperty(key);
      if (sp == null) {
         return defaultValue;
      } else {
         try {
            float value = Float.parseFloat(sp);
            return value;
         } catch (NumberFormatException var6) {
            this.setFloat(key, defaultValue);
            return defaultValue;
         }
      }
   }

   public boolean getBoolean(String key) {
      if (this.internal.getProperty(key) != null) {
         return this.internal.getProperty(key).equals("true");
      } else {
         return this.getDefault(key) != null ? (Boolean)this.getDefault(key) : false;
      }
   }

   public void setRectangle(String key, Rectangle value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.x + " " + value.y + " " + value.width + ' ' + value.height);
      } else {
         this.internal.remove(key);
      }

   }

   public void setDimension(String key, Dimension value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.width + " " + value.height);
      } else {
         this.internal.remove(key);
      }

   }

   public void setPoint(String key, Point value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.x + " " + value.y);
      } else {
         this.internal.remove(key);
      }

   }

   public void setColor(String key, Color value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.getRed() + " " + value.getGreen() + " " + value.getBlue() + " " + value.getAlpha());
      } else {
         this.internal.remove(key);
      }

   }

   public void setFont(String key, Font value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.getName() + " " + value.getSize() + " " + value.getStyle());
      } else {
         this.internal.remove(key);
      }

   }

   public void setString(String key, String value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value);
      } else {
         this.internal.remove(key);
      }

   }

   public void setStrings(String mkey, String[] values) {
      int j = 0;
      if (values != null) {
         String[] var4 = values;
         int var5 = values.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String value = var4[var6];
            if (value != null) {
               this.setString(mkey + j, value);
               ++j;
            }
         }
      }

      while(true) {
         String last = this.getString(mkey + j);
         if (last == null) {
            return;
         }

         this.setString(mkey + j, (String)null);
         ++j;
      }
   }

   public void setURL(String key, URL value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.toString());
      } else {
         this.internal.remove(key);
      }

   }

   public void setURLs(String mkey, URL[] values) {
      int j = 0;
      if (values != null) {
         URL[] var4 = values;
         int var5 = values.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            URL value = var4[var6];
            if (value != null) {
               this.setURL(mkey + j, value);
               ++j;
            }
         }
      }

      while(true) {
         String last = this.getString(mkey + j);
         if (last == null) {
            return;
         }

         this.setString(mkey + j, (String)null);
         ++j;
      }
   }

   public void setFile(String key, File value) {
      if (value != null && !value.equals(this.getDefault(key))) {
         this.internal.setProperty(key, value.getAbsolutePath());
      } else {
         this.internal.remove(key);
      }

   }

   public void setFiles(String mkey, File[] values) {
      int j = 0;
      if (values != null) {
         File[] var4 = values;
         int var5 = values.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File value = var4[var6];
            if (value != null) {
               this.setFile(mkey + j, value);
               ++j;
            }
         }
      }

      while(true) {
         String last = this.getString(mkey + j);
         if (last == null) {
            return;
         }

         this.setString(mkey + j, (String)null);
         ++j;
      }
   }

   public void setInteger(String key, int value) {
      if (this.getDefault(key) != null && (Integer)this.getDefault(key) != value) {
         this.internal.setProperty(key, Integer.toString(value));
      } else {
         this.internal.remove(key);
      }

   }

   public void setFloat(String key, float value) {
      if (this.getDefault(key) != null && (Float)this.getDefault(key) != value) {
         this.internal.setProperty(key, Float.toString(value));
      } else {
         this.internal.remove(key);
      }

   }

   public void setBoolean(String key, boolean value) {
      if (this.getDefault(key) != null && (Boolean)this.getDefault(key) != value) {
         this.internal.setProperty(key, value ? "true" : "false");
      } else {
         this.internal.remove(key);
      }

   }
}
