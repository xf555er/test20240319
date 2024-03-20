package org.apache.fop.afp;

import java.awt.geom.AffineTransform;

public class AFPRectanglePainter extends AbstractAFPPainter {
   public AFPRectanglePainter(AFPPaintingState paintingState, DataStream dataStream) {
      super(paintingState, dataStream);
   }

   public void paint(PaintingInfo paintInfo) {
      RectanglePaintingInfo rectanglePaintInfo = (RectanglePaintingInfo)paintInfo;
      int pageWidth = this.dataStream.getCurrentPage().getWidth();
      int pageHeight = this.dataStream.getCurrentPage().getHeight();
      AFPUnitConverter unitConv = this.paintingState.getUnitConverter();
      float width = unitConv.pt2units(rectanglePaintInfo.getWidth());
      float height = unitConv.pt2units(rectanglePaintInfo.getHeight());
      float x = unitConv.pt2units(rectanglePaintInfo.getX());
      float y = unitConv.pt2units(rectanglePaintInfo.getY());
      AffineTransform at = this.paintingState.getData().getTransform();
      AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
      lineDataInfo.setColor(this.paintingState.getColor());
      lineDataInfo.setRotation(this.paintingState.getRotation());
      lineDataInfo.setThickness(Math.round(height));
      int yNew;
      switch (lineDataInfo.getRotation()) {
         case 0:
         default:
            lineDataInfo.setX1(Math.round((float)at.getTranslateX() + x));
            yNew = Math.round((float)at.getTranslateY() + y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(Math.round((float)at.getTranslateX() + x + width));
            break;
         case 90:
            lineDataInfo.setX1(Math.round((float)at.getTranslateY() + x));
            yNew = pageWidth - Math.round((float)at.getTranslateX()) + Math.round(y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(Math.round(width + (float)at.getTranslateY() + x));
            break;
         case 180:
            lineDataInfo.setX1(pageWidth - Math.round((float)at.getTranslateX() - x));
            yNew = pageHeight - Math.round((float)at.getTranslateY() - y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(pageWidth - Math.round((float)at.getTranslateX() - x - width));
            break;
         case 270:
            lineDataInfo.setX1(pageHeight - Math.round((float)at.getTranslateY() - x));
            yNew = Math.round((float)at.getTranslateX() + y);
            lineDataInfo.setY1(yNew);
            lineDataInfo.setY2(yNew);
            lineDataInfo.setX2(pageHeight - Math.round((float)at.getTranslateY() - x - width));
      }

      this.dataStream.createLine(lineDataInfo);
   }
}
