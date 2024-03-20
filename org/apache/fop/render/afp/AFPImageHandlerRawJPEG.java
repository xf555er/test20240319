package org.apache.fop.render.afp;

import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPImageObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawJPEG;

public class AFPImageHandlerRawJPEG extends AFPImageHandler implements ImageHandler {
   private final Log log = LogFactory.getLog(AFPImageHandlerRawJPEG.class);

   private void setDefaultResourceLevel(AFPImageObjectInfo imageObjectInfo, AFPResourceManager resourceManager) {
      AFPResourceInfo resourceInfo = imageObjectInfo.getResourceInfo();
      if (!resourceInfo.levelChanged()) {
         resourceInfo.setLevel(resourceManager.getResourceLevelDefaults().getDefaultResourceLevel((byte)6));
      }

   }

   protected AFPDataObjectInfo createDataObjectInfo() {
      return new AFPImageObjectInfo();
   }

   public int getPriority() {
      return 150;
   }

   public Class getSupportedImageClass() {
      return ImageRawJPEG.class;
   }

   public ImageFlavor[] getSupportedImageFlavors() {
      return new ImageFlavor[]{ImageFlavor.RAW_JPEG};
   }

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      AFPRenderingContext afpContext = (AFPRenderingContext)context;
      AFPDataObjectInfo info = this.createDataObjectInfo();

      assert info instanceof AFPImageObjectInfo;

      AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)info;
      AFPPaintingState paintingState = afpContext.getPaintingState();
      imageObjectInfo.setResourceInfo(createResourceInformation(image.getInfo().getOriginalURI(), afpContext.getForeignAttributes()));
      this.setDefaultResourceLevel(imageObjectInfo, afpContext.getResourceManager());
      imageObjectInfo.setObjectAreaInfo(createObjectAreaInfo(paintingState, pos));
      this.updateIntrinsicSize(imageObjectInfo, paintingState, image.getSize());
      ImageRawJPEG jpeg = (ImageRawJPEG)image;
      imageObjectInfo.setCompression(-125);
      ColorSpace cs = jpeg.getColorSpace();
      switch (cs.getType()) {
         case 5:
            imageObjectInfo.setMimeType("image/x-afp+fs11");
            imageObjectInfo.setColor(true);
            imageObjectInfo.setBitsPerPixel(24);
            break;
         case 6:
            imageObjectInfo.setMimeType("image/x-afp+fs11");
            imageObjectInfo.setColor(false);
            imageObjectInfo.setBitsPerPixel(8);
            break;
         case 7:
         case 8:
         default:
            throw new IllegalStateException("Color space of JPEG image not supported: " + cs);
         case 9:
            imageObjectInfo.setMimeType("image/x-afp+fs45");
            imageObjectInfo.setColor(true);
            imageObjectInfo.setBitsPerPixel(32);
      }

      boolean included = afpContext.getResourceManager().tryIncludeObject(imageObjectInfo);
      if (!included) {
         this.log.debug("Embedding undecoded JPEG as IOCA image...");
         InputStream inputStream = jpeg.createInputStream();

         try {
            imageObjectInfo.setData(IOUtils.toByteArray(inputStream));
         } finally {
            IOUtils.closeQuietly(inputStream);
         }

         afpContext.getResourceManager().createObject(imageObjectInfo);
      }

   }

   private void updateIntrinsicSize(AFPImageObjectInfo imageObjectInfo, AFPPaintingState paintingState, ImageSize targetSize) {
      imageObjectInfo.setDataHeightRes((int)Math.round(targetSize.getDpiHorizontal() * 10.0));
      imageObjectInfo.setDataWidthRes((int)Math.round(targetSize.getDpiVertical() * 10.0));
      imageObjectInfo.setDataHeight(targetSize.getHeightPx());
      imageObjectInfo.setDataWidth(targetSize.getWidthPx());
      int resolution = paintingState.getResolution();
      AFPObjectAreaInfo objectAreaInfo = imageObjectInfo.getObjectAreaInfo();
      objectAreaInfo.setResolution(resolution);
   }

   public boolean isCompatible(RenderingContext targetContext, Image image) {
      if (!(targetContext instanceof AFPRenderingContext)) {
         return false;
      } else {
         AFPRenderingContext context = (AFPRenderingContext)targetContext;
         AFPPaintingState paintingState = context.getPaintingState();
         if (!paintingState.canEmbedJpeg()) {
            return false;
         } else if (paintingState.getBitsPerPixel() < 8) {
            return false;
         } else if (image == null) {
            return true;
         } else if (image instanceof ImageRawJPEG) {
            ImageRawJPEG jpeg = (ImageRawJPEG)image;
            ColorSpace cs = jpeg.getColorSpace();
            switch (cs.getType()) {
               case 7:
               case 8:
               default:
                  return false;
               case 9:
                  if (!paintingState.isCMYKImagesSupported()) {
                     return false;
                  }
               case 5:
               case 6:
                  return jpeg.getSOFType() == 192;
            }
         } else {
            return false;
         }
      }
   }
}
