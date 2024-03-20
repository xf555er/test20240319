package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import org.apache.commons.io.file.PathVisitor;

public abstract class AbstractFileFilter implements IOFileFilter, PathVisitor {
   static FileVisitResult toFileVisitResult(boolean accept, Path path) {
      return accept ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE;
   }

   public boolean accept(File file) {
      Objects.requireNonNull(file, "file");
      return this.accept(file.getParentFile(), file.getName());
   }

   public boolean accept(File dir, String name) {
      Objects.requireNonNull(name, "name");
      return this.accept(new File(dir, name));
   }

   protected FileVisitResult handle(Throwable t) {
      return FileVisitResult.TERMINATE;
   }

   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
   }

   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
      return this.accept(dir, attributes);
   }

   public String toString() {
      return this.getClass().getSimpleName();
   }

   public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
      return this.accept(file, attributes);
   }

   public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
      return FileVisitResult.CONTINUE;
   }
}
