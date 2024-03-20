package org.apache.fop.render;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;

public abstract class AbstractRendererMaker {
   public abstract Renderer makeRenderer(FOUserAgent var1);

   public abstract boolean needsOutputStream();

   public abstract String[] getSupportedMimeTypes();

   public abstract void configureRenderer(FOUserAgent var1, Renderer var2) throws FOPException;

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
