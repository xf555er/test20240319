package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

public class WildcardFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = -7426486598995782105L;
   private final String[] wildcards;
   private final IOCase caseSensitivity;

   public WildcardFileFilter(List wildcards) {
      this(wildcards, IOCase.SENSITIVE);
   }

   public WildcardFileFilter(List wildcards, IOCase caseSensitivity) {
      if (wildcards == null) {
         throw new IllegalArgumentException("The wildcard list must not be null");
      } else {
         this.wildcards = (String[])wildcards.toArray(EMPTY_STRING_ARRAY);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public WildcardFileFilter(String wildcard) {
      this(wildcard, IOCase.SENSITIVE);
   }

   public WildcardFileFilter(String... wildcards) {
      this(wildcards, IOCase.SENSITIVE);
   }

   public WildcardFileFilter(String wildcard, IOCase caseSensitivity) {
      if (wildcard == null) {
         throw new IllegalArgumentException("The wildcard must not be null");
      } else {
         this.wildcards = new String[]{wildcard};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public WildcardFileFilter(String[] wildcards, IOCase caseSensitivity) {
      if (wildcards == null) {
         throw new IllegalArgumentException("The wildcard array must not be null");
      } else {
         this.wildcards = new String[wildcards.length];
         System.arraycopy(wildcards, 0, this.wildcards, 0, wildcards.length);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      return this.accept(file.getName());
   }

   public boolean accept(File dir, String name) {
      return this.accept(name);
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      return toFileVisitResult(this.accept(Objects.toString(file.getFileName(), (String)null)), file);
   }

   private boolean accept(String name) {
      String[] var2 = this.wildcards;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String wildcard = var2[var4];
         if (FilenameUtils.wildcardMatch(name, wildcard, this.caseSensitivity)) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");

      for(int i = 0; i < this.wildcards.length; ++i) {
         if (i > 0) {
            buffer.append(",");
         }

         buffer.append(this.wildcards[i]);
      }

      buffer.append(")");
      return buffer.toString();
   }
}
