package org.apache.xml.serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.xml.transform.TransformerException;
import org.apache.xml.serializer.utils.SystemIDResolver;
import org.apache.xml.serializer.utils.Utils;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

final class CharInfo {
   private HashMap m_charToString;
   public static final String HTML_ENTITIES_RESOURCE;
   public static final String XML_ENTITIES_RESOURCE;
   static final char S_HORIZONAL_TAB = '\t';
   static final char S_LINEFEED = '\n';
   static final char S_CARRIAGERETURN = '\r';
   static final char S_SPACE = ' ';
   static final char S_QUOTE = '"';
   static final char S_LT = '<';
   static final char S_GT = '>';
   static final char S_NEL = '\u0085';
   static final char S_LINE_SEPARATOR = '\u2028';
   boolean onlyQuotAmpLtGt;
   static final int ASCII_MAX = 128;
   private final boolean[] shouldMapAttrChar_ASCII;
   private final boolean[] shouldMapTextChar_ASCII;
   private final int[] array_of_bits;
   private static final int SHIFT_PER_WORD = 5;
   private static final int LOW_ORDER_BITMASK = 31;
   private int firstWordNotUsed;
   private final CharKey m_charKey;
   private static Hashtable m_getCharInfoCache;
   // $FF: synthetic field
   static Class class$org$apache$xml$serializer$CharInfo;

   private CharInfo() {
      this.array_of_bits = this.createEmptySetOfIntegers(65535);
      this.firstWordNotUsed = 0;
      this.shouldMapAttrChar_ASCII = new boolean[128];
      this.shouldMapTextChar_ASCII = new boolean[128];
      this.m_charKey = new CharKey();
      this.onlyQuotAmpLtGt = true;
   }

   private CharInfo(String entitiesResource, String method, boolean internal) {
      this();
      this.m_charToString = new HashMap();
      ResourceBundle entities = null;
      boolean noExtraEntities = true;
      if (internal) {
         try {
            entities = PropertyResourceBundle.getBundle(entitiesResource);
         } catch (Exception var26) {
         }
      }

      String line;
      int index;
      if (entities != null) {
         Enumeration keys = entities.getKeys();

         while(keys.hasMoreElements()) {
            String name = (String)keys.nextElement();
            line = entities.getString(name);
            index = Integer.parseInt(line);
            boolean extra = this.defineEntity(name, (char)index);
            if (extra) {
               noExtraEntities = false;
            }
         }
      } else {
         InputStream is = null;

         try {
            if (internal) {
               is = (class$org$apache$xml$serializer$CharInfo == null ? (class$org$apache$xml$serializer$CharInfo = class$("org.apache.xml.serializer.CharInfo")) : class$org$apache$xml$serializer$CharInfo).getResourceAsStream(entitiesResource);
            } else {
               ClassLoader cl = ObjectFactory.findClassLoader();
               if (cl == null) {
                  is = ClassLoader.getSystemResourceAsStream(entitiesResource);
               } else {
                  is = cl.getResourceAsStream(entitiesResource);
               }

               if (is == null) {
                  try {
                     URL url = new URL(entitiesResource);
                     is = url.openStream();
                  } catch (Exception var25) {
                  }
               }
            }

            if (is == null) {
               throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_FIND", new Object[]{entitiesResource, entitiesResource}));
            }

            BufferedReader reader;
            try {
               reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            } catch (UnsupportedEncodingException var24) {
               reader = new BufferedReader(new InputStreamReader(is));
            }

            line = reader.readLine();

            while(true) {
               while(line != null) {
                  if (line.length() != 0 && line.charAt(0) != '#') {
                     index = line.indexOf(32);
                     if (index > 1) {
                        String name = line.substring(0, index);
                        ++index;
                        if (index < line.length()) {
                           String value = line.substring(index);
                           index = value.indexOf(32);
                           if (index > 0) {
                              value = value.substring(0, index);
                           }

                           int code = Integer.parseInt(value);
                           boolean extra = this.defineEntity(name, (char)code);
                           if (extra) {
                              noExtraEntities = false;
                           }
                        }
                     }

                     line = reader.readLine();
                  } else {
                     line = reader.readLine();
                  }
               }

               is.close();
               break;
            }
         } catch (Exception var27) {
            throw new RuntimeException(Utils.messages.createMessage("ER_RESOURCE_COULD_NOT_LOAD", new Object[]{entitiesResource, var27.toString(), entitiesResource, var27.toString()}));
         } finally {
            if (is != null) {
               try {
                  is.close();
               } catch (Exception var23) {
               }
            }

         }
      }

      this.onlyQuotAmpLtGt = noExtraEntities;
      if ("xml".equals(method)) {
         this.shouldMapTextChar_ASCII[34] = false;
      }

      if ("html".equals(method)) {
         this.shouldMapAttrChar_ASCII[60] = false;
         this.shouldMapTextChar_ASCII[34] = false;
      }

   }

   private boolean defineEntity(String name, char value) {
      StringBuffer sb = new StringBuffer("&");
      sb.append(name);
      sb.append(';');
      String entityString = sb.toString();
      boolean extra = this.defineChar2StringMapping(entityString, value);
      return extra;
   }

   String getOutputStringForChar(char value) {
      this.m_charKey.setChar(value);
      return (String)this.m_charToString.get(this.m_charKey);
   }

   final boolean shouldMapAttrChar(int value) {
      return value < 128 ? this.shouldMapAttrChar_ASCII[value] : this.get(value);
   }

   final boolean shouldMapTextChar(int value) {
      return value < 128 ? this.shouldMapTextChar_ASCII[value] : this.get(value);
   }

   private static CharInfo getCharInfoBasedOnPrivilege(final String entitiesFileName, final String method, final boolean internal) {
      return (CharInfo)AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            return new CharInfo(entitiesFileName, method, internal);
         }
      });
   }

   static CharInfo getCharInfo(String entitiesFileName, String method) {
      CharInfo charInfo = (CharInfo)m_getCharInfoCache.get(entitiesFileName);
      if (charInfo != null) {
         return mutableCopyOf(charInfo);
      } else {
         try {
            charInfo = getCharInfoBasedOnPrivilege(entitiesFileName, method, true);
            m_getCharInfoCache.put(entitiesFileName, charInfo);
            return mutableCopyOf(charInfo);
         } catch (Exception var7) {
            try {
               return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            } catch (Exception var6) {
               String absoluteEntitiesFileName;
               if (entitiesFileName.indexOf(58) < 0) {
                  absoluteEntitiesFileName = SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
               } else {
                  try {
                     absoluteEntitiesFileName = SystemIDResolver.getAbsoluteURI(entitiesFileName, (String)null);
                  } catch (TransformerException var5) {
                     throw new WrappedRuntimeException(var5);
                  }
               }

               return getCharInfoBasedOnPrivilege(entitiesFileName, method, false);
            }
         }
      }
   }

   private static CharInfo mutableCopyOf(CharInfo charInfo) {
      CharInfo copy = new CharInfo();
      int max = charInfo.array_of_bits.length;
      System.arraycopy(charInfo.array_of_bits, 0, copy.array_of_bits, 0, max);
      copy.firstWordNotUsed = charInfo.firstWordNotUsed;
      max = charInfo.shouldMapAttrChar_ASCII.length;
      System.arraycopy(charInfo.shouldMapAttrChar_ASCII, 0, copy.shouldMapAttrChar_ASCII, 0, max);
      max = charInfo.shouldMapTextChar_ASCII.length;
      System.arraycopy(charInfo.shouldMapTextChar_ASCII, 0, copy.shouldMapTextChar_ASCII, 0, max);
      copy.m_charToString = (HashMap)charInfo.m_charToString.clone();
      copy.onlyQuotAmpLtGt = charInfo.onlyQuotAmpLtGt;
      return copy;
   }

   private static int arrayIndex(int i) {
      return i >> 5;
   }

   private static int bit(int i) {
      int ret = 1 << (i & 31);
      return ret;
   }

   private int[] createEmptySetOfIntegers(int max) {
      this.firstWordNotUsed = 0;
      int[] arr = new int[arrayIndex(max - 1) + 1];
      return arr;
   }

   private final void set(int i) {
      this.setASCIItextDirty(i);
      this.setASCIIattrDirty(i);
      int j = i >> 5;
      int k = j + 1;
      if (this.firstWordNotUsed < k) {
         this.firstWordNotUsed = k;
      }

      int[] var10000 = this.array_of_bits;
      var10000[j] |= 1 << (i & 31);
   }

   private final boolean get(int i) {
      boolean in_the_set = false;
      int j = i >> 5;
      if (j < this.firstWordNotUsed) {
         in_the_set = (this.array_of_bits[j] & 1 << (i & 31)) != 0;
      }

      return in_the_set;
   }

   private boolean extraEntity(String outputString, int charToMap) {
      boolean extra = false;
      if (charToMap < 128) {
         switch (charToMap) {
            case 34:
               if (!outputString.equals("&quot;")) {
                  extra = true;
               }
               break;
            case 38:
               if (!outputString.equals("&amp;")) {
                  extra = true;
               }
               break;
            case 60:
               if (!outputString.equals("&lt;")) {
                  extra = true;
               }
               break;
            case 62:
               if (!outputString.equals("&gt;")) {
                  extra = true;
               }
               break;
            default:
               extra = true;
         }
      }

      return extra;
   }

   private void setASCIItextDirty(int j) {
      if (0 <= j && j < 128) {
         this.shouldMapTextChar_ASCII[j] = true;
      }

   }

   private void setASCIIattrDirty(int j) {
      if (0 <= j && j < 128) {
         this.shouldMapAttrChar_ASCII[j] = true;
      }

   }

   boolean defineChar2StringMapping(String outputString, char inputChar) {
      CharKey character = new CharKey(inputChar);
      this.m_charToString.put(character, outputString);
      this.set(inputChar);
      boolean extraMapping = this.extraEntity(outputString, inputChar);
      return extraMapping;
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException var2) {
         throw new NoClassDefFoundError(var2.getMessage());
      }
   }

   // $FF: synthetic method
   CharInfo(String x0, String x1, boolean x2, Object x3) {
      this(x0, x1, x2);
   }

   static {
      HTML_ENTITIES_RESOURCE = SerializerBase.PKG_NAME + ".HTMLEntities";
      XML_ENTITIES_RESOURCE = SerializerBase.PKG_NAME + ".XMLEntities";
      m_getCharInfoCache = new Hashtable();
   }

   private static class CharKey {
      private char m_char;

      public CharKey(char key) {
         this.m_char = key;
      }

      public CharKey() {
      }

      public final void setChar(char c) {
         this.m_char = c;
      }

      public final int hashCode() {
         return this.m_char;
      }

      public final boolean equals(Object obj) {
         return ((CharKey)obj).m_char == this.m_char;
      }
   }
}
