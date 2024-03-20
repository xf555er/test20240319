package org.apache.commons.io.file;

import org.apache.commons.io.IOUtils;

public enum StandardDeleteOption implements DeleteOption {
   OVERRIDE_READ_ONLY;

   public static boolean overrideReadOnly(DeleteOption[] options) {
      if (IOUtils.length((Object[])options) == 0) {
         return false;
      } else {
         DeleteOption[] var1 = options;
         int var2 = options.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            DeleteOption deleteOption = var1[var3];
            if (deleteOption == OVERRIDE_READ_ONLY) {
               return true;
            }
         }

         return false;
      }
   }
}
