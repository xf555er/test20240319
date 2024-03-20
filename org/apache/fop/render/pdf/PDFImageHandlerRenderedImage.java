package org.apache.fop.render.pdf;

import org.apache.fop.pdf.PDFImage;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRendered;

public class PDFImageHandlerRenderedImage extends AbstractPDFImageHandler {
   private static final ImageFlavor[] FLAVORS;

   PDFImage createPDFImage(Image image, String xobjectKey) {
      return new ImageRenderedAdapter((ImageRendered)image, xobjectKey);
   }

   public int getPriority() {
      return 300;
   }

   public Class getSupportedImageClass() {
      return ImageRendered.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      return (image == null || image instanceof ImageRendered) && targetContext instanceof PDFRenderingContext;
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.BUFFERED_IMAGE, ImageFlavor.RENDERED_IMAGE};
   }
}
