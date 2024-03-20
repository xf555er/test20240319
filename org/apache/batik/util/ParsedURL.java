package org.apache.batik.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.batik.Version;

public class ParsedURL {
   ParsedURLData data;
   String userAgent;
   private static Map handlersMap = null;
   private static ParsedURLProtocolHandler defaultHandler = new ParsedURLDefaultProtocolHandler();
   private static String globalUserAgent = "Batik/" + Version.getVersion();

   public static String getGlobalUserAgent() {
      return globalUserAgent;
   }

   public static void setGlobalUserAgent(String userAgent) {
      globalUserAgent = userAgent;
   }

   private static synchronized Map getHandlersMap() {
      if (handlersMap != null) {
         return handlersMap;
      } else {
         handlersMap = new HashMap();
         registerHandler(new ParsedURLDataProtocolHandler());
         registerHandler(new ParsedURLJarProtocolHandler());
         Iterator iter = Service.providers(ParsedURLProtocolHandler.class);

         while(iter.hasNext()) {
            ParsedURLProtocolHandler handler = (ParsedURLProtocolHandler)iter.next();
            registerHandler(handler);
         }

         return handlersMap;
      }
   }

   public static synchronized ParsedURLProtocolHandler getHandler(String protocol) {
      if (protocol == null) {
         return defaultHandler;
      } else {
         Map handlers = getHandlersMap();
         ParsedURLProtocolHandler ret = (ParsedURLProtocolHandler)handlers.get(protocol);
         if (ret == null) {
            ret = defaultHandler;
         }

         return ret;
      }
   }

   public static synchronized void registerHandler(ParsedURLProtocolHandler handler) {
      if (handler.getProtocolHandled() == null) {
         defaultHandler = handler;
      } else {
         Map handlers = getHandlersMap();
         handlers.put(handler.getProtocolHandled(), handler);
      }
   }

   public static InputStream checkGZIP(InputStream is) throws IOException {
      return ParsedURLData.checkGZIP(is);
   }

   public ParsedURL(String urlStr) {
      this.userAgent = getGlobalUserAgent();
      this.data = parseURL(urlStr);
   }

   public ParsedURL(URL url) {
      this.userAgent = getGlobalUserAgent();
      this.data = new ParsedURLData(url);
   }

   public ParsedURL(String baseStr, String urlStr) {
      this.userAgent = getGlobalUserAgent();
      if (baseStr != null) {
         this.data = parseURL(baseStr, urlStr);
      } else {
         this.data = parseURL(urlStr);
      }

   }

   public ParsedURL(URL baseURL, String urlStr) {
      this.userAgent = getGlobalUserAgent();
      if (baseURL != null) {
         this.data = parseURL(new ParsedURL(baseURL), urlStr);
      } else {
         this.data = parseURL(urlStr);
      }

   }

   public ParsedURL(ParsedURL baseURL, String urlStr) {
      if (baseURL != null) {
         this.userAgent = baseURL.getUserAgent();
         this.data = parseURL(baseURL, urlStr);
      } else {
         this.data = parseURL(urlStr);
      }

   }

   public String toString() {
      return this.data.toString();
   }

   public String getPostConnectionURL() {
      return this.data.getPostConnectionURL();
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof ParsedURL)) {
         return false;
      } else {
         ParsedURL purl = (ParsedURL)obj;
         return this.data.equals(purl.data);
      }
   }

   public int hashCode() {
      return this.data.hashCode();
   }

   public boolean complete() {
      return this.data.complete();
   }

   public String getUserAgent() {
      return this.userAgent;
   }

   public void setUserAgent(String userAgent) {
      this.userAgent = userAgent;
   }

   public String getProtocol() {
      return this.data.protocol == null ? null : this.data.protocol;
   }

   public String getHost() {
      return this.data.host == null ? null : this.data.host;
   }

   public int getPort() {
      return this.data.port;
   }

   public String getPath() {
      return this.data.path == null ? null : this.data.path;
   }

   public String getRef() {
      return this.data.ref == null ? null : this.data.ref;
   }

   public String getPortStr() {
      return this.data.getPortStr();
   }

   public String getContentType() {
      return this.data.getContentType(this.userAgent);
   }

   public String getContentTypeMediaType() {
      return this.data.getContentTypeMediaType(this.userAgent);
   }

   public String getContentTypeCharset() {
      return this.data.getContentTypeCharset(this.userAgent);
   }

   public boolean hasContentTypeParameter(String param) {
      return this.data.hasContentTypeParameter(this.userAgent, param);
   }

   public String getContentEncoding() {
      return this.data.getContentEncoding(this.userAgent);
   }

   public InputStream openStream() throws IOException {
      return this.data.openStream(this.userAgent, (Iterator)null);
   }

   public InputStream openStream(String mimeType) throws IOException {
      List mt = new ArrayList(1);
      mt.add(mimeType);
      return this.data.openStream(this.userAgent, mt.iterator());
   }

   public InputStream openStream(String[] mimeTypes) throws IOException {
      List mt = new ArrayList(mimeTypes.length);
      String[] var3 = mimeTypes;
      int var4 = mimeTypes.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String mimeType = var3[var5];
         mt.add(mimeType);
      }

      return this.data.openStream(this.userAgent, mt.iterator());
   }

   public InputStream openStream(Iterator mimeTypes) throws IOException {
      return this.data.openStream(this.userAgent, mimeTypes);
   }

   public InputStream openStreamRaw() throws IOException {
      return this.data.openStreamRaw(this.userAgent, (Iterator)null);
   }

   public InputStream openStreamRaw(String mimeType) throws IOException {
      List mt = new ArrayList(1);
      mt.add(mimeType);
      return this.data.openStreamRaw(this.userAgent, mt.iterator());
   }

   public InputStream openStreamRaw(String[] mimeTypes) throws IOException {
      List mt = new ArrayList(mimeTypes.length);
      mt.addAll(Arrays.asList(mimeTypes));
      return this.data.openStreamRaw(this.userAgent, mt.iterator());
   }

   public InputStream openStreamRaw(Iterator mimeTypes) throws IOException {
      return this.data.openStreamRaw(this.userAgent, mimeTypes);
   }

   public boolean sameFile(ParsedURL other) {
      return this.data.sameFile(other.data);
   }

   protected static String getProtocol(String urlStr) {
      if (urlStr == null) {
         return null;
      } else {
         int idx = 0;
         int len = urlStr.length();
         if (len == 0) {
            return null;
         } else {
            char ch;
            for(ch = urlStr.charAt(idx); ch == '-' || ch == '+' || ch == '.' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z'; ch = urlStr.charAt(idx)) {
               ++idx;
               if (idx == len) {
                  ch = 0;
                  break;
               }
            }

            return ch == ':' ? urlStr.substring(0, idx).toLowerCase() : null;
         }
      }
   }

   public static ParsedURLData parseURL(String urlStr) {
      if (urlStr != null && !urlStr.contains(":") && !urlStr.startsWith("#")) {
         urlStr = "file:" + urlStr;
      }

      ParsedURLProtocolHandler handler = getHandler(getProtocol(urlStr));
      return handler.parseURL(urlStr);
   }

   public static ParsedURLData parseURL(String baseStr, String urlStr) {
      if (baseStr == null) {
         return parseURL(urlStr);
      } else {
         ParsedURL purl = new ParsedURL(baseStr);
         return parseURL(purl, urlStr);
      }
   }

   public static ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
      if (baseURL == null) {
         return parseURL(urlStr);
      } else {
         String protocol = getProtocol(urlStr);
         if (protocol == null) {
            protocol = baseURL.getProtocol();
         }

         ParsedURLProtocolHandler handler = getHandler(protocol);
         return handler.parseURL(baseURL, urlStr);
      }
   }
}
