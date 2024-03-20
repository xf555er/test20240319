package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.IOCase;

public class SuffixFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -3389157631240246157L;
   private final String[] suffixes;
   private final IOCase caseSensitivity;

   public SuffixFileFilter(List suffixes) {
      this(suffixes, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(List suffixes, IOCase caseSensitivity) {
      if (suffixes == null) {
         throw new IllegalArgumentException("The list of suffixes must not be null");
      } else {
         this.suffixes = (String[])suffixes.toArray(EMPTY_STRING_ARRAY);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public SuffixFileFilter(String suffix) {
      this(suffix, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String... suffixes) {
      this(suffixes, IOCase.SENSITIVE);
   }

   public SuffixFileFilter(String suffix, IOCase caseSensitivity) {
      if (suffix == null) {
         throw new IllegalArgumentException("The suffix must not be null");
      } else {
         this.suffixes = new String[]{suffix};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public SuffixFileFilter(String[] suffixes, IOCase caseSensitivity) {
      if (suffixes == null) {
         throw new IllegalArgumentException("The array of suffixes must not be null");
      } else {
         this.suffixes = new String[suffixes.length];
         System.arraycopy(suffixes, 0, this.suffixes, 0, suffixes.length);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      return this.accept(file.getName());
   }

   public boolean accept(File file, String name) {
      return this.accept(name);
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return toFileVisitResult(this.accept(Objects.toString(file.getFileName(), (String)null)), file);
   }

   private boolean accept(String name) {
      String[] var2 = this.suffixes;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String suffix = var2[var4];
         if (this.caseSensitivity.checkEndsWith(name, suffix)) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.suffixes != null) {
         for(int i = 0; i < this.suffixes.length; ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.suffixes[i]);
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
