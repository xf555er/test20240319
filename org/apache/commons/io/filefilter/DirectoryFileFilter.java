package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class DirectoryFileFilter extends AbstractFileFilter implements Serializable {
   public static final IOFileFilter DIRECTORY = new DirectoryFileFilter();
   public static final IOFileFilter INSTANCE;
   private static final long serialVersionUID = -5148237843784525732L;

   protected DirectoryFileFilter() {
   }

   public boolean accept(File file) {
      return file.isDirectory();
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return toFileVisitResult(Files.isDirectory(file, new LinkOption[0]), file);
   }

   static {
      INSTANCE = DIRECTORY;
   }
}
