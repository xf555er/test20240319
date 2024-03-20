package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFImage;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;

public class PDFImageHandlerRawCCITTFax extends AbstractPDFImageHandler {
   private static final ImageFlavor[] FLAVORS;

   PDFImage createPDFImage(Image image, String xobjectKey) {
      return new ImageRawCCITTFaxAdapter((ImageRawCCITTFax)image, xobjectKey);
   }

   public int getPriority() {
      return 100;
   }

   public Class getSupportedImageClass() {
      return ImageRawCCITTFax.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      return (image == null || image instanceof ImageRawCCITTFax) && targetContext instanceof PDFRenderingContext;
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.RAW_CCITTFAX};
   }
}
