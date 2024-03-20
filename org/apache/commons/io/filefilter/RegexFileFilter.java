package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.apache.commons.io.IOCase;

public class RegexFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 4269646126155225062L;
   private final Pattern pattern;
   private final Function pathToString;

   private static Pattern compile(String pattern, int flags) {
      if (pattern == null) {
         throw new IllegalArgumentException("Pattern is missing");
      } else {
         return Pattern.compile(pattern, flags);
      }
   }

   private static int toFlags(IOCase caseSensitivity) {
      return IOCase.isCaseSensitive(caseSensitivity) ? 2 : 0;
   }

   public RegexFileFilter(Pattern pattern) {
      this(pattern, (p) -> {
         return p.getFileName().toString();
      });
   }

   public RegexFileFilter(Pattern pattern, Function pathToString) {
      if (pattern == null) {
         throw new IllegalArgumentException("Pattern is missing");
      } else {
         this.pattern = pattern;
         this.pathToString = pathToString;
      }
   }

   public RegexFileFilter(String pattern) {
      this(pattern, 0);
   }

   public RegexFileFilter(String pattern, int flags) {
      this(compile(pattern, flags));
   }

   public RegexFileFilter(String pattern, IOCase caseSensitivity) {
      this(compile(pattern, toFlags(caseSensitivity)));
   }

   public boolean accept(File dir, String name) {
      return this.pattern.matcher(name).matches();
   }

   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      return toFileVisitResult(this.pattern.matcher((CharSequence)this.pathToString.apply(path)).matches(), path);
   }

   public String toString() {
      return "RegexFileFilter [pattern=" + this.pattern + "]";
   }
}
