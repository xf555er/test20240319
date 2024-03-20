package org.apache.batik.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ParsedURLDataProtocolHandler extends AbstractParsedURLProtocolHandler {
   static final String DATA_PROTOCOL = "data";
   static final String BASE64 = "base64";
   static final String CHARSET = "charset";

   public ParsedURLDataProtocolHandler() {
      super("data");
   }

   public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
      return this.parseURL(urlStr);
   }

   public ParsedURLData parseURL(String urlStr) {
      DataParsedURLData ret = new DataParsedURLData();
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

      idx = urlStr.indexOf(58);
      if (idx != -1) {
         ret.protocol = urlStr.substring(pidx, idx);
         if (ret.protocol.indexOf(47) == -1) {
            pidx = idx + 1;
         } else {
            ret.protocol = null;
            pidx = 0;
         }
      }

      idx = urlStr.indexOf(44, pidx);
      if (idx != -1 && idx != pidx) {
         ret.host = urlStr.substring(pidx, idx);
         pidx = idx + 1;
         int aidx = ret.host.lastIndexOf(59);
         if (aidx != -1 && aidx != ret.host.length()) {
            String enc = ret.host.substring(aidx + 1);
            idx = enc.indexOf(61);
            if (idx == -1) {
               ret.contentEncoding = enc;
               ret.contentType = ret.host.substring(0, aidx);
            } else {
               ret.contentType = ret.host;
            }

            int aidx = 0;
            idx = ret.contentType.indexOf(59, aidx);
            if (idx != -1) {
               for(aidx = idx + 1; aidx < ret.contentType.length(); aidx = idx + 1) {
                  idx = ret.contentType.indexOf(59, aidx);
                  if (idx == -1) {
                     idx = ret.contentType.length();
                  }

                  String param = ret.contentType.substring(aidx, idx);
                  int eqIdx = param.indexOf(61);
                  if (eqIdx != -1 && "charset".equals(param.substring(0, eqIdx))) {
                     ret.charset = param.substring(eqIdx + 1);
                  }
               }
            }
         } else {
            ret.contentType = ret.host;
         }
      }

      if (pidx < urlStr.length()) {
         ret.path = urlStr.substring(pidx);
      }

      return ret;
   }

   static class DataParsedURLData extends ParsedURLData {
      String charset;

      public boolean complete() {
         return this.path != null;
      }

      public String getPortStr() {
         String portStr = "data:";
         if (this.host != null) {
            portStr = portStr + this.host;
         }

         portStr = portStr + ",";
         return portStr;
      }

      public String toString() {
         String ret = this.getPortStr();
         if (this.path != null) {
            ret = ret + this.path;
         }

         if (this.ref != null) {
            ret = ret + '#' + this.ref;
         }

         return ret;
      }

      public String getContentType(String userAgent) {
         return this.contentType;
      }

      public String getContentEncoding(String userAgent) {
         return this.contentEncoding;
      }

      protected InputStream openStreamInternal(String userAgent, Iterator mimeTypes, Iterator encodingTypes) throws IOException {
         this.stream = decode(this.path);
         if ("base64".equals(this.contentEncoding)) {
            this.stream = new Base64DecodeStream(this.stream);
         }

         return this.stream;
      }

      public static InputStream decode(String s) {
         int len = s.length();
         byte[] data = new byte[len];
         int j = 0;

         for(int i = 0; i < len; ++i) {
            char c = s.charAt(i);
            switch (c) {
               case '%':
                  if (i + 2 >= len) {
                     break;
                  }

                  i += 2;
                  char c1 = s.charAt(i - 1);
                  byte b;
                  if (c1 >= '0' && c1 <= '9') {
                     b = (byte)(c1 - 48);
                  } else if (c1 >= 'a' && c1 <= 'z') {
                     b = (byte)(c1 - 97 + 10);
                  } else {
                     if (c1 < 'A' || c1 > 'Z') {
                        break;
                     }

                     b = (byte)(c1 - 65 + 10);
                  }

                  b = (byte)(b * 16);
                  char c2 = s.charAt(i);
                  if (c2 >= '0' && c2 <= '9') {
                     b += (byte)(c2 - 48);
                  } else if (c2 >= 'a' && c2 <= 'z') {
                     b += (byte)(c2 - 97 + 10);
                  } else {
                     if (c2 < 'A' || c2 > 'Z') {
                        break;
                     }

                     b += (byte)(c2 - 65 + 10);
                  }

                  data[j++] = b;
                  break;
               default:
                  data[j++] = (byte)c;
            }
         }

         return new ByteArrayInputStream(data, 0, j);
      }
   }
}
