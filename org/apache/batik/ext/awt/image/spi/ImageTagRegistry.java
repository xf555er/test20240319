package org.apache.batik.ext.awt.image.spi;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.batik.ext.awt.image.URLImageCache;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.renderable.ProfileRable;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Service;
import org.apache.xmlgraphics.java2d.color.ICCColorSpaceWithIntent;

public class ImageTagRegistry implements ErrorConstants {
   List entries;
   List extensions;
   List mimeTypes;
   URLImageCache rawCache;
   URLImageCache imgCache;
   static ImageTagRegistry registry = null;
   static BrokenLinkProvider defaultProvider = new DefaultBrokenLinkProvider();
   static BrokenLinkProvider brokenLinkProvider = null;

   public ImageTagRegistry() {
      this((URLImageCache)null, (URLImageCache)null);
   }

   public ImageTagRegistry(URLImageCache rawCache, URLImageCache imgCache) {
      this.entries = new LinkedList();
      this.extensions = null;
      this.mimeTypes = null;
      if (rawCache == null) {
         rawCache = new URLImageCache();
      }

      if (imgCache == null) {
         imgCache = new URLImageCache();
      }

      this.rawCache = rawCache;
      this.imgCache = imgCache;
   }

   public void flushCache() {
      this.rawCache.flush();
      this.imgCache.flush();
   }

   public void flushImage(ParsedURL purl) {
      this.rawCache.clear(purl);
      this.imgCache.clear(purl);
   }

   public Filter checkCache(ParsedURL purl, ICCColorSpaceWithIntent colorSpace) {
      boolean needRawData = colorSpace != null;
      Filter ret = null;
      URLImageCache cache;
      if (needRawData) {
         cache = this.rawCache;
      } else {
         cache = this.imgCache;
      }

      ret = cache.request(purl);
      if (ret == null) {
         cache.clear(purl);
         return null;
      } else {
         if (colorSpace != null) {
            ret = new ProfileRable((Filter)ret, colorSpace);
         }

         return (Filter)ret;
      }
   }

   public Filter readURL(ParsedURL purl) {
      return this.readURL((InputStream)null, purl, (ICCColorSpaceWithIntent)null, true, true);
   }

   public Filter readURL(ParsedURL purl, ICCColorSpaceWithIntent colorSpace) {
      return this.readURL((InputStream)null, purl, colorSpace, true, true);
   }

   public Filter readURL(InputStream is, ParsedURL purl, ICCColorSpaceWithIntent colorSpace, boolean allowOpenStream, boolean returnBrokenLink) {
      if (is != null && !((InputStream)is).markSupported()) {
         is = new BufferedInputStream((InputStream)is);
      }

      boolean needRawData = colorSpace != null;
      Filter ret = null;
      URLImageCache cache = null;
      if (purl != null) {
         if (needRawData) {
            cache = this.rawCache;
         } else {
            cache = this.imgCache;
         }

         ret = cache.request(purl);
         if (ret != null) {
            if (colorSpace != null) {
               ret = new ProfileRable((Filter)ret, colorSpace);
            }

            return (Filter)ret;
         }
      }

      boolean openFailed = false;
      List mimeTypes = this.getRegisteredMimeTypes();
      Iterator i = this.entries.iterator();

      while(i.hasNext()) {
         RegistryEntry re = (RegistryEntry)i.next();
         if (re instanceof URLRegistryEntry) {
            if (purl != null && allowOpenStream) {
               URLRegistryEntry ure = (URLRegistryEntry)re;
               if (ure.isCompatibleURL(purl)) {
                  ret = ure.handleURL(purl, needRawData);
                  if (ret != null) {
                     break;
                  }
               }
            }
         } else if (re instanceof StreamRegistryEntry) {
            StreamRegistryEntry sre = (StreamRegistryEntry)re;
            if (!openFailed) {
               try {
                  if (is == null) {
                     if (purl == null || !allowOpenStream) {
                        break;
                     }

                     try {
                        is = purl.openStream(mimeTypes.iterator());
                     } catch (IOException var15) {
                        openFailed = true;
                        continue;
                     }

                     if (!((InputStream)is).markSupported()) {
                        is = new BufferedInputStream((InputStream)is);
                     }
                  }

                  if (sre.isCompatibleStream((InputStream)is)) {
                     ret = sre.handleStream((InputStream)is, purl, needRawData);
                     if (ret != null) {
                        break;
                     }
                  }
               } catch (StreamCorruptedException var16) {
                  is = null;
               }
            }
         }
      }

      if (cache != null) {
         cache.put(purl, (Filter)ret);
      }

      if (ret == null) {
         if (!returnBrokenLink) {
            return null;
         } else {
            return openFailed ? getBrokenLinkImage(this, "url.unreachable", (Object[])null) : getBrokenLinkImage(this, "url.uninterpretable", (Object[])null);
         }
      } else if (BrokenLinkProvider.hasBrokenLinkProperty((Filter)ret)) {
         return (Filter)(returnBrokenLink ? ret : null);
      } else {
         if (colorSpace != null) {
            ret = new ProfileRable((Filter)ret, colorSpace);
         }

         return (Filter)ret;
      }
   }

   public Filter readStream(InputStream is) {
      return this.readStream(is, (ICCColorSpaceWithIntent)null);
   }

   public Filter readStream(InputStream is, ICCColorSpaceWithIntent colorSpace) {
      if (!((InputStream)is).markSupported()) {
         is = new BufferedInputStream((InputStream)is);
      }

      boolean needRawData = colorSpace != null;
      Filter ret = null;
      Iterator var5 = this.entries.iterator();

      while(var5.hasNext()) {
         Object entry = var5.next();
         RegistryEntry re = (RegistryEntry)entry;
         if (re instanceof StreamRegistryEntry) {
            StreamRegistryEntry sre = (StreamRegistryEntry)re;

            try {
               if (sre.isCompatibleStream((InputStream)is)) {
                  ret = sre.handleStream((InputStream)is, (ParsedURL)null, needRawData);
                  if (ret != null) {
                     break;
                  }
               }
            } catch (StreamCorruptedException var10) {
               break;
            }
         }
      }

      if (ret == null) {
         return getBrokenLinkImage(this, "stream.unreadable", (Object[])null);
      } else {
         if (colorSpace != null && !BrokenLinkProvider.hasBrokenLinkProperty((Filter)ret)) {
            ret = new ProfileRable((Filter)ret, colorSpace);
         }

         return (Filter)ret;
      }
   }

   public synchronized void register(RegistryEntry newRE) {
      float priority = newRE.getPriority();
      ListIterator li = this.entries.listIterator();

      while(li.hasNext()) {
         RegistryEntry re = (RegistryEntry)li.next();
         if (re.getPriority() > priority) {
            li.previous();
            break;
         }
      }

      li.add(newRE);
      this.extensions = null;
      this.mimeTypes = null;
   }

   public synchronized List getRegisteredExtensions() {
      if (this.extensions != null) {
         return this.extensions;
      } else {
         this.extensions = new LinkedList();
         Iterator var1 = this.entries.iterator();

         while(var1.hasNext()) {
            Object entry = var1.next();
            RegistryEntry re = (RegistryEntry)entry;
            this.extensions.addAll(re.getStandardExtensions());
         }

         this.extensions = Collections.unmodifiableList(this.extensions);
         return this.extensions;
      }
   }

   public synchronized List getRegisteredMimeTypes() {
      if (this.mimeTypes != null) {
         return this.mimeTypes;
      } else {
         this.mimeTypes = new LinkedList();
         Iterator var1 = this.entries.iterator();

         while(var1.hasNext()) {
            Object entry = var1.next();
            RegistryEntry re = (RegistryEntry)entry;
            this.mimeTypes.addAll(re.getMimeTypes());
         }

         this.mimeTypes = Collections.unmodifiableList(this.mimeTypes);
         return this.mimeTypes;
      }
   }

   public static synchronized ImageTagRegistry getRegistry() {
      if (registry != null) {
         return registry;
      } else {
         registry = new ImageTagRegistry();
         registry.register(new JDKRegistryEntry());
         Iterator iter = Service.providers(RegistryEntry.class);

         while(iter.hasNext()) {
            RegistryEntry re = (RegistryEntry)iter.next();
            registry.register(re);
         }

         return registry;
      }
   }

   public static synchronized Filter getBrokenLinkImage(Object base, String code, Object[] params) {
      Filter ret = null;
      if (brokenLinkProvider != null) {
         ret = brokenLinkProvider.getBrokenLinkImage(base, code, params);
      }

      if (ret == null) {
         ret = defaultProvider.getBrokenLinkImage(base, code, params);
      }

      return ret;
   }

   public static synchronized void setBrokenLinkProvider(BrokenLinkProvider provider) {
      brokenLinkProvider = provider;
   }
}
