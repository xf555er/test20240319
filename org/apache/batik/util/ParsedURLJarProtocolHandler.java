package org.apache.batik.util;

import java.net.MalformedURLException;
import java.net.URL;

public class ParsedURLJarProtocolHandler extends ParsedURLDefaultProtocolHandler {
   public static final String JAR = "jar";

   public ParsedURLJarProtocolHandler() {
      super("jar");
   }

   public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
      String start = urlStr.substring(0, "jar".length() + 1).toLowerCase();
      if (start.equals("jar:")) {
         return this.parseURL(urlStr);
      } else {
         try {
            URL context = new URL(baseURL.toString());
            URL url = new URL(context, urlStr);
            return this.constructParsedURLData(url);
         } catch (MalformedURLException var6) {
            return super.parseURL(baseURL, urlStr);
         }
      }
   }
}
