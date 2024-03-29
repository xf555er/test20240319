package org.apache.commons.io.monitor;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.NameFileComparator;

public class FileAlterationObserver implements Serializable {
   private static final long serialVersionUID = 1185122225658782848L;
   private final List listeners;
   private final FileEntry rootEntry;
   private final FileFilter fileFilter;
   private final Comparator comparator;

   public FileAlterationObserver(String directoryName) {
      this(new File(directoryName));
   }

   public FileAlterationObserver(String directoryName, FileFilter fileFilter) {
      this(new File(directoryName), fileFilter);
   }

   public FileAlterationObserver(String directoryName, FileFilter fileFilter, IOCase caseSensitivity) {
      this(new File(directoryName), fileFilter, caseSensitivity);
   }

   public FileAlterationObserver(File directory) {
      this((File)directory, (FileFilter)null);
   }

   public FileAlterationObserver(File directory, FileFilter fileFilter) {
      this((File)directory, fileFilter, (IOCase)null);
   }

   public FileAlterationObserver(File directory, FileFilter fileFilter, IOCase caseSensitivity) {
      this(new FileEntry(directory), fileFilter, caseSensitivity);
   }

   protected FileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter, IOCase caseSensitivity) {
      this.listeners = new CopyOnWriteArrayList();
      if (rootEntry == null) {
         throw new IllegalArgumentException("Root entry is missing");
      } else if (rootEntry.getFile() == null) {
         throw new IllegalArgumentException("Root directory is missing");
      } else {
         this.rootEntry = rootEntry;
         this.fileFilter = fileFilter;
         if (caseSensitivity != null && !caseSensitivity.equals(IOCase.SYSTEM)) {
            if (caseSensitivity.equals(IOCase.INSENSITIVE)) {
               this.comparator = NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
            } else {
               this.comparator = NameFileComparator.NAME_COMPARATOR;
            }
         } else {
            this.comparator = NameFileComparator.NAME_SYSTEM_COMPARATOR;
         }

      }
   }

   public File getDirectory() {
      return this.rootEntry.getFile();
   }

   public FileFilter getFileFilter() {
      return this.fileFilter;
   }

   public void addListener(FileAlterationListener listener) {
      if (listener != null) {
         this.listeners.add(listener);
      }

   }

   public void removeListener(FileAlterationListener listener) {
      if (listener != null) {
         while(true) {
            if (this.listeners.remove(listener)) {
               continue;
            }
         }
      }

   }

   public Iterable getListeners() {
      return this.listeners;
   }

   public void initialize() throws Exception {
      this.rootEntry.refresh(this.rootEntry.getFile());
      FileEntry[] children = this.doListFiles(this.rootEntry.getFile(), this.rootEntry);
      this.rootEntry.setChildren(children);
   }

   public void destroy() throws Exception {
   }

   public void checkAndNotify() {
      Iterator var1 = this.listeners.iterator();

      while(var1.hasNext()) {
         FileAlterationListener listener = (FileAlterationListener)var1.next();
         listener.onStart(this);
      }

      File rootFile = this.rootEntry.getFile();
      if (rootFile.exists()) {
         this.checkAndNotify(this.rootEntry, this.rootEntry.getChildren(), this.listFiles(rootFile));
      } else if (this.rootEntry.isExists()) {
         this.checkAndNotify(this.rootEntry, this.rootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
      }

      Iterator var5 = this.listeners.iterator();

      while(var5.hasNext()) {
         FileAlterationListener listener = (FileAlterationListener)var5.next();
         listener.onStop(this);
      }

   }

   private void checkAndNotify(FileEntry parent, FileEntry[] previous, File[] files) {
      int c = 0;
      FileEntry[] current = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_FILE_ENTRY_ARRAY;
      FileEntry[] var6 = previous;
      int var7 = previous.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         FileEntry entry;
         for(entry = var6[var8]; c < files.length && this.comparator.compare(entry.getFile(), files[c]) > 0; ++c) {
            current[c] = this.createFileEntry(parent, files[c]);
            this.doCreate(current[c]);
         }

         if (c < files.length && this.comparator.compare(entry.getFile(), files[c]) == 0) {
            this.doMatch(entry, files[c]);
            this.checkAndNotify(entry, entry.getChildren(), this.listFiles(files[c]));
            current[c] = entry;
            ++c;
         } else {
            this.checkAndNotify(entry, entry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
            this.doDelete(entry);
         }
      }

      while(c < files.length) {
         current[c] = this.createFileEntry(parent, files[c]);
         this.doCreate(current[c]);
         ++c;
      }

      parent.setChildren(current);
   }

   private FileEntry createFileEntry(FileEntry parent, File file) {
      FileEntry entry = parent.newChildInstance(file);
      entry.refresh(file);
      FileEntry[] children = this.doListFiles(file, entry);
      entry.setChildren(children);
      return entry;
   }

   private FileEntry[] doListFiles(File file, FileEntry entry) {
      File[] files = this.listFiles(file);
      FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : FileEntry.EMPTY_FILE_ENTRY_ARRAY;

      for(int i = 0; i < files.length; ++i) {
         children[i] = this.createFileEntry(entry, files[i]);
      }

      return children;
   }

   private void doCreate(FileEntry entry) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         FileAlterationListener listener = (FileAlterationListener)var2.next();
         if (entry.isDirectory()) {
            listener.onDirectoryCreate(entry.getFile());
         } else {
            listener.onFileCreate(entry.getFile());
         }
      }

      FileEntry[] children = entry.getChildren();
      FileEntry[] var8 = children;
      int var4 = children.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         FileEntry aChildren = var8[var5];
         this.doCreate(aChildren);
      }

   }

   private void doMatch(FileEntry entry, File file) {
      if (entry.refresh(file)) {
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            FileAlterationListener listener = (FileAlterationListener)var3.next();
            if (entry.isDirectory()) {
               listener.onDirectoryChange(file);
            } else {
               listener.onFileChange(file);
            }
         }
      }

   }

   private void doDelete(FileEntry entry) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         FileAlterationListener listener = (FileAlterationListener)var2.next();
         if (entry.isDirectory()) {
            listener.onDirectoryDelete(entry.getFile());
         } else {
            listener.onFileDelete(entry.getFile());
         }
      }

   }

   private File[] listFiles(File file) {
      File[] children = null;
      if (file.isDirectory()) {
         children = this.fileFilter == null ? file.listFiles() : file.listFiles(this.fileFilter);
      }

      if (children == null) {
         children = FileUtils.EMPTY_FILE_ARRAY;
      }

      if (this.comparator != null && children.length > 1) {
         Arrays.sort(children, this.comparator);
      }

      return children;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getClass().getSimpleName());
      builder.append("[file='");
      builder.append(this.getDirectory().getPath());
      builder.append('\'');
      if (this.fileFilter != null) {
         builder.append(", ");
         builder.append(this.fileFilter.toString());
      }

      builder.append(", listeners=");
      builder.append(this.listeners.size());
      builder.append("]");
      return builder.toString();
   }
}
