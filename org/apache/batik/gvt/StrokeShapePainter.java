package org.apache.batik.gvt;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class StrokeShapePainter implements ShapePainter {
   protected Shape shape;
   protected Shape strokedShape;
   protected Stroke stroke;
   protected Paint paint;

   public StrokeShapePainter(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         this.shape = shape;
      }
   }

   public void setStroke(Stroke newStroke) {
      this.stroke = newStroke;
      this.strokedShape = null;
   }

   public Stroke getStroke() {
      return this.stroke;
   }

   public void setPaint(Paint newPaint) {
      this.paint = newPaint;
   }

   public Paint getPaint() {
      return this.paint;
   }

   public void paint(Graphics2D g2d) {
      if (this.stroke != null && this.paint != null) {
         g2d.setPaint(this.paint);
         g2d.setStroke(this.stroke);
         g2d.draw(this.shape);
      }

   }

   public Shape getPaintedArea() {
      if (this.paint != null && this.stroke != null) {
         if (this.strokedShape == null) {
            this.strokedShape = this.stroke.createStrokedShape(this.shape);
         }

         return this.strokedShape;
      } else {
         return null;
      }
   }

   public Rectangle2D getPaintedBounds2D() {
      Shape painted = this.getPaintedArea();
      return painted == null ? null : painted.getBounds2D();
   }

   public boolean inPaintedArea(Point2D pt) {
      Shape painted = this.getPaintedArea();
      return painted == null ? false : painted.contains(pt);
   }

   public Shape getSensitiveArea() {
      if (this.stroke == null) {
         return null;
      } else {
         if (this.strokedShape == null) {
            this.strokedShape = this.stroke.createStrokedShape(this.shape);
         }

         return this.strokedShape;
      }
   }

   public Rectangle2D getSensitiveBounds2D() {
      Shape sensitive = this.getSensitiveArea();
      return sensitive == null ? null : sensitive.getBounds2D();
   }

   public boolean inSensitiveArea(Point2D pt) {
      Shape sensitive = this.getSensitiveArea();
      return sensitive == null ? false : sensitive.contains(pt);
   }

   public void setShape(Shape shape) {
      if (shape == null) {
         throw new IllegalArgumentException();
      } else {
         this.shape = shape;
         this.strokedShape = null;
      }
   }

   public Shape getShape() {
      return this.shape;
   }
}
