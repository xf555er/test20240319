package org.apache.commons.io.file.spi;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FileSystemProviders {
   private static final FileSystemProviders INSTALLED = new FileSystemProviders(FileSystemProvider.installedProviders());
   private final List providers;

   public static FileSystemProvider getFileSystemProvider(Path path) {
      return ((Path)Objects.requireNonNull(path, "path")).getFileSystem().provider();
   }

   public static FileSystemProviders installed() {
      return INSTALLED;
   }

   private FileSystemProviders(List providers) {
      this.providers = providers;
   }

   public FileSystemProvider getFileSystemProvider(String scheme) {
      Objects.requireNonNull(scheme, "scheme");
      if (scheme.equalsIgnoreCase("file")) {
         return FileSystems.getDefault().provider();
      } else {
         if (this.providers != null) {
            Iterator var2 = this.providers.iterator();

            while(var2.hasNext()) {
               FileSystemProvider provider = (FileSystemProvider)var2.next();
               if (provider.getScheme().equalsIgnoreCase(scheme)) {
                  return provider;
               }
            }
         }

         return null;
      }
   }

   public FileSystemProvider getFileSystemProvider(URI uri) {
      return this.getFileSystemProvider(((URI)Objects.requireNonNull(uri, "uri")).getScheme());
   }

   public FileSystemProvider getFileSystemProvider(URL url) {
      return this.getFileSystemProvider(((URL)Objects.requireNonNull(url, "url")).getProtocol());
   }
}
