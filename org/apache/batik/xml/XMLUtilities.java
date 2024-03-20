package org.apache.batik.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import org.apache.batik.util.EncodingUtilities;

public class XMLUtilities extends XMLCharacters {
   public static final int IS_XML_10_NAME = 1;
   public static final int IS_XML_10_QNAME = 2;

   protected XMLUtilities() {
   }

   public static boolean isXMLSpace(char c) {
      return c <= ' ' && (4294977024L >> c & 1L) != 0L;
   }

   public static boolean isXMLNameFirstCharacter(char c) {
      return (NAME_FIRST_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static boolean isXML11NameFirstCharacter(char c) {
      return (NAME11_FIRST_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static boolean isXMLNameCharacter(char c) {
      return (NAME_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static boolean isXML11NameCharacter(char c) {
      return (NAME11_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static boolean isXMLCharacter(int c) {
      return (XML_CHARACTER[c >>> 5] & 1 << (c & 31)) != 0 || c >= 65536 && c <= 1114111;
   }

   public static boolean isXML11Character(int c) {
      return c >= 1 && c <= 55295 || c >= 57344 && c <= 65533 || c >= 65536 && c <= 1114111;
   }

   public static boolean isXMLPublicIdCharacter(char c) {
      return c < 128 && (PUBLIC_ID_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static boolean isXMLVersionCharacter(char c) {
      return c < 128 && (VERSION_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static boolean isXMLAlphabeticCharacter(char c) {
      return c < 128 && (ALPHABETIC_CHARACTER[c / 32] & 1 << c % 32) != 0;
   }

   public static int testXMLQName(String s) {
      int isQName = 2;
      boolean foundColon = false;
      int len = s.length();
      if (len == 0) {
         return 0;
      } else {
         char c = s.charAt(0);
         if (!isXMLNameFirstCharacter(c)) {
            return 0;
         } else {
            if (c == ':') {
               isQName = 0;
            }

            for(int i = 1; i < len; ++i) {
               c = s.charAt(i);
               if (!isXMLNameCharacter(c)) {
                  return 0;
               }

               if (isQName != 0 && c == ':') {
                  if (!foundColon && i != len - 1) {
                     foundColon = true;
                  } else {
                     isQName = 0;
                  }
               }
            }

            return 1 | isQName;
         }
      }
   }

   public static Reader createXMLDocumentReader(InputStream is) throws IOException {
      PushbackInputStream pbis = new PushbackInputStream(is, 128);
      byte[] buf = new byte[4];
      int len = pbis.read(buf);
      if (len > 0) {
         pbis.unread(buf, 0, len);
      }

      if (len == 4) {
         Reader r;
         String enc;
         switch (buf[0] & 255) {
            case 0:
               if (buf[1] == 60 && buf[2] == 0 && buf[3] == 63) {
                  return new InputStreamReader(pbis, "UnicodeBig");
               }
               break;
            case 60:
               switch (buf[1] & 255) {
                  case 0:
                     if (buf[2] == 63 && buf[3] == 0) {
                        return new InputStreamReader(pbis, "UnicodeLittle");
                     }

                     return new InputStreamReader(pbis, "UTF8");
                  case 63:
                     if (buf[2] == 120 && buf[3] == 109) {
                        r = createXMLDeclarationReader(pbis, "UTF8");
                        enc = getXMLDeclarationEncoding(r, "UTF8");
                        return new InputStreamReader(pbis, enc);
                     }

                     return new InputStreamReader(pbis, "UTF8");
                  default:
                     return new InputStreamReader(pbis, "UTF8");
               }
            case 76:
               if (buf[1] == 111 && (buf[2] & 255) == 167 && (buf[3] & 255) == 148) {
                  r = createXMLDeclarationReader(pbis, "CP037");
                  enc = getXMLDeclarationEncoding(r, "CP037");
                  return new InputStreamReader(pbis, enc);
               }
               break;
            case 254:
               if ((buf[1] & 255) == 255) {
                  return new InputStreamReader(pbis, "Unicode");
               }
               break;
            case 255:
               if ((buf[1] & 255) == 254) {
                  return new InputStreamReader(pbis, "Unicode");
               }
         }
      }

      return new InputStreamReader(pbis, "UTF8");
   }

   protected static Reader createXMLDeclarationReader(PushbackInputStream pbis, String enc) throws IOException {
      byte[] buf = new byte[128];
      int len = pbis.read(buf);
      if (len > 0) {
         pbis.unread(buf, 0, len);
      }

      return new InputStreamReader(new ByteArrayInputStream(buf, 4, len), enc);
   }

   protected static String getXMLDeclarationEncoding(Reader r, String e) throws IOException {
      if (r.read() != 108) {
         return e;
      } else if (!isXMLSpace((char)r.read())) {
         return e;
      } else {
         int c;
         while(isXMLSpace((char)(c = r.read()))) {
         }

         if (c != 118) {
            return e;
         } else if (r.read() != 101) {
            return e;
         } else if (r.read() != 114) {
            return e;
         } else if (r.read() != 115) {
            return e;
         } else if (r.read() != 105) {
            return e;
         } else if (r.read() != 111) {
            return e;
         } else if (r.read() != 110) {
            return e;
         } else {
            for(c = r.read(); isXMLSpace((char)c); c = r.read()) {
            }

            if (c != 61) {
               return e;
            } else {
               while(isXMLSpace((char)(c = r.read()))) {
               }

               if (c != 34 && c != 39) {
                  return e;
               } else {
                  char sc = (char)c;

                  do {
                     c = r.read();
                     if (c == sc) {
                        if (!isXMLSpace((char)r.read())) {
                           return e;
                        } else {
                           while(isXMLSpace((char)(c = r.read()))) {
                           }

                           if (c != 101) {
                              return e;
                           } else if (r.read() != 110) {
                              return e;
                           } else if (r.read() != 99) {
                              return e;
                           } else if (r.read() != 111) {
                              return e;
                           } else if (r.read() != 100) {
                              return e;
                           } else if (r.read() != 105) {
                              return e;
                           } else if (r.read() != 110) {
                              return e;
                           } else if (r.read() != 103) {
                              return e;
                           } else {
                              for(c = r.read(); isXMLSpace((char)c); c = r.read()) {
                              }

                              if (c != 61) {
                                 return e;
                              } else {
                                 while(isXMLSpace((char)(c = r.read()))) {
                                 }

                                 if (c != 34 && c != 39) {
                                    return e;
                                 } else {
                                    sc = (char)c;
                                    StringBuffer enc = new StringBuffer();

                                    while(true) {
                                       c = r.read();
                                       if (c == -1) {
                                          return e;
                                       }

                                       if (c == sc) {
                                          return encodingToJavaEncoding(enc.toString(), e);
                                       }

                                       enc.append((char)c);
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } while(isXMLVersionCharacter((char)c));

                  return e;
               }
            }
         }
      }
   }

   public static String encodingToJavaEncoding(String e, String de) {
      String result = EncodingUtilities.javaEncoding(e);
      return result == null ? de : result;
   }
}
