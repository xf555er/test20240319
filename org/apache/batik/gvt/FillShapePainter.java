package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class FillShapePainter implements ShapePainter {
   protected Shape shape;
   protected Paint paint;

   public FillShapePainter(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException("Shape can not be null!");
      } else {
         this.shape = shape;
      }
   }

   public void setPaint(Paint newPaint) {
      this.paint = newPaint;
   }

   public Paint getPaint() {
      return this.paint;
   }

   public void paint(Graphics2D g2d) {
      if (this.paint != null) {
         g2d.setPaint(this.paint);
         g2d.fill(this.shape);
      }

   }

   public Shape getPaintedArea() {
      return this.paint == null ? null : this.shape;
   }

   public Rectangle2D getPaintedBounds2D() {
      return this.paint != null && this.shape != null ? this.shape.getBounds2D() : null;
   }

   public boolean inPaintedArea(Point2D pt) {
      return this.paint != null && this.shape != null ? this.shape.contains(pt) : false;
   }

   public Shape getSensitiveArea() {
      return this.shape;
   }

   public Rectangle2D getSensitiveBounds2D() {
      return this.shape == null ? null : this.shape.getBounds2D();
   }

   public boolean inSensitiveArea(Point2D pt) {
      return this.shape == null ? false : this.shape.contains(pt);
   }

   public void setShape(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         this.shape = shape;
      }
   }

   public Shape getShape() {
      return this.shape;
   }
}
