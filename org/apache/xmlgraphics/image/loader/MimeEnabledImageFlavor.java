package org.apache.xmlgraphics.image.loader;

public class MimeEnabledImageFlavor extends RefinedImageFlavor {
   private String mime;

   public MimeEnabledImageFlavor(ImageFlavor parentFlavor, String mime) {
      super(mime + ";" + parentFlavor.getName(), parentFlavor);
      this.mime = mime;
   }

   public String getMimeType() {
      return this.mime;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            MimeEnabledImageFlavor that = (MimeEnabledImageFlavor)o;
            if (this.mime != null) {
               if (!this.mime.equals(that.mime)) {
                  return false;
               }
            } else if (that.mime != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + (this.mime != null ? this.mime.hashCode() : 0);
      return result;
   }
}
