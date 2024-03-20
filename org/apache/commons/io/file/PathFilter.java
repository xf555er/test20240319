package org.apache.commons.io.file;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@FunctionalInterface
public interface PathFilter {
   FileVisitResult accept(Path var1, BasicFileAttributes var2);
}
