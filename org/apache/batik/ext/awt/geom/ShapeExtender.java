package org.apache.batik.ext.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ShapeExtender implements ExtendedShape {
   Shape shape;

   public ShapeExtender(Shape shape) {
      this.shape = shape;
   }

   public boolean contains(double x, double y) {
      return this.shape.contains(x, y);
   }

   public boolean contains(double x, double y, double w, double h) {
      return this.shape.contains(x, y, w, h);
   }

   public boolean contains(Point2D p) {
      return this.shape.contains(p);
   }

   public boolean contains(Rectangle2D r) {
      return this.shape.contains(r);
   }

   public Rectangle getBounds() {
      return this.shape.getBounds();
   }

   public Rectangle2D getBounds2D() {
      return this.shape.getBounds2D();
   }

   public PathIterator getPathIterator(AffineTransform at) {
      return this.shape.getPathIterator(at);
   }

   public PathIterator getPathIterator(AffineTransform at, double flatness) {
      return this.shape.getPathIterator(at, flatness);
   }

   public ExtendedPathIterator getExtendedPathIterator() {
      return new EPIWrap(this.shape.getPathIterator((AffineTransform)null));
   }

   public boolean intersects(double x, double y, double w, double h) {
      return this.shape.intersects(x, y, w, h);
   }

   public boolean intersects(Rectangle2D r) {
      return this.shape.intersects(r);
   }

   public static class EPIWrap implements ExtendedPathIterator {
      PathIterator pi = null;

      public EPIWrap(PathIterator pi) {
         this.pi = pi;
      }

      public int currentSegment() {
         float[] coords = new float[6];
         return this.pi.currentSegment(coords);
      }

      public int currentSegment(double[] coords) {
         return this.pi.currentSegment(coords);
      }

      public int currentSegment(float[] coords) {
         return this.pi.currentSegment(coords);
      }

      public int getWindingRule() {
         return this.pi.getWindingRule();
      }

      public boolean isDone() {
         return this.pi.isDone();
      }

      public void next() {
         this.pi.next();
      }
   }
}
