package org.apache.fop.render.afp;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPObjectAreaInfo;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceInfo;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;

public abstract class AbstractAFPImageHandlerRawStream extends AFPImageHandler implements ImageHandler {
   protected void setAdditionalParameters(AFPDataObjectInfo imageObjectInfo, ImageRawStream image) {
   }

   private void updateDataObjectInfo(AFPDataObjectInfo dataObjectInfo, ImageRawStream rawStream, AFPResourceManager resourceManager) throws IOException {
      dataObjectInfo.setMimeType(rawStream.getFlavor().getMimeType());
      AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
      if (!resourceInfo.levelChanged()) {
         resourceInfo.setLevel(resourceManager.getResourceLevelDefaults().getDefaultResourceLevel((byte)6));
      }

      InputStream inputStream = rawStream.createInputStream();

      try {
         dataObjectInfo.setData(IOUtils.toByteArray(inputStream));
      } finally {
         IOUtils.closeQuietly(inputStream);
      }

      int dataHeight = rawStream.getSize().getHeightPx();
      dataObjectInfo.setDataHeight(dataHeight);
      int dataWidth = rawStream.getSize().getWidthPx();
      dataObjectInfo.setDataWidth(dataWidth);
      ImageSize imageSize = rawStream.getSize();
      dataObjectInfo.setDataHeightRes((int)(imageSize.getDpiHorizontal() * 10.0));
      dataObjectInfo.setDataWidthRes((int)(imageSize.getDpiVertical() * 10.0));
   }

   public void handleImage(RenderingContext context, Image image, Rectangle pos) throws IOException {
      AFPRenderingContext afpContext = (AFPRenderingContext)context;
      AFPDataObjectInfo dataObjectInfo = this.createDataObjectInfo();
      dataObjectInfo.setResourceInfo(createResourceInformation(image.getInfo().getOriginalURI(), afpContext.getForeignAttributes()));
      dataObjectInfo.setObjectAreaInfo(createObjectAreaInfo(afpContext.getPaintingState(), pos));
      AFPPaintingState paintingState = afpContext.getPaintingState();
      int resolution = paintingState.getResolution();
      AFPObjectAreaInfo objectAreaInfo = dataObjectInfo.getObjectAreaInfo();
      objectAreaInfo.setResolution(resolution);
      ImageRawStream imageStream = (ImageRawStream)image;
      this.updateDataObjectInfo(dataObjectInfo, imageStream, afpContext.getResourceManager());
      this.setAdditionalParameters(dataObjectInfo, imageStream);
      afpContext.getResourceManager().createObject(dataObjectInfo);
   }
}
