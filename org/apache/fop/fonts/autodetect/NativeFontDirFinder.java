package org.apache.fop.fonts.autodetect;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class NativeFontDirFinder implements FontDirFinder {
   public List find() {
      List fontDirList = new ArrayList();
      String[] searchableDirectories = this.getSearchableDirectories();
      if (searchableDirectories != null) {
         String[] var3 = searchableDirectories;
         int var4 = searchableDirectories.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String searchableDirectory = var3[var5];
            File fontDir = new File(searchableDirectory);
            if (fontDir.exists() && fontDir.canRead()) {
               fontDirList.add(fontDir);
            }
         }
      }

      return fontDirList;
   }

   protected abstract String[] getSearchableDirectories();
}
