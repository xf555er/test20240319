package org.apache.xml.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.xml.serializer.utils.WrappedRuntimeException;

public final class Encodings {
   private static final String ENCODINGS_FILE;
   static final String DEFAULT_MIME_ENCODING = "UTF-8";
   private static final Hashtable _encodingTableKeyJava;
   private static final Hashtable _encodingTableKeyMime;
   private static final EncodingInfo[] _encodings;

   static Writer getWriter(OutputStream output, String encoding) throws UnsupportedEncodingException {
      for(int i = 0; i < _encodings.length; ++i) {
         if (_encodings[i].name.equalsIgnoreCase(encoding)) {
            try {
               String javaName = _encodings[i].javaName;
               OutputStreamWriter osw = new OutputStreamWriter(output, javaName);
               return osw;
            } catch (IllegalArgumentException var6) {
            } catch (UnsupportedEncodingException var7) {
            }
         }
      }

      try {
         return new OutputStreamWriter(output, encoding);
      } catch (IllegalArgumentException var5) {
         throw new UnsupportedEncodingException(encoding);
      }
   }

   static EncodingInfo getEncodingInfo(String encoding) {
      String normalizedEncoding = toUpperCaseFast(encoding);
      EncodingInfo ei = (EncodingInfo)_encodingTableKeyJava.get(normalizedEncoding);
      if (ei == null) {
         ei = (EncodingInfo)_encodingTableKeyMime.get(normalizedEncoding);
      }

      if (ei == null) {
         ei = new EncodingInfo((String)null, (String)null, '\u0000');
      }

      return ei;
   }

   public static boolean isRecognizedEncoding(String encoding) {
      String normalizedEncoding = encoding.toUpperCase();
      EncodingInfo ei = (EncodingInfo)_encodingTableKeyJava.get(normalizedEncoding);
      if (ei == null) {
         ei = (EncodingInfo)_encodingTableKeyMime.get(normalizedEncoding);
      }

      return ei != null;
   }

   private static String toUpperCaseFast(String s) {
      boolean different = false;
      int mx = s.length();
      char[] chars = new char[mx];

      for(int i = 0; i < mx; ++i) {
         char ch = s.charAt(i);
         if ('a' <= ch && ch <= 'z') {
            ch = (char)(ch + -32);
            different = true;
         }

         chars[i] = ch;
      }

      String upper;
      if (different) {
         upper = String.valueOf(chars);
      } else {
         upper = s;
      }

      return upper;
   }

   static String getMimeEncoding(String encoding) {
      if (null == encoding) {
         try {
            encoding = System.getProperty("file.encoding", "UTF8");
            if (null != encoding) {
               String jencoding = !encoding.equalsIgnoreCase("Cp1252") && !encoding.equalsIgnoreCase("ISO8859_1") && !encoding.equalsIgnoreCase("8859_1") && !encoding.equalsIgnoreCase("UTF8") ? convertJava2MimeEncoding(encoding) : "UTF-8";
               encoding = null != jencoding ? jencoding : "UTF-8";
            } else {
               encoding = "UTF-8";
            }
         } catch (SecurityException var2) {
            encoding = "UTF-8";
         }
      } else {
         encoding = convertJava2MimeEncoding(encoding);
      }

      return encoding;
   }

   private static String convertJava2MimeEncoding(String encoding) {
      EncodingInfo enc = (EncodingInfo)_encodingTableKeyJava.get(toUpperCaseFast(encoding));
      return null != enc ? enc.name : encoding;
   }

   public static String convertMime2JavaEncoding(String encoding) {
      for(int i = 0; i < _encodings.length; ++i) {
         if (_encodings[i].name.equalsIgnoreCase(encoding)) {
            return _encodings[i].javaName;
         }
      }

      return encoding;
   }

   private static EncodingInfo[] loadEncodingInfo() {
      try {
         InputStream is = SecuritySupport.getResourceAsStream(ObjectFactory.findClassLoader(), ENCODINGS_FILE);
         Properties props = new Properties();
         if (is != null) {
            props.load(is);
            is.close();
         }

         int totalEntries = props.size();
         List encodingInfo_list = new ArrayList();
         Enumeration keys = props.keys();

         for(int i = 0; i < totalEntries; ++i) {
            String javaName = (String)keys.nextElement();
            String val = props.getProperty(javaName);
            int len = lengthOfMimeNames(val);
            if (len == 0) {
               boolean var19 = false;
            } else {
               char highChar;
               String mimeNames;
               try {
                  mimeNames = val.substring(len).trim();
                  highChar = (char)Integer.decode(mimeNames);
               } catch (NumberFormatException var15) {
                  highChar = 0;
               }

               mimeNames = val.substring(0, len);
               StringTokenizer st = new StringTokenizer(mimeNames, ",");

               for(boolean first = true; st.hasMoreTokens(); first = false) {
                  String mimeName = st.nextToken();
                  EncodingInfo ei = new EncodingInfo(mimeName, javaName, highChar);
                  encodingInfo_list.add(ei);
                  _encodingTableKeyMime.put(mimeName.toUpperCase(), ei);
                  if (first) {
                     _encodingTableKeyJava.put(javaName.toUpperCase(), ei);
                  }
               }
            }
         }

         EncodingInfo[] ret_ei = new EncodingInfo[encodingInfo_list.size()];
         encodingInfo_list.toArray(ret_ei);
         return ret_ei;
      } catch (MalformedURLException var16) {
         throw new WrappedRuntimeException(var16);
      } catch (IOException var17) {
         throw new WrappedRuntimeException(var17);
      }
   }

   private static int lengthOfMimeNames(String val) {
      int len = val.indexOf(32);
      if (len < 0) {
         len = val.length();
      }

      return len;
   }

   static boolean isHighUTF16Surrogate(char ch) {
      return '\ud800' <= ch && ch <= '\udbff';
   }

   static boolean isLowUTF16Surrogate(char ch) {
      return '\udc00' <= ch && ch <= '\udfff';
   }

   static int toCodePoint(char highSurrogate, char lowSurrogate) {
      int codePoint = (highSurrogate - '\ud800' << 10) + (lowSurrogate - '\udc00') + 65536;
      return codePoint;
   }

   static int toCodePoint(char ch) {
      return ch;
   }

   public static char getHighChar(String encoding) {
      String normalizedEncoding = toUpperCaseFast(encoding);
      EncodingInfo ei = (EncodingInfo)_encodingTableKeyJava.get(normalizedEncoding);
      if (ei == null) {
         ei = (EncodingInfo)_encodingTableKeyMime.get(normalizedEncoding);
      }

      char highCodePoint;
      if (ei != null) {
         highCodePoint = ei.getHighChar();
      } else {
         highCodePoint = 0;
      }

      return highCodePoint;
   }

   static {
      ENCODINGS_FILE = SerializerBase.PKG_PATH + "/Encodings.properties";
      _encodingTableKeyJava = new Hashtable();
      _encodingTableKeyMime = new Hashtable();
      _encodings = loadEncodingInfo();
   }
}
