package org.apache.fop.render.intermediate;

public abstract class AbstractIFDocumentHandlerMaker {
   public abstract IFDocumentHandler makeIFDocumentHandler(IFContext var1);

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
