package org.apache.commons.io.file;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.util.Objects;

public class DirectoryStreamFilter implements DirectoryStream.Filter {
   private final PathFilter pathFilter;

   public DirectoryStreamFilter(PathFilter pathFilter) {
      this.pathFilter = (PathFilter)Objects.requireNonNull(pathFilter, "pathFilter");
   }

   public boolean accept(Path path) throws IOException {
      return this.pathFilter.accept(path, PathUtils.readBasicFileAttributes(path)) == FileVisitResult.CONTINUE;
   }

   public PathFilter getPathFilter() {
      return this.pathFilter;
   }
}
