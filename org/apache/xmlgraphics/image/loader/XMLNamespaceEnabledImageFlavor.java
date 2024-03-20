package org.apache.xmlgraphics.image.loader;

public class XMLNamespaceEnabledImageFlavor extends RefinedImageFlavor {
   public static final ImageFlavor SVG_DOM;
   private String namespace;

   public XMLNamespaceEnabledImageFlavor(ImageFlavor parentFlavor, String namespace) {
      super(parentFlavor.getName() + ";namespace=" + namespace, parentFlavor);
      this.namespace = namespace;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         if (!super.equals(o)) {
            return false;
         } else {
            XMLNamespaceEnabledImageFlavor that = (XMLNamespaceEnabledImageFlavor)o;
            if (this.namespace != null) {
               if (!this.namespace.equals(that.namespace)) {
                  return false;
               }
            } else if (that.namespace != null) {
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
      result = 31 * result + (this.namespace != null ? this.namespace.hashCode() : 0);
      return result;
   }

   static {
      SVG_DOM = new XMLNamespaceEnabledImageFlavor(ImageFlavor.XML_DOM, "http://www.w3.org/2000/svg");
   }
}
