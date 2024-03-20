package org.apache.batik.apps.svgbrowser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileSystemView;

class WindowsAltFileSystemView extends FileSystemView {
   public static final String EXCEPTION_CONTAINING_DIR_NULL = "AltFileSystemView.exception.containing.dir.null";
   public static final String EXCEPTION_DIRECTORY_ALREADY_EXISTS = "AltFileSystemView.exception.directory.already.exists";
   public static final String NEW_FOLDER_NAME = " AltFileSystemView.new.folder.name";
   public static final String FLOPPY_DRIVE = "AltFileSystemView.floppy.drive";

   public boolean isRoot(File f) {
      if (!f.isAbsolute()) {
         return false;
      } else {
         String parentPath = f.getParent();
         if (parentPath == null) {
            return true;
         } else {
            File parent = new File(parentPath);
            return parent.equals(f);
         }
      }
   }

   public File createNewFolder(File containingDir) throws IOException {
      if (containingDir == null) {
         throw new IOException(Resources.getString("AltFileSystemView.exception.containing.dir.null"));
      } else {
         File newFolder = null;
         newFolder = this.createFileObject(containingDir, Resources.getString(" AltFileSystemView.new.folder.name"));

         for(int i = 2; newFolder.exists() && i < 100; ++i) {
            newFolder = this.createFileObject(containingDir, Resources.getString(" AltFileSystemView.new.folder.name") + " (" + i + ')');
         }

         if (newFolder.exists()) {
            throw new IOException(Resources.formatMessage("AltFileSystemView.exception.directory.already.exists", new Object[]{newFolder.getAbsolutePath()}));
         } else {
            newFolder.mkdirs();
            return newFolder;
         }
      }
   }

   public boolean isHiddenFile(File f) {
      return false;
   }

   public File[] getRoots() {
      List rootsVector = new ArrayList();
      FileSystemRoot floppy = new FileSystemRoot(Resources.getString("AltFileSystemView.floppy.drive") + "\\");
      rootsVector.add(floppy);

      for(char c = 'C'; c <= 'Z'; ++c) {
         char[] device = new char[]{c, ':', '\\'};
         String deviceName = new String(device);
         File deviceFile = new FileSystemRoot(deviceName);
         if (deviceFile != null && deviceFile.exists()) {
            rootsVector.add(deviceFile);
         }
      }

      File[] roots = new File[rootsVector.size()];
      rootsVector.toArray(roots);
      return roots;
   }

   static class FileSystemRoot extends File {
      public FileSystemRoot(File f) {
         super(f, "");
      }

      public FileSystemRoot(String s) {
         super(s);
      }

      public boolean isDirectory() {
         return true;
      }
   }
}
