package org.apache.commons.io.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;

public class CleaningPathVisitor extends CountingPathVisitor {
   private final String[] skip;
   private final boolean overrideReadOnly;

   public static CountingPathVisitor withBigIntegerCounters() {
      return new CleaningPathVisitor(Counters.bigIntegerPathCounters(), new String[0]);
   }

   public static CountingPathVisitor withLongCounters() {
      return new CleaningPathVisitor(Counters.longPathCounters(), new String[0]);
   }

   public CleaningPathVisitor(Counters.PathCounters pathCounter, DeleteOption[] deleteOption, String... skip) {
      super(pathCounter);
      String[] temp = skip != null ? (String[])skip.clone() : EMPTY_STRING_ARRAY;
      Arrays.sort(temp);
      this.skip = temp;
      this.overrideReadOnly = StandardDeleteOption.overrideReadOnly(deleteOption);
   }

   public CleaningPathVisitor(Counters.PathCounters pathCounter, String... skip) {
      this(pathCounter, PathUtils.EMPTY_DELETE_OPTION_ARRAY, skip);
   }

   private boolean accept(Path path) {
      return Arrays.binarySearch(this.skip, Objects.toString(path.getFileName(), (String)null)) < 0;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!super.equals(obj)) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         CleaningPathVisitor other = (CleaningPathVisitor)obj;
         return this.overrideReadOnly == other.overrideReadOnly && Arrays.equals(this.skip, other.skip);
      }
   }

   public int hashCode() {
      int prime = true;
      int result = super.hashCode();
      result = 31 * result + Arrays.hashCode(this.skip);
      result = 31 * result + Objects.hash(new Object[]{this.overrideReadOnly});
      return result;
   }

   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
      super.preVisitDirectory(dir, attributes);
      return this.accept(dir) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
   }

   public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
      if (this.accept(file) && Files.exists(file, new LinkOption[]{LinkOption.NOFOLLOW_LINKS})) {
         if (this.overrideReadOnly) {
            PathUtils.setReadOnly(file, false, LinkOption.NOFOLLOW_LINKS);
         }

         Files.deleteIfExists(file);
      }

      this.updateFileCounters(file, attributes);
      return FileVisitResult.CONTINUE;
   }
}
