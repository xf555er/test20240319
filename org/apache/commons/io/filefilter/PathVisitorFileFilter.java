package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import org.apache.commons.io.file.NoopPathVisitor;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.PathVisitor;

public class PathVisitorFileFilter extends AbstractFileFilter {
   private final PathVisitor pathVisitor;

   public PathVisitorFileFilter(PathVisitor pathVisitor) {
      this.pathVisitor = (PathVisitor)(pathVisitor == null ? NoopPathVisitor.INSTANCE : pathVisitor);
   }

   public boolean accept(File file) {
      try {
         Path path = file.toPath();
         return this.visitFile(path, file.exists() ? PathUtils.readBasicFileAttributes(path) : null) == FileVisitResult.CONTINUE;
      } catch (IOException var3) {
         return this.handle(var3) == FileVisitResult.CONTINUE;
      }
   }

   public boolean accept(File dir, String name) {
      try {
         Path path = dir.toPath().resolve(name);
         return this.accept(path, PathUtils.readBasicFileAttributes(path)) == FileVisitResult.CONTINUE;
      } catch (IOException var4) {
         return this.handle(var4) == FileVisitResult.CONTINUE;
      }
   }

   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      try {
         return Files.isDirectory(path, new LinkOption[0]) ? this.pathVisitor.postVisitDirectory(path, (IOException)null) : this.visitFile(path, attributes);
      } catch (IOException var4) {
         return this.handle(var4);
      }
   }

   public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
      return this.pathVisitor.visitFile(path, attributes);
   }
}
