package org.apache.commons.io;

import java.io.File;
import java.io.IOException;

public class FileDeleteStrategy {
   public static final FileDeleteStrategy NORMAL = new FileDeleteStrategy("Normal");
   public static final FileDeleteStrategy FORCE = new ForceFileDeleteStrategy();
   private final String name;

   protected FileDeleteStrategy(String name) {
      this.name = name;
   }

   public void delete(File fileToDelete) throws IOException {
      if (fileToDelete.exists() && !this.doDelete(fileToDelete)) {
         throw new IOException("Deletion failed: " + fileToDelete);
      }
   }

   public boolean deleteQuietly(File fileToDelete) {
      if (fileToDelete != null && fileToDelete.exists()) {
         try {
            return this.doDelete(fileToDelete);
         } catch (IOException var3) {
            return false;
         }
      } else {
         return true;
      }
   }

   protected boolean doDelete(File file) throws IOException {
      FileUtils.delete(file);
      return true;
   }

   public String toString() {
      return "FileDeleteStrategy[" + this.name + "]";
   }

   static class ForceFileDeleteStrategy extends FileDeleteStrategy {
      ForceFileDeleteStrategy() {
         super("Force");
      }

      protected boolean doDelete(File fileToDelete) throws IOException {
         FileUtils.forceDelete(fileToDelete);
         return true;
      }
   }
}
