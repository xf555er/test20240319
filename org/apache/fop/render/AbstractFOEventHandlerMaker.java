package org.apache.fop.render;

import java.io.OutputStream;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FOEventHandler;

public abstract class AbstractFOEventHandlerMaker {
   public abstract FOEventHandler makeFOEventHandler(FOUserAgent var1, OutputStream var2) throws FOPException;

   public abstract boolean needsOutputStream();

   public abstract String[] getSupportedMimeTypes();

   public boolean isMimeTypeSupported(String mimeType) {
      String[] mimes = this.getSupportedMimeTypes();
      String[] var3 = mimes;
      int var4 = mimes.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String mime = var3[var5];
         if (mime.equals(mimeType)) {
            return true;
         }
      }

      return false;
   }
}
