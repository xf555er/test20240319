package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFImage;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;

public class PDFImageHandlerRawJPEG extends AbstractPDFImageHandler {
   private static final ImageFlavor[] FLAVORS;

   public int getPriority() {
      return 100;
   }

   PDFImage createPDFImage(Image image, String xobjectKey) {
      return new ImageRawJPEGAdapter((ImageRawJPEG)image, xobjectKey);
   }

   public Class getSupportedImageClass() {
      return ImageRawJPEG.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      return (image == null || image instanceof ImageRawJPEG) && targetContext instanceof PDFRenderingContext;
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.RAW_JPEG};
   }
}
