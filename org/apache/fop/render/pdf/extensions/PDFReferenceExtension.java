package org.apache.fop.render.pdf.extensions;

public class PDFReferenceExtension extends PDFCollectionEntryExtension {
   public static final String PROPERTY_REFID = "refid";
   private String refid;
   private Object resolvedReference;

   PDFReferenceExtension() {
      super(PDFObjectType.Reference);
   }

   public void setValue(Object value) {
      throw new UnsupportedOperationException();
   }

   public Object getValue() {
      return this;
   }

   public String getReferenceId() {
      return this.refid;
   }

   public void setReferenceId(String refid) {
      this.refid = refid;
   }

   public Object getResolvedReference() {
      return this.resolvedReference;
   }

   public void setResolvedReference(Object resolvedReference) {
      this.resolvedReference = resolvedReference;
   }
}
