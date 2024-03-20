package org.apache.batik.parser;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Reader;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;

public class AWTPathProducer implements PathHandler, ShapeProducer {
   protected ExtendedGeneralPath path;
   protected float currentX;
   protected float currentY;
   protected float xCenter;
   protected float yCenter;
   protected int windingRule;

   public static Shape createShape(Reader r, int wr) throws IOException, ParseException {
      PathParser p = new PathParser();
      AWTPathProducer ph = new AWTPathProducer();
      ph.setWindingRule(wr);
      p.setPathHandler(ph);
      p.parse(r);
      return ph.getShape();
   }

   public void setWindingRule(int i) {
      this.windingRule = i;
   }

   public int getWindingRule() {
      return this.windingRule;
   }

   public Shape getShape() {
      return this.path;
   }

   public void startPath() throws ParseException {
      this.currentX = 0.0F;
      this.currentY = 0.0F;
      this.xCenter = 0.0F;
      this.yCenter = 0.0F;
      this.path = new ExtendedGeneralPath(this.windingRule);
   }

   public void endPath() throws ParseException {
   }

   public void movetoRel(float x, float y) throws ParseException {
      this.path.moveTo(this.xCenter = this.currentX += x, this.yCenter = this.currentY += y);
   }

   public void movetoAbs(float x, float y) throws ParseException {
      this.path.moveTo(this.xCenter = this.currentX = x, this.yCenter = this.currentY = y);
   }

   public void closePath() throws ParseException {
      this.path.closePath();
      Point2D pt = this.path.getCurrentPoint();
      this.currentX = (float)pt.getX();
      this.currentY = (float)pt.getY();
   }

   public void linetoRel(float x, float y) throws ParseException {
      this.path.lineTo(this.xCenter = this.currentX += x, this.yCenter = this.currentY += y);
   }

   public void linetoAbs(float x, float y) throws ParseException {
      this.path.lineTo(this.xCenter = this.currentX = x, this.yCenter = this.currentY = y);
   }

   public void linetoHorizontalRel(float x) throws ParseException {
      this.path.lineTo(this.xCenter = this.currentX += x, this.yCenter = this.currentY);
   }

   public void linetoHorizontalAbs(float x) throws ParseException {
      this.path.lineTo(this.xCenter = this.currentX = x, this.yCenter = this.currentY);
   }

   public void linetoVerticalRel(float y) throws ParseException {
      this.path.lineTo(this.xCenter = this.currentX, this.yCenter = this.currentY += y);
   }

   public void linetoVerticalAbs(float y) throws ParseException {
      this.path.lineTo(this.xCenter = this.currentX, this.yCenter = this.currentY = y);
   }

   public void curvetoCubicRel(float x1, float y1, float x2, float y2, float x, float y) throws ParseException {
      this.path.curveTo(this.currentX + x1, this.currentY + y1, this.xCenter = this.currentX + x2, this.yCenter = this.currentY + y2, this.currentX += x, this.currentY += y);
   }

   public void curvetoCubicAbs(float x1, float y1, float x2, float y2, float x, float y) throws ParseException {
      this.path.curveTo(x1, y1, this.xCenter = x2, this.yCenter = y2, this.currentX = x, this.currentY = y);
   }

   public void curvetoCubicSmoothRel(float x2, float y2, float x, float y) throws ParseException {
      this.path.curveTo(this.currentX * 2.0F - this.xCenter, this.currentY * 2.0F - this.yCenter, this.xCenter = this.currentX + x2, this.yCenter = this.currentY + y2, this.currentX += x, this.currentY += y);
   }

   public void curvetoCubicSmoothAbs(float x2, float y2, float x, float y) throws ParseException {
      this.path.curveTo(this.currentX * 2.0F - this.xCenter, this.currentY * 2.0F - this.yCenter, this.xCenter = x2, this.yCenter = y2, this.currentX = x, this.currentY = y);
   }

   public void curvetoQuadraticRel(float x1, float y1, float x, float y) throws ParseException {
      this.path.quadTo(this.xCenter = this.currentX + x1, this.yCenter = this.currentY + y1, this.currentX += x, this.currentY += y);
   }

   public void curvetoQuadraticAbs(float x1, float y1, float x, float y) throws ParseException {
      this.path.quadTo(this.xCenter = x1, this.yCenter = y1, this.currentX = x, this.currentY = y);
   }

   public void curvetoQuadraticSmoothRel(float x, float y) throws ParseException {
      this.path.quadTo(this.xCenter = this.currentX * 2.0F - this.xCenter, this.yCenter = this.currentY * 2.0F - this.yCenter, this.currentX += x, this.currentY += y);
   }

   public void curvetoQuadraticSmoothAbs(float x, float y) throws ParseException {
      this.path.quadTo(this.xCenter = this.currentX * 2.0F - this.xCenter, this.yCenter = this.currentY * 2.0F - this.yCenter, this.currentX = x, this.currentY = y);
   }

   public void arcRel(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException {
      this.path.arcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, this.xCenter = this.currentX += x, this.yCenter = this.currentY += y);
   }

   public void arcAbs(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) throws ParseException {
      this.path.arcTo(rx, ry, xAxisRotation, largeArcFlag, sweepFlag, this.xCenter = this.currentX = x, this.yCenter = this.currentY = y);
   }
}
