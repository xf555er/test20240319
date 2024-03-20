package org.apache.fop.render.pdf.extensions;

public abstract class PDFCollectionExtension extends PDFCollectionEntryExtension {
   protected PDFCollectionExtension(PDFObjectType type) {
      super(type);
   }

   public abstract void addEntry(PDFCollectionEntryExtension var1);

   public abstract PDFCollectionEntryExtension getLastEntry();
}
