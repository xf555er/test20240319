package org.apache.fop.fonts;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.util.LogUtil;

public final class FontCache implements Serializable {
   private static final long serialVersionUID = 9129238336422194339L;
   private static Log log = LogFactory.getLog(FontCache.class);
   private static final String FOP_USER_DIR = ".fop";
   private static final String DEFAULT_CACHE_FILENAME = "fop-fonts.cache";
   private transient boolean changed;
   private final boolean[] changeLock = new boolean[1];
   private Map fontfileMap;
   private Map failedFontMap;

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }

   private static File getUserHome() {
      return toDirectory(System.getProperty("user.home"));
   }

   private static File getTempDirectory() {
      return toDirectory(System.getProperty("java.io.tmpdir"));
   }

   private static File toDirectory(String path) {
      if (path != null) {
         File dir = new File(path);
         if (dir.exists()) {
            return dir;
         }
      }

      return null;
   }

   public static File getDefaultCacheFile(boolean forWriting) {
      File userHome = getUserHome();
      if (userHome != null) {
         File fopUserDir = new File(userHome, ".fop");
         if (forWriting) {
            boolean writable = fopUserDir.canWrite();
            if (!fopUserDir.exists()) {
               writable = fopUserDir.mkdir();
            }

            if (!writable) {
               userHome = getTempDirectory();
               fopUserDir = new File(userHome, ".fop");
               fopUserDir.mkdir();
            }
         }

         return new File(fopUserDir, "fop-fonts.cache");
      } else {
         return new File(".fop");
      }
   }

   /** @deprecated */
   public static FontCache load() {
      return loadFrom(getDefaultCacheFile(false));
   }

   public static FontCache loadFrom(File cacheFile) {
      if (cacheFile.exists()) {
         try {
            if (log.isTraceEnabled()) {
               log.trace("Loading font cache from " + cacheFile.getCanonicalPath());
            }

            InputStream in = new BufferedInputStream(new FileInputStream(cacheFile));
            ObjectInputStream oin = new ObjectInputStream(in);

            FontCache var3;
            try {
               var3 = (FontCache)oin.readObject();
            } finally {
               IOUtils.closeQuietly((InputStream)oin);
            }

            return var3;
         } catch (ClassNotFoundException var11) {
            log.warn("Could not read font cache. Discarding font cache file. Reason: " + var11.getMessage());
         } catch (IOException var12) {
            log.warn("I/O exception while reading font cache (" + var12.getMessage() + "). Discarding font cache file.");

            try {
               cacheFile.delete();
            } catch (SecurityException var9) {
               log.warn("Failed to delete font cache file: " + cacheFile.getAbsolutePath());
            }
         }
      }

      return null;
   }

   /** @deprecated */
   public void save() throws FOPException {
      this.saveTo(getDefaultCacheFile(true));
   }

   public void saveTo(File cacheFile) throws FOPException {
      synchronized(this.changeLock) {
         if (this.changed) {
            try {
               log.trace("Writing font cache to " + cacheFile.getCanonicalPath());
               OutputStream out = new FileOutputStream(cacheFile);
               OutputStream out = new BufferedOutputStream(out);
               ObjectOutputStream oout = new ObjectOutputStream(out);

               try {
                  oout.writeObject(this);
               } finally {
                  IOUtils.closeQuietly((OutputStream)oout);
               }
            } catch (IOException var11) {
               LogUtil.handleException(log, var11, true);
            }

            this.changed = false;
            log.trace("Cache file written.");
         }

      }
   }

   protected static String getCacheKey(EmbedFontInfo fontInfo) {
      if (fontInfo != null) {
         URI embedFile = fontInfo.getEmbedURI();
         URI metricsFile = fontInfo.getMetricsURI();
         return embedFile != null ? embedFile.toASCIIString() : metricsFile.toASCIIString();
      } else {
         return null;
      }
   }

   public boolean hasChanged() {
      return this.changed;
   }

   public boolean containsFont(String embedUrl) {
      return embedUrl != null && this.getFontFileMap().containsKey(embedUrl);
   }

   public boolean containsFont(EmbedFontInfo fontInfo) {
      return fontInfo != null && this.getFontFileMap().containsKey(getCacheKey(fontInfo));
   }

   public static File getFileFromUrls(String[] urls) {
      String[] var1 = urls;
      int var2 = urls.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         String urlStr = var1[var3];
         if (urlStr != null) {
            File fontFile = null;
            if (urlStr.startsWith("file:")) {
               try {
                  URL url = new URL(urlStr);
                  fontFile = FileUtils.toFile(url);
               } catch (MalformedURLException var7) {
               }
            }

            if (fontFile == null) {
               fontFile = new File(urlStr);
            }

            if (fontFile.exists() && fontFile.canRead()) {
               return fontFile;
            }
         }
      }

      return null;
   }

   private Map getFontFileMap() {
      if (this.fontfileMap == null) {
         this.fontfileMap = new HashMap();
      }

      return this.fontfileMap;
   }

   public void addFont(EmbedFontInfo fontInfo, InternalResourceResolver resourceResolver) {
      String cacheKey = getCacheKey(fontInfo);
      synchronized(this.changeLock) {
         CachedFontFile cachedFontFile;
         if (this.containsFont(cacheKey)) {
            cachedFontFile = (CachedFontFile)this.getFontFileMap().get(cacheKey);
            if (!cachedFontFile.containsFont(fontInfo)) {
               cachedFontFile.put(fontInfo);
            }
         } else {
            URI fontUri = resourceResolver.resolveFromBase(fontInfo.getEmbedURI());
            long lastModified = getLastModified(fontUri);
            cachedFontFile = new CachedFontFile(lastModified);
            if (log.isTraceEnabled()) {
               log.trace("Font added to cache: " + cacheKey);
            }

            cachedFontFile.put(fontInfo);
            this.getFontFileMap().put(cacheKey, cachedFontFile);
            this.changed = true;
         }

      }
   }

   public CachedFontFile getFontFile(String embedUrl) {
      return this.containsFont(embedUrl) ? (CachedFontFile)this.getFontFileMap().get(embedUrl) : null;
   }

   public EmbedFontInfo[] getFontInfos(String embedUrl, long lastModified) {
      CachedFontFile cff = this.getFontFile(embedUrl);
      if (cff.lastModified() == lastModified) {
         return cff.getEmbedFontInfos();
      } else {
         this.removeFont(embedUrl);
         return null;
      }
   }

   public void removeFont(String embedUrl) {
      synchronized(this.changeLock) {
         if (this.containsFont(embedUrl)) {
            if (log.isTraceEnabled()) {
               log.trace("Font removed from cache: " + embedUrl);
            }

            this.getFontFileMap().remove(embedUrl);
            this.changed = true;
         }

      }
   }

   public boolean isFailedFont(String embedUrl, long lastModified) {
      synchronized(this.changeLock) {
         if (this.getFailedFontMap().containsKey(embedUrl)) {
            long failedLastModified = (Long)this.getFailedFontMap().get(embedUrl);
            if (lastModified != failedLastModified) {
               this.getFailedFontMap().remove(embedUrl);
               this.changed = true;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public void registerFailedFont(String embedUrl, long lastModified) {
      synchronized(this.changeLock) {
         if (!this.getFailedFontMap().containsKey(embedUrl)) {
            this.getFailedFontMap().put(embedUrl, lastModified);
            this.changed = true;
         }

      }
   }

   private Map getFailedFontMap() {
      if (this.failedFontMap == null) {
         this.failedFontMap = new HashMap();
      }

      return this.failedFontMap;
   }

   public void clear() {
      synchronized(this.changeLock) {
         if (log.isTraceEnabled()) {
            log.trace("Font cache cleared.");
         }

         this.fontfileMap = null;
         this.failedFontMap = null;
         this.changed = true;
      }
   }

   public static long getLastModified(URI uri) {
      try {
         URL url = uri.toURL();
         URLConnection conn = url.openConnection();

         long var3;
         try {
            var3 = conn.getLastModified();
         } finally {
            IOUtils.closeQuietly(conn.getInputStream());
         }

         return var3;
      } catch (IOException var9) {
         log.debug("IOError: " + var9.getMessage());
         return 0L;
      }
   }

   private static class CachedFontFile implements Serializable {
      private static final long serialVersionUID = 4524237324330578883L;
      private long lastModified = -1L;
      private Map filefontsMap;

      public CachedFontFile(long lastModified) {
         this.setLastModified(lastModified);
      }

      private Map getFileFontsMap() {
         if (this.filefontsMap == null) {
            this.filefontsMap = new HashMap();
         }

         return this.filefontsMap;
      }

      void put(EmbedFontInfo efi) {
         this.getFileFontsMap().put(efi.getPostScriptName(), efi);
      }

      public boolean containsFont(EmbedFontInfo efi) {
         return efi.getPostScriptName() != null && this.getFileFontsMap().containsKey(efi.getPostScriptName());
      }

      public EmbedFontInfo[] getEmbedFontInfos() {
         return (EmbedFontInfo[])this.getFileFontsMap().values().toArray(new EmbedFontInfo[this.getFileFontsMap().size()]);
      }

      public long lastModified() {
         return this.lastModified;
      }

      public void setLastModified(long lastModified) {
         this.lastModified = lastModified;
      }

      public String toString() {
         return super.toString() + ", lastModified=" + this.lastModified;
      }
   }
}
