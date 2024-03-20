package org.apache.batik.ext.awt.geom;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Linear implements Segment {
   public Point2D.Double p1;
   public Point2D.Double p2;

   public Linear() {
      this.p1 = new Point2D.Double();
      this.p2 = new Point2D.Double();
   }

   public Linear(double x1, double y1, double x2, double y2) {
      this.p1 = new Point2D.Double(x1, y1);
      this.p2 = new Point2D.Double(x2, y2);
   }

   public Linear(Point2D.Double p1, Point2D.Double p2) {
      this.p1 = p1;
      this.p2 = p2;
   }

   public Object clone() {
      return new Linear(new Point2D.Double(this.p1.x, this.p1.y), new Point2D.Double(this.p2.x, this.p2.y));
   }

   public Segment reverse() {
      return new Linear(new Point2D.Double(this.p2.x, this.p2.y), new Point2D.Double(this.p1.x, this.p1.y));
   }

   public double minX() {
      return this.p1.x < this.p2.x ? this.p1.x : this.p2.x;
   }

   public double maxX() {
      return this.p1.x > this.p2.x ? this.p1.x : this.p2.x;
   }

   public double minY() {
      return this.p1.y < this.p2.y ? this.p1.y : this.p2.y;
   }

   public double maxY() {
      return this.p1.y > this.p2.y ? this.p2.y : this.p1.y;
   }

   public Rectangle2D getBounds2D() {
      double x;
      double w;
      if (this.p1.x < this.p2.x) {
         x = this.p1.x;
         w = this.p2.x - this.p1.x;
      } else {
         x = this.p2.x;
         w = this.p1.x - this.p2.x;
      }

      double y;
      double h;
      if (this.p1.y < this.p2.y) {
         y = this.p1.y;
         h = this.p2.y - this.p1.y;
      } else {
         y = this.p2.y;
         h = this.p1.y - this.p2.y;
      }

      return new Rectangle2D.Double(x, y, w, h);
   }

   public Point2D.Double evalDt(double t) {
      double x = this.p2.x - this.p1.x;
      double y = this.p2.y - this.p1.y;
      return new Point2D.Double(x, y);
   }

   public Point2D.Double eval(double t) {
      double x = this.p1.x + t * (this.p2.x - this.p1.x);
      double y = this.p1.y + t * (this.p2.y - this.p1.y);
      return new Point2D.Double(x, y);
   }

   public Segment.SplitResults split(double y) {
      if (y != this.p1.y && y != this.p2.y) {
         if (y <= this.p1.y && y <= this.p2.y) {
            return null;
         } else if (y >= this.p1.y && y >= this.p2.y) {
            return null;
         } else {
            double t = (y - this.p1.y) / (this.p2.y - this.p1.y);
            Segment[] t0 = new Segment[]{this.getSegment(0.0, t)};
            Segment[] t1 = new Segment[]{this.getSegment(t, 1.0)};
            return this.p2.y < y ? new Segment.SplitResults(t0, t1) : new Segment.SplitResults(t1, t0);
         }
      } else {
         return null;
      }
   }

   public Segment getSegment(double t0, double t1) {
      Point2D.Double np1 = this.eval(t0);
      Point2D.Double np2 = this.eval(t1);
      return new Linear(np1, np2);
   }

   public Segment splitBefore(double t) {
      return new Linear(this.p1, this.eval(t));
   }

   public Segment splitAfter(double t) {
      return new Linear(this.eval(t), this.p2);
   }

   public void subdivide(Segment s0, Segment s1) {
      Linear l0 = null;
      Linear l1 = null;
      if (s0 instanceof Linear) {
         l0 = (Linear)s0;
      }

      if (s1 instanceof Linear) {
         l1 = (Linear)s1;
      }

      this.subdivide(l0, l1);
   }

   public void subdivide(double t, Segment s0, Segment s1) {
      Linear l0 = null;
      Linear l1 = null;
      if (s0 instanceof Linear) {
         l0 = (Linear)s0;
      }

      if (s1 instanceof Linear) {
         l1 = (Linear)s1;
      }

      this.subdivide(t, l0, l1);
   }

   public void subdivide(Linear l0, Linear l1) {
      if (l0 != null || l1 != null) {
         double x = (this.p1.x + this.p2.x) * 0.5;
         double y = (this.p1.y + this.p2.y) * 0.5;
         if (l0 != null) {
            l0.p1.x = this.p1.x;
            l0.p1.y = this.p1.y;
            l0.p2.x = x;
            l0.p2.y = y;
         }

         if (l1 != null) {
            l1.p1.x = x;
            l1.p1.y = y;
            l1.p2.x = this.p2.x;
            l1.p2.y = this.p2.y;
         }

      }
   }

   public void subdivide(double t, Linear l0, Linear l1) {
      if (l0 != null || l1 != null) {
         double x = this.p1.x + t * (this.p2.x - this.p1.x);
         double y = this.p1.y + t * (this.p2.y - this.p1.y);
         if (l0 != null) {
            l0.p1.x = this.p1.x;
            l0.p1.y = this.p1.y;
            l0.p2.x = x;
            l0.p2.y = y;
         }

         if (l1 != null) {
            l1.p1.x = x;
            l1.p1.y = y;
            l1.p2.x = this.p2.x;
            l1.p2.y = this.p2.y;
         }

      }
   }

   public double getLength() {
      double dx = this.p2.x - this.p1.x;
      double dy = this.p2.y - this.p1.y;
      return Math.sqrt(dx * dx + dy * dy);
   }

   public double getLength(double maxErr) {
      return this.getLength();
   }

   public String toString() {
      return "M" + this.p1.x + ',' + this.p1.y + 'L' + this.p2.x + ',' + this.p2.y;
   }
}
