package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class OrFileFilter extends AbstractFileFilter implements ConditionalFileFilter, Serializable {
   private static final long serialVersionUID = 5767770777065432721L;
   private final List fileFilters;

   public OrFileFilter() {
      this(0);
   }

   private OrFileFilter(ArrayList initialList) {
      this.fileFilters = (List)Objects.requireNonNull(initialList, "initialList");
   }

   private OrFileFilter(int initialCapacity) {
      this(new ArrayList(initialCapacity));
   }

   public OrFileFilter(IOFileFilter... fileFilters) {
      this(((IOFileFilter[])Objects.requireNonNull(fileFilters, "fileFilters")).length);
      this.addFileFilter(fileFilters);
   }

   public OrFileFilter(IOFileFilter filter1, IOFileFilter filter2) {
      this(2);
      this.addFileFilter(filter1);
      this.addFileFilter(filter2);
   }

   public OrFileFilter(List fileFilters) {
      this(new ArrayList((Collection)Objects.requireNonNull(fileFilters, "fileFilters")));
   }

   public boolean accept(File file) {
      Iterator var2 = this.fileFilters.iterator();

      IOFileFilter fileFilter;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         fileFilter = (IOFileFilter)var2.next();
      } while(!fileFilter.accept(file));

      return true;
   }

   public boolean accept(File file, String name) {
      Iterator var3 = this.fileFilters.iterator();

      IOFileFilter fileFilter;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         fileFilter = (IOFileFilter)var3.next();
      } while(!fileFilter.accept(file, name));

      return true;
   }

   public FileVisitResult accept(Path file, BasicFileAttributes attributes) {
      Iterator var3 = this.fileFilters.iterator();

      IOFileFilter fileFilter;
      do {
         if (!var3.hasNext()) {
            return FileVisitResult.TERMINATE;
         }

         fileFilter = (IOFileFilter)var3.next();
      } while(fileFilter.accept(file, attributes) != FileVisitResult.CONTINUE);

      return FileVisitResult.CONTINUE;
   }

   public void addFileFilter(IOFileFilter fileFilter) {
      this.fileFilters.add(Objects.requireNonNull(fileFilter, "fileFilter"));
   }

   public void addFileFilter(IOFileFilter... fileFilters) {
      IOFileFilter[] var2 = (IOFileFilter[])Objects.requireNonNull(fileFilters, "fileFilters");
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         IOFileFilter fileFilter = var2[var4];
         this.addFileFilter(fileFilter);
      }

   }

   public List getFileFilters() {
      return Collections.unmodifiableList(this.fileFilters);
   }

   public boolean removeFileFilter(IOFileFilter fileFilter) {
      return this.fileFilters.remove(fileFilter);
   }

   public void setFileFilters(List fileFilters) {
      this.fileFilters.clear();
      this.fileFilters.addAll((Collection)Objects.requireNonNull(fileFilters, "fileFilters"));
   }

   public String toString() {
      StringBuilder buffer = new StringBuilder();
      buffer.append(super.toString());
      buffer.append("(");
      if (this.fileFilters != null) {
         for(int i = 0; i < this.fileFilters.size(); ++i) {
            if (i > 0) {
               buffer.append(",");
            }

            buffer.append(this.fileFilters.get(i));
         }
      }

      buffer.append(")");
      return buffer.toString();
   }
}
