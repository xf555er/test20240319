package org.apache.batik.util;

import java.net.MalformedURLException;
import java.net.URL;

public class ParsedURLDefaultProtocolHandler extends AbstractParsedURLProtocolHandler {
   public ParsedURLDefaultProtocolHandler() {
      super((String)null);
   }

   protected ParsedURLDefaultProtocolHandler(String protocol) {
      super(protocol);
   }

   protected ParsedURLData constructParsedURLData() {
      return new ParsedURLData();
   }

   protected ParsedURLData constructParsedURLData(URL url) {
      return new ParsedURLData(url);
   }

   public ParsedURLData parseURL(String urlStr) {
      try {
         URL url = new URL(urlStr);
         return this.constructParsedURLData(url);
      } catch (MalformedURLException var12) {
         ParsedURLData ret = this.constructParsedURLData();
         if (urlStr == null) {
            return ret;
         } else {
            int pidx = 0;
            int len = urlStr.length();
            int idx = urlStr.indexOf(35);
            ret.ref = null;
            if (idx != -1) {
               if (idx + 1 < len) {
                  ret.ref = urlStr.substring(idx + 1);
               }

               urlStr = urlStr.substring(0, idx);
               len = urlStr.length();
            }

            if (len == 0) {
               return ret;
            } else {
               idx = 0;

               char ch;
               for(ch = urlStr.charAt(idx); ch == '-' || ch == '+' || ch == '.' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'; ch = urlStr.charAt(idx)) {
                  ++idx;
                  if (idx == len) {
                     ch = 0;
                     break;
                  }
               }

               if (ch == ':') {
                  ret.protocol = urlStr.substring(pidx, idx).toLowerCase();
                  pidx = idx + 1;
               }

               idx = urlStr.indexOf(47);
               if (idx == -1 || pidx + 2 < len && urlStr.charAt(pidx) == '/' && urlStr.charAt(pidx + 1) == '/') {
                  if (idx != -1) {
                     pidx += 2;
                  }

                  idx = urlStr.indexOf(47, pidx);
                  String hostPort;
                  if (idx == -1) {
                     hostPort = urlStr.substring(pidx);
                  } else {
                     hostPort = urlStr.substring(pidx, idx);
                  }

                  int hidx = idx;
                  idx = hostPort.indexOf(58);
                  ret.port = -1;
                  if (idx == -1) {
                     if (hostPort.length() == 0) {
                        ret.host = null;
                     } else {
                        ret.host = hostPort;
                     }
                  } else {
                     if (idx == 0) {
                        ret.host = null;
                     } else {
                        ret.host = hostPort.substring(0, idx);
                     }

                     if (idx + 1 < hostPort.length()) {
                        String portStr = hostPort.substring(idx + 1);

                        try {
                           ret.port = Integer.parseInt(portStr);
                        } catch (NumberFormatException var11) {
                        }
                     }
                  }

                  if ((ret.host == null || ret.host.indexOf(46) == -1) && ret.port == -1) {
                     ret.host = null;
                  } else {
                     pidx = hidx;
                  }
               }

               if (pidx != -1 && pidx < len) {
                  ret.path = urlStr.substring(pidx);
                  return ret;
               } else {
                  return ret;
               }
            }
         }
      }
   }

   public static String unescapeStr(String str) {
      int idx = str.indexOf(37);
      if (idx == -1) {
         return str;
      } else {
         int prev = 0;
         StringBuffer ret = new StringBuffer();

         while(idx != -1) {
            if (idx != prev) {
               ret.append(str.substring(prev, idx));
            }

            if (idx + 2 >= str.length()) {
               break;
            }

            prev = idx + 3;
            idx = str.indexOf(37, prev);
            int ch1 = charToHex(str.charAt(idx + 1));
            int ch2 = charToHex(str.charAt(idx + 1));
            if (ch1 != -1 && ch2 != -1) {
               ret.append((char)(ch1 << 4 | ch2));
            }
         }

         return ret.toString();
      }
   }

   public static int charToHex(int ch) {
      switch (ch) {
         case 48:
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
            return ch - 48;
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 71:
         case 72:
         case 73:
         case 74:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 83:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 90:
         case 91:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         default:
            return -1;
         case 65:
         case 97:
            return 10;
         case 66:
         case 98:
            return 11;
         case 67:
         case 99:
            return 12;
         case 68:
         case 100:
            return 13;
         case 69:
         case 101:
            return 14;
         case 70:
         case 102:
            return 15;
      }
   }

   public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
      if (urlStr.length() == 0) {
         return baseURL.data;
      } else {
         int idx = 0;
         int len = urlStr.length();
         if (len == 0) {
            return baseURL.data;
         } else {
            char ch;
            for(ch = urlStr.charAt(idx); ch == '-' || ch == '+' || ch == '.' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'; ch = urlStr.charAt(idx)) {
               ++idx;
               if (idx == len) {
                  ch = 0;
                  break;
               }
            }

            String protocol = null;
            if (ch == ':') {
               protocol = urlStr.substring(0, idx).toLowerCase();
            }

            if (protocol != null) {
               if (!protocol.equals(baseURL.getProtocol())) {
                  return this.parseURL(urlStr);
               }

               ++idx;
               if (idx == urlStr.length()) {
                  return this.parseURL(urlStr);
               }

               if (urlStr.charAt(idx) == '/') {
                  return this.parseURL(urlStr);
               }

               urlStr = urlStr.substring(idx);
            }

            if (urlStr.startsWith("/")) {
               return urlStr.length() > 1 && urlStr.charAt(1) == '/' ? this.parseURL(baseURL.getProtocol() + ":" + urlStr) : this.parseURL(baseURL.getPortStr() + urlStr);
            } else {
               String path;
               if (urlStr.startsWith("#")) {
                  path = baseURL.getPortStr();
                  if (baseURL.getPath() != null) {
                     path = path + baseURL.getPath();
                  }

                  return this.parseURL(path + urlStr);
               } else {
                  path = baseURL.getPath();
                  if (path == null) {
                     path = "";
                  }

                  idx = path.lastIndexOf(47);
                  if (idx == -1) {
                     path = "";
                  } else {
                     path = path.substring(0, idx + 1);
                     if (urlStr.startsWith(path)) {
                        urlStr = urlStr.substring(path.length());
                     }
                  }

                  return this.parseURL(baseURL.getPortStr() + path + urlStr);
               }
            }
         }
      }
   }
}
