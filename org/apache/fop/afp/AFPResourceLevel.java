package org.apache.fop.afp;

import java.net.URI;

public class AFPResourceLevel {
   private URI extUri;
   private final ResourceType resourceType;

   public static AFPResourceLevel valueOf(String levelString) {
      ResourceType resType = AFPResourceLevel.ResourceType.getValueOf(levelString);
      return resType != null ? new AFPResourceLevel(resType) : null;
   }

   public AFPResourceLevel(ResourceType resourceType) {
      this.resourceType = resourceType;
   }

   public boolean isPage() {
      return this.resourceType == AFPResourceLevel.ResourceType.PAGE;
   }

   public boolean isPageGroup() {
      return this.resourceType == AFPResourceLevel.ResourceType.PAGE_GROUP;
   }

   public boolean isDocument() {
      return this.resourceType == AFPResourceLevel.ResourceType.DOCUMENT;
   }

   public boolean isExternal() {
      return this.resourceType == AFPResourceLevel.ResourceType.EXTERNAL;
   }

   public boolean isPrintFile() {
      return this.resourceType == AFPResourceLevel.ResourceType.PRINT_FILE;
   }

   public boolean isInline() {
      return this.resourceType == AFPResourceLevel.ResourceType.INLINE;
   }

   public URI getExternalURI() {
      return this.extUri;
   }

   public void setExternalUri(URI uri) {
      this.extUri = uri;
   }

   public String toString() {
      return this.resourceType + (this.isExternal() ? ", uri=" + this.extUri : "");
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj instanceof AFPResourceLevel) {
         AFPResourceLevel rl = (AFPResourceLevel)obj;
         return this.resourceType == rl.resourceType && (this.extUri == rl.extUri || this.extUri != null && this.extUri.equals(rl.extUri));
      } else {
         return false;
      }
   }

   public int hashCode() {
      int hash = 7;
      hash = 31 * hash + this.resourceType.hashCode();
      hash = 31 * hash + (null == this.extUri ? 0 : this.extUri.hashCode());
      return hash;
   }

   public static enum ResourceType {
      INLINE("inline"),
      PAGE("page"),
      PAGE_GROUP("page-group"),
      DOCUMENT("document"),
      PRINT_FILE("print-file"),
      EXTERNAL("external");

      private final String name;

      private ResourceType(String name) {
         this.name = name;
      }

      public static ResourceType getValueOf(String levelString) {
         ResourceType[] var1 = values();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            ResourceType resType = var1[var3];
            if (resType.name.equalsIgnoreCase(levelString)) {
               return resType;
            }
         }

         return null;
      }

      public String getName() {
         return this.name;
      }
   }
}
