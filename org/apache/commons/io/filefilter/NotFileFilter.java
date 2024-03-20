package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class NotFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 6131563330944994230L;
   private final IOFileFilter filter;

   public NotFileFilter(IOFileFilter filter) {
      if (filter == null) {
         throw new IllegalArgumentException("The filter must not be null");
      } else {
         this.filter = filter;
      }
   }

   public boolean accept(File file) {
      return !this.filter.accept(file);
   }

   public boolean accept(File file, String name) {
      return !this.filter.accept(file, name);
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return this.not(this.filter.accept(file, attributes));
   }

   private FileVisitResult not(FileVisitResult accept) {
      return accept == FileVisitResult.CONTINUE ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
   }

   public String toString() {
      return "NOT (" + this.filter.toString() + ")";
   }
}
