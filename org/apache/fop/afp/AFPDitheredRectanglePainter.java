package org.apache.fop.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import org.apache.fop.util.bitmap.DitherUtil;
import org.apache.xmlgraphics.image.loader.ImageSize;

public class AFPDitheredRectanglePainter extends AbstractAFPPainter {
   private AFPResourceManager resourceManager;

   public AFPDitheredRectanglePainter(AFPPaintingState paintingState, DataStream dataStream, AFPResourceManager resourceManager) {
      super(paintingState, dataStream);
      this.resourceManager = resourceManager;
   }

   public void paint(PaintingInfo paintInfo) throws IOException {
      RectanglePaintingInfo rectanglePaintInfo = (RectanglePaintingInfo)paintInfo;
      if (!(rectanglePaintInfo.getWidth() <= 0.0F) && !(rectanglePaintInfo.getHeight() <= 0.0F)) {
         int ditherMatrix = 8;
         Dimension ditherSize = new Dimension(ditherMatrix, ditherMatrix);
         AFPImageObjectInfo imageObjectInfo = new AFPImageObjectInfo();
         imageObjectInfo.setMimeType("image/x-afp+fs10");
         imageObjectInfo.getResourceInfo().setLevel(new AFPResourceLevel(AFPResourceLevel.ResourceType.INLINE));
         imageObjectInfo.getResourceInfo().setImageDimension(ditherSize);
         imageObjectInfo.setBitsPerPixel(1);
         imageObjectInfo.setColor(false);
         imageObjectInfo.setMappingOption((byte)80);
         int resolution = this.paintingState.getResolution();
         ImageSize ditherBitmapSize = new ImageSize(ditherSize.width, ditherSize.height, (double)resolution);
         imageObjectInfo.setDataHeightRes((int)Math.round(ditherBitmapSize.getDpiHorizontal() * 10.0));
         imageObjectInfo.setDataWidthRes((int)Math.round(ditherBitmapSize.getDpiVertical() * 10.0));
         imageObjectInfo.setDataWidth(ditherSize.width);
         imageObjectInfo.setDataHeight(ditherSize.height);
         Color col = this.paintingState.getColor();
         byte[] dither = DitherUtil.getBayerDither(ditherMatrix, col, false);
         imageObjectInfo.setData(dither);
         int rotation = this.paintingState.getRotation();
         AffineTransform at = this.paintingState.getData().getTransform();
         Point2D origin = at.transform(new Point2D.Float(rectanglePaintInfo.getX() * 1000.0F, rectanglePaintInfo.getY() * 1000.0F), (Point2D)null);
         AFPUnitConverter unitConv = this.paintingState.getUnitConverter();
         float width = unitConv.pt2units(rectanglePaintInfo.getWidth());
         float height = unitConv.pt2units(rectanglePaintInfo.getHeight());
         AFPObjectAreaInfo objectAreaInfo = new AFPObjectAreaInfo((int)Math.round(origin.getX()), (int)Math.round(origin.getY()), Math.round(width), Math.round(height), resolution, rotation);
         imageObjectInfo.setObjectAreaInfo(objectAreaInfo);
         this.resourceManager.createObject(imageObjectInfo);
      }
   }
}
