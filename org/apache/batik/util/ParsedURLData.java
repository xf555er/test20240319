package org.apache.batik.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class ParsedURLData {
   protected static final String HTTP_USER_AGENT_HEADER = "User-Agent";
   protected static final String HTTP_ACCEPT_HEADER = "Accept";
   protected static final String HTTP_ACCEPT_LANGUAGE_HEADER = "Accept-Language";
   protected static final String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";
   protected static List acceptedEncodings = new LinkedList();
   public static final byte[] GZIP_MAGIC;
   public String protocol = null;
   public String host = null;
   public int port = -1;
   public String path = null;
   public String ref = null;
   public String contentType = null;
   public String contentEncoding = null;
   public InputStream stream = null;
   public boolean hasBeenOpened = false;
   protected String contentTypeMediaType;
   protected String contentTypeCharset;
   protected URL postConnectionURL;

   public static InputStream checkGZIP(InputStream is) throws IOException {
      if (!((InputStream)is).markSupported()) {
         is = new BufferedInputStream((InputStream)is);
      }

      byte[] data = new byte[2];

      try {
         ((InputStream)is).mark(2);
         ((InputStream)is).read(data);
         ((InputStream)is).reset();
      } catch (Exception var5) {
         ((InputStream)is).reset();
         return (InputStream)is;
      }

      if (data[0] == GZIP_MAGIC[0] && data[1] == GZIP_MAGIC[1]) {
         return new GZIPInputStream((InputStream)is);
      } else {
         if ((data[0] & 15) == 8 && data[0] >>> 4 <= 7) {
            int chk = (data[0] & 255) * 256 + (data[1] & 255);
            if (chk % 31 == 0) {
               try {
                  ((InputStream)is).mark(100);
                  InputStream ret = new InflaterInputStream((InputStream)is);
                  if (!((InputStream)ret).markSupported()) {
                     ret = new BufferedInputStream((InputStream)ret);
                  }

                  ((InputStream)ret).mark(2);
                  ((InputStream)ret).read(data);
                  ((InputStream)is).reset();
                  InputStream ret = new InflaterInputStream((InputStream)is);
                  return ret;
               } catch (ZipException var4) {
                  ((InputStream)is).reset();
                  return (InputStream)is;
               }
            }
         }

         return (InputStream)is;
      }
   }

   public ParsedURLData() {
   }

   public ParsedURLData(URL url) {
      this.protocol = url.getProtocol();
      if (this.protocol != null && this.protocol.length() == 0) {
         this.protocol = null;
      }

      this.host = url.getHost();
      if (this.host != null && this.host.length() == 0) {
         this.host = null;
      }

      this.port = url.getPort();
      this.path = url.getFile();
      if (this.path != null && this.path.length() == 0) {
         this.path = null;
      }

      this.ref = url.getRef();
      if (this.ref != null && this.ref.length() == 0) {
         this.ref = null;
      }

   }

   protected URL buildURL() throws MalformedURLException {
      if (this.protocol != null && this.host != null) {
         String file = "";
         if (this.path != null) {
            file = this.path;
         }

         return this.port == -1 ? new URL(this.protocol, this.host, file) : new URL(this.protocol, this.host, this.port, file);
      } else {
         return new URL(this.toString());
      }
   }

   public int hashCode() {
      int hc = this.port;
      if (this.protocol != null) {
         hc ^= this.protocol.hashCode();
      }

      if (this.host != null) {
         hc ^= this.host.hashCode();
      }

      int len;
      if (this.path != null) {
         len = this.path.length();
         if (len > 20) {
            hc ^= this.path.substring(len - 20).hashCode();
         } else {
            hc ^= this.path.hashCode();
         }
      }

      if (this.ref != null) {
         len = this.ref.length();
         if (len > 20) {
            hc ^= this.ref.substring(len - 20).hashCode();
         } else {
            hc ^= this.ref.hashCode();
         }
      }

      return hc;
   }

   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (!(obj instanceof ParsedURLData)) {
         return false;
      } else {
         ParsedURLData ud = (ParsedURLData)obj;
         if (ud.port != this.port) {
            return false;
         } else {
            if (ud.protocol == null) {
               if (this.protocol != null) {
                  return false;
               }
            } else {
               if (this.protocol == null) {
                  return false;
               }

               if (!ud.protocol.equals(this.protocol)) {
                  return false;
               }
            }

            if (ud.host == null) {
               if (this.host != null) {
                  return false;
               }
            } else {
               if (this.host == null) {
                  return false;
               }

               if (!ud.host.equals(this.host)) {
                  return false;
               }
            }

            if (ud.ref == null) {
               if (this.ref != null) {
                  return false;
               }
            } else {
               if (this.ref == null) {
                  return false;
               }

               if (!ud.ref.equals(this.ref)) {
                  return false;
               }
            }

            if (ud.path == null) {
               if (this.path != null) {
                  return false;
               }
            } else {
               if (this.path == null) {
                  return false;
               }

               if (!ud.path.equals(this.path)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public String getContentType(String userAgent) {
      if (this.contentType != null) {
         return this.contentType;
      } else {
         if (!this.hasBeenOpened) {
            try {
               this.openStreamInternal(userAgent, (Iterator)null, (Iterator)null);
            } catch (IOException var3) {
            }
         }

         return this.contentType;
      }
   }

   public String getContentTypeMediaType(String userAgent) {
      if (this.contentTypeMediaType != null) {
         return this.contentTypeMediaType;
      } else {
         this.extractContentTypeParts(userAgent);
         return this.contentTypeMediaType;
      }
   }

   public String getContentTypeCharset(String userAgent) {
      if (this.contentTypeMediaType != null) {
         return this.contentTypeCharset;
      } else {
         this.extractContentTypeParts(userAgent);
         return this.contentTypeCharset;
      }
   }

   public boolean hasContentTypeParameter(String userAgent, String param) {
      this.getContentType(userAgent);
      if (this.contentType == null) {
         return false;
      } else {
         int i = 0;
         int len = this.contentType.length();
         int plen = param.length();

         label68:
         while(i < len) {
            switch (this.contentType.charAt(i)) {
               case ' ':
               case ';':
                  break label68;
               default:
                  ++i;
            }
         }

         if (i == len) {
            this.contentTypeMediaType = this.contentType;
         } else {
            this.contentTypeMediaType = this.contentType.substring(0, i);
         }

         while(true) {
            label58:
            while(i >= len || this.contentType.charAt(i) == ';') {
               if (i == len) {
                  return false;
               }

               ++i;

               while(i < len && this.contentType.charAt(i) == ' ') {
                  ++i;
               }

               if (i >= len - plen - 1) {
                  return false;
               }

               for(int j = 0; j < plen; ++j) {
                  if (this.contentType.charAt(i++) != param.charAt(j)) {
                     continue label58;
                  }
               }

               if (this.contentType.charAt(i) == '=') {
                  return true;
               }
            }

            ++i;
         }
      }
   }

   protected void extractContentTypeParts(String userAgent) {
      this.getContentType(userAgent);
      if (this.contentType != null) {
         int i = 0;
         int len = this.contentType.length();

         label92:
         while(i < len) {
            switch (this.contentType.charAt(i)) {
               case ' ':
               case ';':
                  break label92;
               default:
                  ++i;
            }
         }

         if (i == len) {
            this.contentTypeMediaType = this.contentType;
         } else {
            this.contentTypeMediaType = this.contentType.substring(0, i);
         }

         while(true) {
            while(i >= len || this.contentType.charAt(i) == ';') {
               if (i == len) {
                  return;
               }

               ++i;

               while(i < len && this.contentType.charAt(i) == ' ') {
                  ++i;
               }

               if (i >= len - 8) {
                  return;
               }

               if (this.contentType.charAt(i++) == 'c' && this.contentType.charAt(i++) == 'h' && this.contentType.charAt(i++) == 'a' && this.contentType.charAt(i++) == 'r' && this.contentType.charAt(i++) == 's' && this.contentType.charAt(i++) == 'e' && this.contentType.charAt(i++) == 't' && this.contentType.charAt(i++) == '=') {
                  int j = i;

                  label58:
                  while(i < len) {
                     switch (this.contentType.charAt(i)) {
                        case ' ':
                        case ';':
                           break label58;
                        default:
                           ++i;
                     }
                  }

                  this.contentTypeCharset = this.contentType.substring(j, i);
                  return;
               }
            }

            ++i;
         }
      }
   }

   public String getContentEncoding(String userAgent) {
      if (this.contentEncoding != null) {
         return this.contentEncoding;
      } else {
         if (!this.hasBeenOpened) {
            try {
               this.openStreamInternal(userAgent, (Iterator)null, (Iterator)null);
            } catch (IOException var3) {
            }
         }

         return this.contentEncoding;
      }
   }

   public boolean complete() {
      try {
         this.buildURL();
         return true;
      } catch (MalformedURLException var2) {
         return false;
      }
   }

   public InputStream openStream(String userAgent, Iterator mimeTypes) throws IOException {
      InputStream raw = this.openStreamInternal(userAgent, mimeTypes, acceptedEncodings.iterator());
      if (raw == null) {
         return null;
      } else {
         this.stream = null;
         return checkGZIP(raw);
      }
   }

   public InputStream openStreamRaw(String userAgent, Iterator mimeTypes) throws IOException {
      InputStream ret = this.openStreamInternal(userAgent, mimeTypes, (Iterator)null);
      this.stream = null;
      return ret;
   }

   protected InputStream openStreamInternal(String userAgent, Iterator mimeTypes, Iterator encodingTypes) throws IOException {
      if (this.stream != null) {
         return this.stream;
      } else {
         this.hasBeenOpened = true;
         URL url = null;

         try {
            url = this.buildURL();
         } catch (MalformedURLException var7) {
            throw new IOException("Unable to make sense of URL for connection");
         }

         if (url == null) {
            return null;
         } else {
            URLConnection urlC = url.openConnection();
            if (urlC instanceof HttpURLConnection) {
               if (userAgent != null) {
                  urlC.setRequestProperty("User-Agent", userAgent);
               }

               String encodingHeader;
               if (mimeTypes != null) {
                  encodingHeader = "";

                  while(mimeTypes.hasNext()) {
                     encodingHeader = encodingHeader + mimeTypes.next();
                     if (mimeTypes.hasNext()) {
                        encodingHeader = encodingHeader + ",";
                     }
                  }

                  urlC.setRequestProperty("Accept", encodingHeader);
               }

               if (encodingTypes != null) {
                  encodingHeader = "";

                  while(encodingTypes.hasNext()) {
                     encodingHeader = encodingHeader + encodingTypes.next();
                     if (encodingTypes.hasNext()) {
                        encodingHeader = encodingHeader + ",";
                     }
                  }

                  urlC.setRequestProperty("Accept-Encoding", encodingHeader);
               }

               this.contentType = urlC.getContentType();
               this.contentEncoding = urlC.getContentEncoding();
               this.postConnectionURL = urlC.getURL();
            }

            try {
               return this.stream = urlC.getInputStream();
            } catch (IOException var8) {
               if (urlC instanceof HttpURLConnection) {
                  this.stream = ((HttpURLConnection)urlC).getErrorStream();
                  if (this.stream == null) {
                     throw var8;
                  } else {
                     return this.stream;
                  }
               } else {
                  throw var8;
               }
            }
         }
      }
   }

   public String getPortStr() {
      String portStr = "";
      if (this.protocol != null) {
         portStr = portStr + this.protocol + ":";
      }

      if (this.host != null || this.port != -1) {
         portStr = portStr + "//";
         if (this.host != null) {
            portStr = portStr + this.host;
         }

         if (this.port != -1) {
            portStr = portStr + ":" + this.port;
         }
      }

      return portStr;
   }

   protected boolean sameFile(ParsedURLData other) {
      if (this == other) {
         return true;
      } else {
         return this.port == other.port && (this.path == other.path || this.path != null && this.path.equals(other.path)) && (this.host == other.host || this.host != null && this.host.equals(other.host)) && (this.protocol == other.protocol || this.protocol != null && this.protocol.equals(other.protocol));
      }
   }

   public String toString() {
      String ret = this.getPortStr();
      if (this.path != null) {
         ret = ret + this.path;
      }

      if (this.ref != null) {
         ret = ret + "#" + this.ref;
      }

      return ret;
   }

   public String getPostConnectionURL() {
      if (this.postConnectionURL != null) {
         return this.ref != null ? this.postConnectionURL.toString() + '#' + this.ref : this.postConnectionURL.toString();
      } else {
         return this.toString();
      }
   }

   static {
      acceptedEncodings.add("gzip");
      GZIP_MAGIC = new byte[]{31, -117};
   }
}
