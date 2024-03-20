package org.apache.fop.render.afp;

import java.awt.Rectangle;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

public class AFPImageHandlerRawCCITTFax extends AbstractAFPImageHandlerRawStream {
   private static final ImageFlavor[] FLAVORS;
   private final Log log = LogFactory.getLog(AFPImageHandlerRawJPEG.class);

   protected void setAdditionalParameters(AFPDataObjectInfo dataObjectInfo, ImageRawStream image) {
      AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)dataObjectInfo;
      ImageRawCCITTFax ccitt = (ImageRawCCITTFax)image;
      int compression = ccitt.getCompression();
      imageObjectInfo.setCompression(compression);
      imageObjectInfo.setBitsPerPixel(1);
      imageObjectInfo.setMimeType("image/tiff");
   }

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      this.log.debug("Embedding undecoded CCITT data as data container...");
      super.handleImage(context, image, pos);
   }

   protected AFPDataObjectInfo createDataObjectInfo() {
      return new AFPImageObjectInfo();
   }

   public int getPriority() {
      return 400;
   }

   public Class getSupportedImageClass() {
      return ImageRawCCITTFax.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return FLAVORS;
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      if (!(targetContext instanceof AFPRenderingContext)) {
         return false;
      } else {
         AFPRenderingContext afpContext = (AFPRenderingContext)targetContext;
         return afpContext.getPaintingState().isNativeImagesSupported() && (image == null || image instanceof ImageRawCCITTFax);
      }
   }

   static {
      FLAVORS = new ImageFlavor[]{ImageFlavor.RAW_CCITTFAX};
   }
}
