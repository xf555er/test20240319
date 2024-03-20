package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;

public class EmptyFileFilter extends AbstractFileFilter implements Serializable {
   public static final IOFileFilter EMPTY = new EmptyFileFilter();
   public static final IOFileFilter NOT_EMPTY;
   private static final long serialVersionUID = 3631422087512832211L;

   protected EmptyFileFilter() {
   }

   public boolean accept(File file) {
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         return IOUtils.length((Object[])files) == 0;
      } else {
         return file.length() == 0L;
      }
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      try {
         if (Files.isDirectory(file, new LinkOption[0])) {
            Stream stream = Files.list(file);
            Throwable var4 = null;

            FileVisitResult var5;
            try {
               var5 = toFileVisitResult(!stream.findFirst().isPresent(), file);
            } catch (Throwable var15) {
               var4 = var15;
               throw var15;
            } finally {
               if (stream != null) {
                  if (var4 != null) {
                     try {
                        stream.close();
                     } catch (Throwable var14) {
                        var4.addSuppressed(var14);
                     }
                  } else {
                     stream.close();
                  }
               }

            }

            return var5;
         } else {
            return toFileVisitResult(Files.size(file) == 0L, file);
         }
      } catch (IOException var17) {
         return this.handle(var17);
      }
   }

   static {
      NOT_EMPTY = EMPTY.negate();
   }
}
