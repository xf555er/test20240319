package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class PDFImageXObject extends PDFXObject {
   private PDFImage pdfimage;

   public PDFImageXObject(int xnumber, PDFImage img) {
      this.put("Name", new PDFName("Im" + xnumber));
      this.pdfimage = img;
   }

   public int output(OutputStream stream) throws IOException {
      if (this.getDocument().getProfile().isPDFVTActive()) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         this.pdfimage.outputContents(baos);
         this.put("GTS_XID", "uuid:" + UUID.nameUUIDFromBytes(baos.toByteArray()));
      }

      int length = super.output(stream);
      this.pdfimage = null;
      return length;
   }

   protected void populateStreamDict(Object lengthEntry) {
      super.populateStreamDict(lengthEntry);
      if (this.pdfimage.isPS()) {
         this.populateDictionaryFromPS();
      } else {
         this.populateDictionaryFromImage();
      }

   }

   private void populateDictionaryFromPS() {
      this.getDocumentSafely().getProfile().verifyPSXObjectsAllowed();
      this.put("Subtype", new PDFName("PS"));
   }

   private void populateDictionaryFromImage() {
      this.put("Subtype", new PDFName("Image"));
      this.put("Width", this.pdfimage.getWidth());
      this.put("Height", this.pdfimage.getHeight());
      this.put("BitsPerComponent", this.pdfimage.getBitsPerComponent());
      PDFICCStream pdfICCStream = this.pdfimage.getICCStream();
      if (pdfICCStream != null) {
         this.put("ColorSpace", new PDFArray(this, new Object[]{new PDFName("ICCBased"), pdfICCStream}));
      } else {
         PDFDeviceColorSpace cs = this.pdfimage.getColorSpace();
         this.put("ColorSpace", new PDFName(cs.getName()));
      }

      if (this.pdfimage.isInverted()) {
         Float zero = 0.0F;
         Float one = 1.0F;
         PDFArray decode = new PDFArray(this);
         int i = 0;

         for(int c = this.pdfimage.getColorSpace().getNumComponents(); i < c; ++i) {
            decode.add(one);
            decode.add(zero);
         }

         this.put("Decode", decode);
      }

      if (this.pdfimage.isTransparent()) {
         PDFColor transp = this.pdfimage.getTransparentColor();
         PDFArray mask = new PDFArray(this);
         if (this.pdfimage.getColorSpace().isGrayColorSpace()) {
            mask.add(transp.red255());
            mask.add(transp.red255());
         } else {
            mask.add(transp.red255());
            mask.add(transp.red255());
            mask.add(transp.green255());
            mask.add(transp.green255());
            mask.add(transp.blue255());
            mask.add(transp.blue255());
         }

         this.put("Mask", mask);
      }

      PDFReference ref = this.pdfimage.getSoftMaskReference();
      if (ref != null) {
         this.put("SMask", ref);
      }

      this.pdfimage.populateXObjectDictionary(this.getDictionary());
   }

   protected void outputRawStreamData(OutputStream out) throws IOException {
      this.pdfimage.outputContents(out);
   }

   protected int getSizeHint() throws IOException {
      return 0;
   }

   protected void prepareImplicitFilters() {
      PDFFilter pdfFilter = this.pdfimage.getPDFFilter();
      if (pdfFilter != null) {
         this.getFilterList().ensureFilterInPlace(pdfFilter);
      }

   }

   protected String getDefaultFilterName() {
      return this.pdfimage.getFilterHint();
   }

   protected boolean multipleFiltersAllowed() {
      return this.pdfimage.multipleFiltersAllowed();
   }

   public void getChildren(Set children) {
      super.getChildren(children);
      PDFICCStream pdfICCStream = this.pdfimage.getICCStream();
      if (pdfICCStream != null) {
         children.add(pdfICCStream);
         pdfICCStream.getChildren(children);
      }

   }
}
