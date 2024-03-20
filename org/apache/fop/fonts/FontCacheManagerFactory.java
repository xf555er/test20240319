package org.apache.fop.fonts;

import java.io.File;
import java.net.URI;
import org.apache.fop.apps.FOPException;

public final class FontCacheManagerFactory {
   private FontCacheManagerFactory() {
   }

   public static FontCacheManager createDefault() {
      return new FontCacheManagerImpl();
   }

   public static FontCacheManager createDisabled() {
      return new DisabledFontCacheManager();
   }

   private static final class DisabledFontCacheManager implements FontCacheManager {
      private DisabledFontCacheManager() {
      }

      public FontCache load() {
         return null;
      }

      public void save() throws FOPException {
      }

      public void delete() throws FOPException {
         throw new FOPException("Font Cache disabled");
      }

      public void setCacheFile(URI fontCacheURI) {
      }

      // $FF: synthetic method
      DisabledFontCacheManager(Object x0) {
         this();
      }
   }

   private static final class FontCacheManagerImpl implements FontCacheManager {
      private File cacheFile;
      private FontCache fontCache;

      private FontCacheManagerImpl() {
      }

      public FontCache load() {
         if (this.fontCache == null) {
            this.fontCache = FontCache.loadFrom(this.getCacheFile(false));
            if (this.fontCache == null) {
               this.fontCache = new FontCache();
            }
         }

         return this.fontCache;
      }

      public void save() throws FOPException {
         if (this.fontCache != null && this.fontCache.hasChanged()) {
            this.fontCache.saveTo(this.getCacheFile(true));
         }

      }

      public void delete() throws FOPException {
         if (!this.getCacheFile(true).delete()) {
            throw new FOPException("Failed to flush the font cache file '" + this.cacheFile + "'.");
         }
      }

      private File getCacheFile(boolean forWriting) {
         return this.cacheFile != null ? this.cacheFile : FontCache.getDefaultCacheFile(forWriting);
      }

      public void setCacheFile(URI fontCacheURI) {
         this.cacheFile = new File(fontCacheURI);
      }

      // $FF: synthetic method
      FontCacheManagerImpl(Object x0) {
         this();
      }
   }
}
