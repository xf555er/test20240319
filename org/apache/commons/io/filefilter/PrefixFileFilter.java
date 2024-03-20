package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import org.apache.commons.io.IOCase;

public class PrefixFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 8533897440809599867L;
   private final String[] prefixes;
   private final IOCase caseSensitivity;

   public PrefixFileFilter(List prefixes) {
      this(prefixes, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(List prefixes, IOCase caseSensitivity) {
      if (prefixes == null) {
         throw new IllegalArgumentException("The list of prefixes must not be null");
      } else {
         this.prefixes = (String[])prefixes.toArray(EMPTY_STRING_ARRAY);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public PrefixFileFilter(String prefix) {
      this(prefix, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String... prefixes) {
      this(prefixes, IOCase.SENSITIVE);
   }

   public PrefixFileFilter(String prefix, IOCase caseSensitivity) {
      if (prefix == null) {
         throw new IllegalArgumentException("The prefix must not be null");
      } else {
         this.prefixes = new String[]{prefix};
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public PrefixFileFilter(String[] prefixes, IOCase caseSensitivity) {
      if (prefixes == null) {
         throw new IllegalArgumentException("The array of prefixes must not be null");
      } else {
         this.prefixes = new String[prefixes.length];
         System.arraycopy(prefixes, 0, this.prefixes, 0, prefixes.length);
         this.caseSensitivity = caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity;
      }
   }

   public boolean accept(File file) {
      return this.accept(file == null ? null : file.getName());
   }

   public boolean accept(File file, String name) {
      return this.accept(name);
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      Path fileName = file.getFileName();
      return toFileVisitResult(this.accept(fileName == null ? null : fileName.toFile()), file);
   }

   private boolean accept(String name) {
      String[] var2 = this.prefixes;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String prefix = var2[var4];
         if (this.caseSensitivity.checkStartsWith(name, prefix)) {
            return true;
         }
      }

      return false;
   }

   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.prefixes != null) {
         for(int i = 0; i < this.prefixes.length; ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.prefixes[i]);
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
