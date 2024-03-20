package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import org.apache.fop.traits.BorderProps;

public class BorderPainter {
   protected static final int TOP = 0;
   protected static final int RIGHT = 1;
   protected static final int BOTTOM = 2;
   protected static final int LEFT = 3;
   protected static final int TOP_LEFT = 0;
   protected static final int TOP_RIGHT = 1;
   protected static final int BOTTOM_RIGHT = 2;
   protected static final int BOTTOM_LEFT = 3;
   public static final float DASHED_BORDER_SPACE_RATIO = 0.5F;
   protected static final float DASHED_BORDER_LENGTH_FACTOR = 2.0F;
   private final GraphicsPainter graphicsPainter;

   public BorderPainter(GraphicsPainter graphicsPainter) {
      this.graphicsPainter = graphicsPainter;
   }

   public void drawBorders(Rectangle borderRect, BorderProps bpsTop, BorderProps bpsBottom, BorderProps bpsLeft, BorderProps bpsRight, Color innerBackgroundColor) throws IFException {
      try {
         this.drawRoundedBorders(borderRect, bpsTop, bpsBottom, bpsLeft, bpsRight);
      } catch (IOException var8) {
         throw new IFException("IO error drawing borders", var8);
      }
   }

   private BorderProps sanitizeBorderProps(BorderProps bps) {
      return bps == null ? bps : (bps.width == 0 ? (BorderProps)null : bps);
   }

   protected void drawRectangularBorders(Rectangle borderRect, BorderProps bpsTop, BorderProps bpsBottom, BorderProps bpsLeft, BorderProps bpsRight) throws IOException {
      bpsTop = this.sanitizeBorderProps(bpsTop);
      bpsBottom = this.sanitizeBorderProps(bpsBottom);
      bpsLeft = this.sanitizeBorderProps(bpsLeft);
      bpsRight = this.sanitizeBorderProps(bpsRight);
      int startx = borderRect.x;
      int starty = borderRect.y;
      int width = borderRect.width;
      int height = borderRect.height;
      boolean[] b = new boolean[]{bpsTop != null, bpsRight != null, bpsBottom != null, bpsLeft != null};
      if (b[0] || b[1] || b[2] || b[3]) {
         int[] bw = new int[]{b[0] ? bpsTop.width : 0, b[1] ? bpsRight.width : 0, b[2] ? bpsBottom.width : 0, b[3] ? bpsLeft.width : 0};
         int[] clipw = new int[]{BorderProps.getClippedWidth(bpsTop), BorderProps.getClippedWidth(bpsRight), BorderProps.getClippedWidth(bpsBottom), BorderProps.getClippedWidth(bpsLeft)};
         starty += clipw[0];
         height -= clipw[0];
         height -= clipw[2];
         startx += clipw[3];
         width -= clipw[3];
         width -= clipw[1];
         boolean[] slant = new boolean[]{b[3] && b[0], b[0] && b[1], b[1] && b[2], b[2] && b[3]};
         int sy2;
         int ey1;
         int ey2;
         int outerx;
         int clipx;
         int innerx;
         int sy1a;
         int ey1a;
         if (bpsTop != null) {
            sy2 = slant[0] ? startx + bw[3] - clipw[3] : startx;
            ey1 = startx + width;
            ey2 = slant[1] ? ey1 - bw[1] + clipw[1] : ey1;
            outerx = starty - clipw[0];
            clipx = outerx + clipw[0];
            innerx = outerx + bw[0];
            this.saveGraphicsState();
            this.moveTo(startx, clipx);
            sy1a = startx;
            ey1a = ey1;
            if (this.isCollapseOuter(bpsTop)) {
               if (this.isCollapseOuter(bpsLeft)) {
                  sy1a = startx - clipw[3];
               }

               if (this.isCollapseOuter(bpsRight)) {
                  ey1a = ey1 + clipw[1];
               }

               this.lineTo(sy1a, outerx);
               this.lineTo(ey1a, outerx);
            }

            this.lineTo(ey1, clipx);
            this.lineTo(ey2, innerx);
            this.lineTo(sy2, innerx);
            this.closePath();
            this.clip();
            this.drawBorderLine(sy1a, outerx, ey1a, innerx, true, true, bpsTop.style, bpsTop.color);
            this.restoreGraphicsState();
         }

         if (bpsRight != null) {
            sy2 = slant[1] ? starty + bw[0] - clipw[0] : starty;
            ey1 = starty + height;
            ey2 = slant[2] ? ey1 - bw[2] + clipw[2] : ey1;
            outerx = startx + width + clipw[1];
            clipx = outerx - clipw[1];
            innerx = outerx - bw[1];
            this.saveGraphicsState();
            this.moveTo(clipx, starty);
            sy1a = starty;
            ey1a = ey1;
            if (this.isCollapseOuter(bpsRight)) {
               if (this.isCollapseOuter(bpsTop)) {
                  sy1a = starty - clipw[0];
               }

               if (this.isCollapseOuter(bpsBottom)) {
                  ey1a = ey1 + clipw[2];
               }

               this.lineTo(outerx, sy1a);
               this.lineTo(outerx, ey1a);
            }

            this.lineTo(clipx, ey1);
            this.lineTo(innerx, ey2);
            this.lineTo(innerx, sy2);
            this.closePath();
            this.clip();
            this.drawBorderLine(innerx, sy1a, outerx, ey1a, false, false, bpsRight.style, bpsRight.color);
            this.restoreGraphicsState();
         }

         if (bpsBottom != null) {
            sy2 = slant[3] ? startx + bw[3] - clipw[3] : startx;
            ey1 = startx + width;
            ey2 = slant[2] ? ey1 - bw[1] + clipw[1] : ey1;
            outerx = starty + height + clipw[2];
            clipx = outerx - clipw[2];
            innerx = outerx - bw[2];
            this.saveGraphicsState();
            this.moveTo(ey1, clipx);
            sy1a = startx;
            ey1a = ey1;
            if (this.isCollapseOuter(bpsBottom)) {
               if (this.isCollapseOuter(bpsLeft)) {
                  sy1a = startx - clipw[3];
               }

               if (this.isCollapseOuter(bpsRight)) {
                  ey1a = ey1 + clipw[1];
               }

               this.lineTo(ey1a, outerx);
               this.lineTo(sy1a, outerx);
            }

            this.lineTo(startx, clipx);
            this.lineTo(sy2, innerx);
            this.lineTo(ey2, innerx);
            this.closePath();
            this.clip();
            this.drawBorderLine(sy1a, innerx, ey1a, outerx, true, false, bpsBottom.style, bpsBottom.color);
            this.restoreGraphicsState();
         }

         if (bpsLeft != null) {
            sy2 = slant[0] ? starty + bw[0] - clipw[0] : starty;
            ey1 = starty + height;
            ey2 = slant[3] ? ey1 - bw[2] + clipw[2] : ey1;
            outerx = startx - clipw[3];
            clipx = outerx + clipw[3];
            innerx = outerx + bw[3];
            this.saveGraphicsState();
            this.moveTo(clipx, ey1);
            sy1a = starty;
            ey1a = ey1;
            if (this.isCollapseOuter(bpsLeft)) {
               if (this.isCollapseOuter(bpsTop)) {
                  sy1a = starty - clipw[0];
               }

               if (this.isCollapseOuter(bpsBottom)) {
                  ey1a = ey1 + clipw[2];
               }

               this.lineTo(outerx, ey1a);
               this.lineTo(outerx, sy1a);
            }

            this.lineTo(clipx, starty);
            this.lineTo(innerx, sy2);
            this.lineTo(innerx, ey2);
            this.closePath();
            this.clip();
            this.drawBorderLine(outerx, sy1a, innerx, ey1a, false, true, bpsLeft.style, bpsLeft.color);
            this.restoreGraphicsState();
         }

      }
   }

   private boolean isCollapseOuter(BorderProps bp) {
      return bp != null && bp.isCollapseOuter();
   }

   public static float dashWidthCalculator(float borderLength, float borderWidth) {
      float dashWidth = 2.0F * borderWidth;
      if (borderWidth < 3.0F) {
         dashWidth = 6.0F * borderWidth;
      }

      int period = (int)((borderLength - dashWidth) / dashWidth / 1.5F);
      period = period < 0 ? 0 : period;
      return borderLength / ((float)period * 1.5F + 1.0F);
   }

   protected void drawRoundedBorders(Rectangle borderRect, BorderProps beforeBorderProps, BorderProps afterBorderProps, BorderProps startBorderProps, BorderProps endBorderProps) throws IOException {
      BorderSegment before = borderSegmentForBefore(beforeBorderProps);
      BorderSegment after = borderSegmentForAfter(afterBorderProps);
      BorderSegment start = borderSegmentForStart(startBorderProps);
      BorderSegment end = borderSegmentForEnd(endBorderProps);
      if (before.getWidth() != 0 || after.getWidth() != 0 || start.getWidth() != 0 || end.getWidth() != 0) {
         int startx = borderRect.x + start.getClippedWidth();
         int starty = borderRect.y + before.getClippedWidth();
         int width = borderRect.width - start.getClippedWidth() - end.getClippedWidth();
         int height = borderRect.height - before.getClippedWidth() - after.getClippedWidth();
         double cornerCorrectionFactor = calculateCornerScaleCorrection(width, height, before, after, start, end);
         this.drawBorderSegment(start, before, end, 0, width, startx, starty, cornerCorrectionFactor);
         this.drawBorderSegment(before, end, after, 1, height, startx + width, starty, cornerCorrectionFactor);
         this.drawBorderSegment(end, after, start, 2, width, startx + width, starty + height, cornerCorrectionFactor);
         this.drawBorderSegment(after, start, before, 3, height, startx, starty + height, cornerCorrectionFactor);
      }
   }

   private void drawBorderSegment(BorderSegment start, BorderSegment before, BorderSegment end, int orientation, int width, int x, int y, double cornerCorrectionFactor) throws IOException {
      if (before.getWidth() != 0) {
         int sx2 = start.getWidth() - start.getClippedWidth();
         int ex2 = width - end.getWidth() + end.getClippedWidth();
         int outery = -before.getClippedWidth();
         int innery = outery + before.getWidth();
         int ellipseSBRadiusX = correctRadius(cornerCorrectionFactor, start.getRadiusEnd());
         int ellipseSBRadiusY = correctRadius(cornerCorrectionFactor, before.getRadiusStart());
         int ellipseBERadiusX = correctRadius(cornerCorrectionFactor, end.getRadiusStart());
         int ellipseBERadiusY = correctRadius(cornerCorrectionFactor, before.getRadiusEnd());
         this.saveGraphicsState();
         this.translateCoordinates(x, y);
         if (orientation != 0) {
            this.rotateCoordinates(Math.PI * (double)orientation / 2.0);
         }

         int ellipseBEX = width - ellipseBERadiusX;
         int sx1a = 0;
         int ex1a = width;
         double[] innerJoinMetrics;
         double sbInnerJoinAngle;
         if (ellipseSBRadiusX != 0 && ellipseSBRadiusY != 0) {
            innerJoinMetrics = this.getCornerBorderJoinMetrics((double)ellipseSBRadiusX, (double)ellipseSBRadiusY, (double)sx2, (double)innery);
            sbInnerJoinAngle = innerJoinMetrics[0];
            double outerJoinPointY = innerJoinMetrics[1];
            double sbJoinAngle = innerJoinMetrics[2];
            this.moveTo((int)sbInnerJoinAngle, (int)outerJoinPointY);
            this.arcTo(Math.PI + sbJoinAngle, 4.71238898038469, ellipseSBRadiusX, ellipseSBRadiusY, ellipseSBRadiusX, ellipseSBRadiusY);
         } else {
            this.moveTo(0, 0);
            if (before.isCollapseOuter()) {
               if (start.isCollapseOuter()) {
                  sx1a -= start.getClippedWidth();
               }

               if (end.isCollapseOuter()) {
                  ex1a = width + end.getClippedWidth();
               }

               this.lineTo(sx1a, outery);
               this.lineTo(ex1a, outery);
            }
         }

         if (ellipseBERadiusX != 0 && ellipseBERadiusY != 0) {
            innerJoinMetrics = this.getCornerBorderJoinMetrics((double)ellipseBERadiusX, (double)ellipseBERadiusY, (double)(width - ex2), (double)innery);
            sbInnerJoinAngle = width == ex2 ? 1.5707963267948966 : 1.5707963267948966 - innerJoinMetrics[2];
            this.lineTo(ellipseBEX, 0);
            this.arcTo(4.71238898038469, 4.71238898038469 + sbInnerJoinAngle, ellipseBEX, ellipseBERadiusY, ellipseBERadiusX, ellipseBERadiusY);
            if (ellipseBEX < ex2 && ellipseBERadiusY > innery) {
               double[] innerJoinMetrics = this.getCornerBorderJoinMetrics((double)ex2 - (double)ellipseBEX, (double)ellipseBERadiusY - (double)innery, (double)(width - ex2), (double)innery);
               double innerJoinPointX = innerJoinMetrics[0];
               double innerJoinPointY = innerJoinMetrics[1];
               double beInnerJoinAngle = 1.5707963267948966 - innerJoinMetrics[2];
               this.lineTo((int)((double)ex2 - innerJoinPointX), (int)(innerJoinPointY + (double)innery));
               this.arcTo(beInnerJoinAngle + 4.71238898038469, 4.71238898038469, ellipseBEX, ellipseBERadiusY, ex2 - ellipseBEX, ellipseBERadiusY - innery);
            } else {
               this.lineTo(ex2, innery);
            }
         } else {
            this.lineTo(width, 0);
            this.lineTo(ex2, innery);
         }

         if (ellipseSBRadiusX == 0) {
            this.lineTo(sx2, innery);
         } else if (ellipseSBRadiusX > sx2 && ellipseSBRadiusY > innery) {
            innerJoinMetrics = this.getCornerBorderJoinMetrics((double)(ellipseSBRadiusX - sx2), (double)(ellipseSBRadiusY - innery), (double)sx2, (double)innery);
            sbInnerJoinAngle = innerJoinMetrics[2];
            this.lineTo(ellipseSBRadiusX, innery);
            this.arcTo(4.71238898038469, sbInnerJoinAngle + Math.PI, ellipseSBRadiusX, ellipseSBRadiusY, ellipseSBRadiusX - sx2, ellipseSBRadiusY - innery);
         } else {
            this.lineTo(sx2, innery);
         }

         this.closePath();
         this.clip();
         if (ellipseBERadiusY == 0 && ellipseSBRadiusY == 0) {
            this.drawBorderLine(sx1a, outery, ex1a, innery, true, true, before.getStyle(), before.getColor());
         } else {
            int innerFillY = Math.max(Math.max(ellipseBERadiusY, ellipseSBRadiusY), innery);
            this.drawBorderLine(sx1a, outery, ex1a, innerFillY, true, true, before.getStyle(), before.getColor());
         }

         this.restoreGraphicsState();
      }

   }

   private static int correctRadius(double cornerCorrectionFactor, int radius) {
      return (int)Math.round(cornerCorrectionFactor * (double)radius);
   }

   private static BorderSegment borderSegmentForBefore(BorderProps before) {
      return BorderPainter.AbstractBorderSegment.asBorderSegment(before);
   }

   private static BorderSegment borderSegmentForAfter(BorderProps after) {
      return BorderPainter.AbstractBorderSegment.asFlippedBorderSegment(after);
   }

   private static BorderSegment borderSegmentForStart(BorderProps start) {
      return BorderPainter.AbstractBorderSegment.asFlippedBorderSegment(start);
   }

   private static BorderSegment borderSegmentForEnd(BorderProps end) {
      return BorderPainter.AbstractBorderSegment.asBorderSegment(end);
   }

   private double[] getCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY, double xWidth, double yWidth) {
      return xWidth > 0.0 ? this.getCornerBorderJoinMetrics(ellipseCenterX, ellipseCenterY, yWidth / xWidth) : new double[]{0.0, ellipseCenterY, 0.0};
   }

   private double[] getCornerBorderJoinMetrics(double ellipseCenterX, double ellipseCenterY, double borderWidthRatio) {
      double x = ellipseCenterY * ellipseCenterX * (ellipseCenterY + ellipseCenterX * borderWidthRatio - Math.sqrt(2.0 * ellipseCenterX * ellipseCenterY * borderWidthRatio)) / (ellipseCenterY * ellipseCenterY + ellipseCenterX * ellipseCenterX * borderWidthRatio * borderWidthRatio);
      double y = borderWidthRatio * x;
      return new double[]{x, y, Math.atan((ellipseCenterY - y) / (ellipseCenterX - x))};
   }

   public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) throws IOException {
      BorderSegment before = borderSegmentForBefore(bpsBefore);
      BorderSegment after = borderSegmentForAfter(bpsAfter);
      BorderSegment start = borderSegmentForStart(bpsStart);
      BorderSegment end = borderSegmentForEnd(bpsEnd);
      int startx = rect.x;
      int starty = rect.y;
      int width = rect.width;
      int height = rect.height;
      double correctionFactor = calculateCornerCorrectionFactor(width + start.getWidth() + end.getWidth(), height + before.getWidth() + after.getWidth(), bpsBefore, bpsAfter, bpsStart, bpsEnd);
      Corner cornerBeforeEnd = BorderPainter.Corner.createBeforeEndCorner(before, end, correctionFactor);
      Corner cornerEndAfter = BorderPainter.Corner.createEndAfterCorner(end, after, correctionFactor);
      Corner cornerAfterStart = BorderPainter.Corner.createAfterStartCorner(after, start, correctionFactor);
      Corner cornerStartBefore = BorderPainter.Corner.createStartBeforeCorner(start, before, correctionFactor);
      (new PathPainter(startx + cornerStartBefore.radiusX, starty)).lineHorizTo(width - cornerStartBefore.radiusX - cornerBeforeEnd.radiusX).drawCorner(cornerBeforeEnd).lineVertTo(height - cornerBeforeEnd.radiusY - cornerEndAfter.radiusY).drawCorner(cornerEndAfter).lineHorizTo(cornerEndAfter.radiusX + cornerAfterStart.radiusX - width).drawCorner(cornerAfterStart).lineVertTo(cornerAfterStart.radiusY + cornerStartBefore.radiusY - height).drawCorner(cornerStartBefore);
      this.clip();
   }

   protected static double calculateCornerCorrectionFactor(int width, int height, BorderProps before, BorderProps after, BorderProps start, BorderProps end) {
      return calculateCornerScaleCorrection(width, height, borderSegmentForBefore(before), borderSegmentForAfter(after), borderSegmentForStart(start), borderSegmentForEnd(end));
   }

   protected static double calculateCornerScaleCorrection(int width, int height, BorderSegment before, BorderSegment after, BorderSegment start, BorderSegment end) {
      return BorderPainter.CornerScaleCorrectionCalculator.calculate(width, height, before, after, start, end);
   }

   private void drawBorderLine(int x1, int y1, int x2, int y2, boolean horz, boolean startOrBefore, int style, Color color) throws IOException {
      this.graphicsPainter.drawBorderLine(x1, y1, x2, y2, horz, startOrBefore, style, color);
   }

   private void moveTo(int x, int y) throws IOException {
      this.graphicsPainter.moveTo(x, y);
   }

   private void lineTo(int x, int y) throws IOException {
      this.graphicsPainter.lineTo(x, y);
   }

   private void arcTo(double startAngle, double endAngle, int cx, int cy, int width, int height) throws IOException {
      this.graphicsPainter.arcTo(startAngle, endAngle, cx, cy, width, height);
   }

   private void rotateCoordinates(double angle) throws IOException {
      this.graphicsPainter.rotateCoordinates(angle);
   }

   private void translateCoordinates(int xTranslate, int yTranslate) throws IOException {
      this.graphicsPainter.translateCoordinates(xTranslate, yTranslate);
   }

   private void closePath() throws IOException {
      this.graphicsPainter.closePath();
   }

   private void clip() throws IOException {
      this.graphicsPainter.clip();
   }

   private void saveGraphicsState() throws IOException {
      this.graphicsPainter.saveGraphicsState();
   }

   private void restoreGraphicsState() throws IOException {
      this.graphicsPainter.restoreGraphicsState();
   }

   private static final class CornerScaleCorrectionCalculator {
      private double correctionFactor = 1.0;

      private CornerScaleCorrectionCalculator(int width, int height, BorderSegment before, BorderSegment after, BorderSegment start, BorderSegment end) {
         this.calculateForSegment(width, start, before, end);
         this.calculateForSegment(height, before, end, after);
         this.calculateForSegment(width, end, after, start);
         this.calculateForSegment(height, after, start, before);
      }

      public static double calculate(int width, int height, BorderSegment before, BorderSegment after, BorderSegment start, BorderSegment end) {
         return (new CornerScaleCorrectionCalculator(width, height, before, after, start, end)).correctionFactor;
      }

      private void calculateForSegment(int width, BorderSegment bpsStart, BorderSegment bpsBefore, BorderSegment bpsEnd) {
         if (bpsBefore.isSpecified()) {
            double ellipseExtent = (double)(bpsStart.getRadiusEnd() + bpsEnd.getRadiusStart());
            if (ellipseExtent > 0.0) {
               double thisCorrectionFactor = (double)width / ellipseExtent;
               if (thisCorrectionFactor < this.correctionFactor) {
                  this.correctionFactor = thisCorrectionFactor;
               }
            }
         }

      }
   }

   private final class PathPainter {
      private int x;
      private int y;

      PathPainter(int x, int y) throws IOException {
         this.moveTo(x, y);
      }

      private void moveTo(int x, int y) throws IOException {
         this.x += x;
         this.y += y;
         BorderPainter.this.moveTo(this.x, this.y);
      }

      public PathPainter lineTo(int x, int y) throws IOException {
         this.x += x;
         this.y += y;
         BorderPainter.this.lineTo(this.x, this.y);
         return this;
      }

      public PathPainter lineHorizTo(int x) throws IOException {
         return this.lineTo(x, 0);
      }

      public PathPainter lineVertTo(int y) throws IOException {
         return this.lineTo(0, y);
      }

      PathPainter drawCorner(Corner corner) throws IOException {
         if (corner.radiusX == 0 && corner.radiusY == 0) {
            return this;
         } else if (corner.radiusX != 0 && corner.radiusY != 0) {
            BorderPainter.this.arcTo(corner.angles.start, corner.angles.end, this.x + corner.centerX, this.y + corner.centerY, corner.radiusX, corner.radiusY);
            this.x += corner.incrementX;
            this.y += corner.incrementY;
            return this;
         } else {
            this.x += corner.incrementX;
            this.y += corner.incrementY;
            BorderPainter.this.lineTo(this.x, this.y);
            return this;
         }
      }
   }

   private static final class Corner {
      private static final Corner SQUARE = new Corner(0, 0, (CornerAngles)null, 0, 0, 0, 0);
      private final int radiusX;
      private final int radiusY;
      private final CornerAngles angles;
      private final int centerX;
      private final int centerY;
      private final int incrementX;
      private final int incrementY;

      private Corner(int radiusX, int radiusY, CornerAngles angles, int ellipseOffsetX, int ellipseOffsetY, int incrementX, int incrementY) {
         this.radiusX = radiusX;
         this.radiusY = radiusY;
         this.angles = angles;
         this.centerX = ellipseOffsetX;
         this.centerY = ellipseOffsetY;
         this.incrementX = incrementX;
         this.incrementY = incrementY;
      }

      private static int extentFromRadiusStart(BorderSegment border, double correctionFactor) {
         return extentFromRadius(border.getRadiusStart(), border, correctionFactor);
      }

      private static int extentFromRadiusEnd(BorderSegment border, double correctionFactor) {
         return extentFromRadius(border.getRadiusEnd(), border, correctionFactor);
      }

      private static int extentFromRadius(int radius, BorderSegment border, double correctionFactor) {
         return Math.max((int)((double)radius * correctionFactor) - border.getWidth(), 0);
      }

      public static Corner createBeforeEndCorner(BorderSegment before, BorderSegment end, double correctionFactor) {
         int width = end.getRadiusStart();
         int height = before.getRadiusEnd();
         if (width != 0 && height != 0) {
            int x = extentFromRadiusStart(end, correctionFactor);
            int y = extentFromRadiusEnd(before, correctionFactor);
            return new Corner(x, y, BorderPainter.CornerAngles.BEFORE_END, 0, y, x, y);
         } else {
            return SQUARE;
         }
      }

      public static Corner createEndAfterCorner(BorderSegment end, BorderSegment after, double correctionFactor) {
         int width = end.getRadiusEnd();
         int height = after.getRadiusStart();
         if (width != 0 && height != 0) {
            int x = extentFromRadiusEnd(end, correctionFactor);
            int y = extentFromRadiusStart(after, correctionFactor);
            return new Corner(x, y, BorderPainter.CornerAngles.END_AFTER, -x, 0, -x, y);
         } else {
            return SQUARE;
         }
      }

      public static Corner createAfterStartCorner(BorderSegment after, BorderSegment start, double correctionFactor) {
         int width = start.getRadiusStart();
         int height = after.getRadiusEnd();
         if (width != 0 && height != 0) {
            int x = extentFromRadiusStart(start, correctionFactor);
            int y = extentFromRadiusEnd(after, correctionFactor);
            return new Corner(x, y, BorderPainter.CornerAngles.AFTER_START, 0, -y, -x, -y);
         } else {
            return SQUARE;
         }
      }

      public static Corner createStartBeforeCorner(BorderSegment start, BorderSegment before, double correctionFactor) {
         int width = start.getRadiusEnd();
         int height = before.getRadiusStart();
         if (width != 0 && height != 0) {
            int x = extentFromRadiusEnd(start, correctionFactor);
            int y = extentFromRadiusStart(before, correctionFactor);
            return new Corner(x, y, BorderPainter.CornerAngles.START_BEFORE, x, 0, x, -y);
         } else {
            return SQUARE;
         }
      }
   }

   private static enum CornerAngles {
      BEFORE_END(4.71238898038469, 0.0),
      END_AFTER(0.0, 1.5707963267948966),
      AFTER_START(1.5707963267948966, Math.PI),
      START_BEFORE(Math.PI, 4.71238898038469);

      private final double start;
      private final double end;

      private CornerAngles(double start, double end) {
         this.start = start;
         this.end = end;
      }
   }

   private abstract static class AbstractBorderSegment implements BorderSegment {
      private AbstractBorderSegment() {
      }

      private static BorderSegment asBorderSegment(BorderProps borderProps) {
         return (BorderSegment)(borderProps == null ? BorderPainter.AbstractBorderSegment.NullBorderSegment.INSTANCE : new WrappingBorderSegment(borderProps));
      }

      private static BorderSegment asFlippedBorderSegment(BorderProps borderProps) {
         return (BorderSegment)(borderProps == null ? BorderPainter.AbstractBorderSegment.NullBorderSegment.INSTANCE : new FlippedBorderSegment(borderProps));
      }

      public boolean isSpecified() {
         return !(this instanceof NullBorderSegment);
      }

      // $FF: synthetic method
      AbstractBorderSegment(Object x0) {
         this();
      }

      private static final class NullBorderSegment extends AbstractBorderSegment {
         public static final NullBorderSegment INSTANCE = new NullBorderSegment();

         private NullBorderSegment() {
            super(null);
         }

         public int getWidth() {
            return 0;
         }

         public int getClippedWidth() {
            return 0;
         }

         public int getRadiusStart() {
            return 0;
         }

         public int getRadiusEnd() {
            return 0;
         }

         public boolean isCollapseOuter() {
            return false;
         }

         public Color getColor() {
            throw new UnsupportedOperationException();
         }

         public int getStyle() {
            throw new UnsupportedOperationException();
         }

         public boolean isSpecified() {
            return false;
         }
      }

      private static class FlippedBorderSegment extends WrappingBorderSegment {
         FlippedBorderSegment(BorderProps borderProps) {
            super(borderProps);
         }

         public int getRadiusStart() {
            return this.borderProps.getRadiusEnd();
         }

         public int getRadiusEnd() {
            return this.borderProps.getRadiusStart();
         }
      }

      private static class WrappingBorderSegment extends AbstractBorderSegment {
         protected final BorderProps borderProps;
         private final int clippedWidth;

         WrappingBorderSegment(BorderProps borderProps) {
            super(null);
            this.borderProps = borderProps;
            this.clippedWidth = BorderProps.getClippedWidth(borderProps);
         }

         public int getStyle() {
            return this.borderProps.style;
         }

         public Color getColor() {
            return this.borderProps.color;
         }

         public int getWidth() {
            return this.borderProps.width;
         }

         public int getClippedWidth() {
            return this.clippedWidth;
         }

         public boolean isCollapseOuter() {
            return this.borderProps.isCollapseOuter();
         }

         public int getRadiusStart() {
            return this.borderProps.getRadiusStart();
         }

         public int getRadiusEnd() {
            return this.borderProps.getRadiusEnd();
         }
      }
   }

   private interface BorderSegment {
      Color getColor();

      int getStyle();

      int getWidth();

      int getClippedWidth();

      int getRadiusStart();

      int getRadiusEnd();

      boolean isCollapseOuter();

      boolean isSpecified();
   }
}
