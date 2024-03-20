package org.apache.fop.render.pdf.extensions;

public class PDFCollectionEntryExtension extends PDFObjectExtension {
   public static final String PROPERTY_KEY = "key";
   private String key;

   PDFCollectionEntryExtension(PDFObjectType type) {
      super(type);
   }

   public String getKey() {
      return this.key;
   }

   public void setKey(String key) {
      this.key = key;
   }
}
