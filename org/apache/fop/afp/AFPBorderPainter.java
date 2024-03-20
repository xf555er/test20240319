package org.apache.fop.afp;

import java.awt.geom.AffineTransform;
import org.apache.fop.render.intermediate.BorderPainter;
import org.apache.fop.util.ColorUtil;

public class AFPBorderPainter extends AbstractAFPPainter {
   public AFPBorderPainter(AFPPaintingState paintingState, DataStream dataStream) {
      super(paintingState, dataStream);
   }

   public void paint(PaintingInfo paintInfo) {
      Integer bytesAvailable = this.dataStream.getCurrentPage().getPresentationTextObject().getBytesAvailable();
      if (bytesAvailable != null && bytesAvailable < 1024) {
         this.dataStream.getCurrentPage().endPresentationObject();
      }

      BorderPaintingInfo borderPaintInfo = (BorderPaintingInfo)paintInfo;
      float w = borderPaintInfo.getX2() - borderPaintInfo.getX1();
      float h = borderPaintInfo.getY2() - borderPaintInfo.getY1();
      if (!(w < 0.0F) && !(h < 0.0F)) {
         int pageWidth = this.dataStream.getCurrentPage().getWidth();
         int pageHeight = this.dataStream.getCurrentPage().getHeight();
         AFPUnitConverter unitConv = this.paintingState.getUnitConverter();
         AffineTransform at = this.paintingState.getData().getTransform();
         float x1 = unitConv.pt2units(borderPaintInfo.getX1());
         float y1 = unitConv.pt2units(borderPaintInfo.getY1());
         float x2 = unitConv.pt2units(borderPaintInfo.getX2());
         float y2 = unitConv.pt2units(borderPaintInfo.getY2());
         switch (this.paintingState.getRotation()) {
            case 0:
            default:
               x1 = (float)((double)x1 + at.getTranslateX());
               y1 = (float)((double)y1 + at.getTranslateY());
               x2 = (float)((double)x2 + at.getTranslateX());
               y2 = (float)((double)y2 + at.getTranslateY());
               break;
            case 90:
               x1 = (float)((double)x1 + at.getTranslateY());
               y1 += (float)((double)pageWidth - at.getTranslateX());
               x2 = (float)((double)x2 + at.getTranslateY());
               y2 += (float)((double)pageWidth - at.getTranslateX());
               break;
            case 180:
               x1 += (float)((double)pageWidth - at.getTranslateX());
               y1 += (float)((double)pageHeight - at.getTranslateY());
               x2 += (float)((double)pageWidth - at.getTranslateX());
               y2 += (float)((double)pageHeight - at.getTranslateY());
               break;
            case 270:
               x1 = (float)((double)pageHeight - at.getTranslateY());
               y1 += (float)at.getTranslateX();
               x2 += x1;
               y2 += (float)at.getTranslateX();
         }

         AFPLineDataInfo lineDataInfo = new AFPLineDataInfo();
         lineDataInfo.setColor(borderPaintInfo.getColor());
         lineDataInfo.setRotation(this.paintingState.getRotation());
         lineDataInfo.setX1(Math.round(x1));
         lineDataInfo.setY1(Math.round(y1));
         float thickness;
         if (borderPaintInfo.isHorizontal()) {
            thickness = y2 - y1;
         } else {
            thickness = x2 - x1;
         }

         lineDataInfo.setThickness(Math.round(thickness));
         int dashWidth;
         switch (borderPaintInfo.getStyle()) {
            case 31:
               int ex2;
               int spaceWidth;
               if (borderPaintInfo.isHorizontal()) {
                  dashWidth = (int)unitConv.pt2units(BorderPainter.dashWidthCalculator(w, h));
                  lineDataInfo.setX2(lineDataInfo.getX1() + dashWidth);
                  lineDataInfo.setY2(lineDataInfo.getY1());
                  ex2 = Math.round(x2);
                  spaceWidth = (int)(0.5F * (float)dashWidth);

                  while(lineDataInfo.getX2() <= ex2 && dashWidth > 0) {
                     this.dataStream.createLine(lineDataInfo);
                     lineDataInfo.setX1(lineDataInfo.getX2() + spaceWidth);
                     lineDataInfo.setX2(lineDataInfo.getX1() + dashWidth);
                  }

                  return;
               } else {
                  dashWidth = (int)unitConv.pt2units(BorderPainter.dashWidthCalculator(h, w));
                  lineDataInfo.setX2(lineDataInfo.getX1());
                  lineDataInfo.setY2(lineDataInfo.getY1() + dashWidth);
                  ex2 = Math.round(y2);
                  spaceWidth = (int)(0.5F * (float)dashWidth);

                  while(lineDataInfo.getY2() <= ex2 && dashWidth > 0) {
                     this.dataStream.createLine(lineDataInfo);
                     lineDataInfo.setY1(lineDataInfo.getY2() + spaceWidth);
                     lineDataInfo.setY2(lineDataInfo.getY1() + dashWidth);
                  }

                  return;
               }
            case 36:
               if (borderPaintInfo.isHorizontal()) {
                  lineDataInfo.setX2(lineDataInfo.getX1() + lineDataInfo.getThickness());
                  lineDataInfo.setY2(lineDataInfo.getY1());
                  dashWidth = Math.round(x2);

                  while(lineDataInfo.getX1() + lineDataInfo.getThickness() < dashWidth) {
                     this.dataStream.createLine(lineDataInfo);
                     lineDataInfo.setX1(lineDataInfo.getX1() + 3 * lineDataInfo.getThickness());
                     lineDataInfo.setX2(lineDataInfo.getX1() + lineDataInfo.getThickness());
                  }

                  return;
               } else {
                  lineDataInfo.setX2(lineDataInfo.getX1());
                  lineDataInfo.setY2(lineDataInfo.getY1() + lineDataInfo.getThickness());
                  dashWidth = Math.round(y2);

                  while(lineDataInfo.getY1() + lineDataInfo.getThickness() < dashWidth) {
                     this.dataStream.createLine(lineDataInfo);
                     lineDataInfo.setY1(lineDataInfo.getY1() + 3 * lineDataInfo.getThickness());
                     lineDataInfo.setY2(lineDataInfo.getY1() + lineDataInfo.getThickness());
                  }

                  return;
               }
            case 37:
               int thickness3 = (int)Math.floor((double)(thickness / 3.0F));
               lineDataInfo.setThickness(thickness3);
               if (borderPaintInfo.isHorizontal()) {
                  lineDataInfo.setX2(Math.round(x2));
                  lineDataInfo.setY2(lineDataInfo.getY1());
                  this.dataStream.createLine(lineDataInfo);
                  dashWidth = thickness3 * 2;
                  lineDataInfo = new AFPLineDataInfo(lineDataInfo);
                  lineDataInfo.setY1(lineDataInfo.getY1() + dashWidth);
                  lineDataInfo.setY2(lineDataInfo.getY2() + dashWidth);
                  this.dataStream.createLine(lineDataInfo);
               } else {
                  lineDataInfo.setX2(lineDataInfo.getX1());
                  lineDataInfo.setY2(Math.round(y2));
                  this.dataStream.createLine(lineDataInfo);
                  dashWidth = thickness3 * 2;
                  lineDataInfo = new AFPLineDataInfo(lineDataInfo);
                  lineDataInfo.setX1(lineDataInfo.getX1() + dashWidth);
                  lineDataInfo.setX2(lineDataInfo.getX2() + dashWidth);
                  this.dataStream.createLine(lineDataInfo);
               }
               break;
            case 55:
            case 119:
               lineDataInfo.setX2(Math.round(x2));
               float colFactor = borderPaintInfo.getStyle() == 55 ? 0.4F : -0.4F;
               float h3 = (y2 - y1) / 3.0F;
               lineDataInfo.setColor(ColorUtil.lightenColor(borderPaintInfo.getColor(), -colFactor));
               lineDataInfo.setThickness(Math.round(h3));
               dashWidth = Math.round(y1);
               lineDataInfo.setY1(dashWidth);
               lineDataInfo.setY2(dashWidth);
               this.dataStream.createLine(lineDataInfo);
               lineDataInfo.setColor(borderPaintInfo.getColor());
               dashWidth = Math.round(y1 + h3);
               lineDataInfo.setY1(dashWidth);
               lineDataInfo.setY2(dashWidth);
               this.dataStream.createLine(lineDataInfo);
               lineDataInfo.setColor(ColorUtil.lightenColor(borderPaintInfo.getColor(), colFactor));
               dashWidth = Math.round(y1 + h3 + h3);
               lineDataInfo.setY1(dashWidth);
               lineDataInfo.setY2(dashWidth);
               this.dataStream.createLine(lineDataInfo);
            case 57:
               break;
            case 67:
            case 101:
            case 133:
            default:
               if (borderPaintInfo.isHorizontal()) {
                  lineDataInfo.setX2(Math.round(x2));
                  lineDataInfo.setY2(lineDataInfo.getY1());
               } else {
                  lineDataInfo.setX2(lineDataInfo.getX1());
                  lineDataInfo.setY2(Math.round(y2));
               }

               this.dataStream.createLine(lineDataInfo);
         }

      } else {
         log.error("Negative extent received. Border won't be painted.");
      }
   }
}
