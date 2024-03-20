package org.apache.batik.ext.awt.geom;

import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

public class Quadradic extends AbstractSegment {
   public Point2D.Double p1;
   public Point2D.Double p2;
   public Point2D.Double p3;
   static int count = 0;

   public Quadradic() {
      this.p1 = new Point2D.Double();
      this.p2 = new Point2D.Double();
      this.p3 = new Point2D.Double();
   }

   public Quadradic(double x1, double y1, double x2, double y2, double x3, double y3) {
      this.p1 = new Point2D.Double(x1, y1);
      this.p2 = new Point2D.Double(x2, y2);
      this.p3 = new Point2D.Double(x3, y3);
   }

   public Quadradic(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3) {
      this.p1 = p1;
      this.p2 = p2;
      this.p3 = p3;
   }

   public Object clone() {
      return new Quadradic(new Point2D.Double(this.p1.x, this.p1.y), new Point2D.Double(this.p2.x, this.p2.y), new Point2D.Double(this.p3.x, this.p3.y));
   }

   public Segment reverse() {
      return new Quadradic(new Point2D.Double(this.p3.x, this.p3.y), new Point2D.Double(this.p2.x, this.p2.y), new Point2D.Double(this.p1.x, this.p1.y));
   }

   private void getMinMax(double p1, double p2, double p3, double[] minMax) {
      if (p3 > p1) {
         minMax[0] = p1;
         minMax[1] = p3;
      } else {
         minMax[0] = p3;
         minMax[1] = p1;
      }

      double a = p1 - 2.0 * p2 + p3;
      double b = p3 - p2;
      if (a != 0.0) {
         double tv = b / a;
         if (!(tv <= 0.0) && !(tv >= 1.0)) {
            tv = ((p1 - 2.0 * p2 + p3) * tv + 2.0 * (p2 - p1)) * tv + p1;
            if (tv < minMax[0]) {
               minMax[0] = tv;
            } else if (tv > minMax[1]) {
               minMax[1] = tv;
            }

         }
      }
   }

   public double minX() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.x, this.p2.x, this.p3.x, minMax);
      return minMax[0];
   }

   public double maxX() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.x, this.p2.x, this.p3.x, minMax);
      return minMax[1];
   }

   public double minY() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.y, this.p2.y, this.p3.y, minMax);
      return minMax[0];
   }

   public double maxY() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.y, this.p2.y, this.p3.y, minMax);
      return minMax[1];
   }

   public Rectangle2D getBounds2D() {
      double[] minMaxX = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.x, this.p2.x, this.p3.x, minMaxX);
      double[] minMaxY = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.y, this.p2.y, this.p3.y, minMaxY);
      return new Rectangle2D.Double(minMaxX[0], minMaxY[0], minMaxX[1] - minMaxX[0], minMaxY[1] - minMaxY[0]);
   }

   protected int findRoots(double y, double[] roots) {
      double[] eqn = new double[]{this.p1.y - y, 2.0 * (this.p2.y - this.p1.y), this.p1.y - 2.0 * this.p2.y + this.p3.y};
      return QuadCurve2D.solveQuadratic(eqn, roots);
   }

   public Point2D.Double evalDt(double t) {
      double x = 2.0 * (this.p1.x - 2.0 * this.p2.x + this.p3.x) * t + 2.0 * (this.p2.x - this.p1.x);
      double y = 2.0 * (this.p1.y - 2.0 * this.p2.y + this.p3.y) * t + 2.0 * (this.p2.y - this.p1.y);
      return new Point2D.Double(x, y);
   }

   public Point2D.Double eval(double t) {
      double x = ((this.p1.x - 2.0 * this.p2.x + this.p3.x) * t + 2.0 * (this.p2.x - this.p1.x)) * t + this.p1.x;
      double y = ((this.p1.y - 2.0 * this.p2.y + this.p3.y) * t + 2.0 * (this.p2.y - this.p1.y)) * t + this.p1.y;
      return new Point2D.Double(x, y);
   }

   public Segment getSegment(double t0, double t1) {
      double dt = t1 - t0;
      Point2D.Double np1 = this.eval(t0);
      Point2D.Double dp1 = this.evalDt(t0);
      Point2D.Double np2 = new Point2D.Double(np1.x + 0.5 * dt * dp1.x, np1.y + 0.5 * dt * dp1.y);
      Point2D.Double np3 = this.eval(t1);
      return new Quadradic(np1, np2, np3);
   }

   public void subdivide(Quadradic q0, Quadradic q1) {
      if (q0 != null || q1 != null) {
         double x = (this.p1.x - 2.0 * this.p2.x + this.p3.x) * 0.25 + (this.p2.x - this.p1.x) + this.p1.x;
         double y = (this.p1.y - 2.0 * this.p2.y + this.p3.y) * 0.25 + (this.p2.y - this.p1.y) + this.p1.y;
         double dx = (this.p1.x - 2.0 * this.p2.x + this.p3.x) * 0.25 + (this.p2.x - this.p1.x) * 0.5;
         double dy = (this.p1.y - 2.0 * this.p2.y + this.p3.y) * 0.25 + (this.p2.y - this.p1.y) * 0.5;
         if (q0 != null) {
            q0.p1.x = this.p1.x;
            q0.p1.y = this.p1.y;
            q0.p2.x = x - dx;
            q0.p2.y = y - dy;
            q0.p3.x = x;
            q0.p3.y = y;
         }

         if (q1 != null) {
            q1.p1.x = x;
            q1.p1.y = y;
            q1.p2.x = x + dx;
            q1.p2.y = y + dy;
            q1.p3.x = this.p3.x;
            q1.p3.y = this.p3.y;
         }

      }
   }

   public void subdivide(double t, Quadradic q0, Quadradic q1) {
      Point2D.Double np = this.eval(t);
      Point2D.Double npd = this.evalDt(t);
      if (q0 != null) {
         q0.p1.x = this.p1.x;
         q0.p1.y = this.p1.y;
         q0.p2.x = np.x - npd.x * t * 0.5;
         q0.p2.y = np.y - npd.y * t * 0.5;
         q0.p3.x = np.x;
         q0.p3.y = np.y;
      }

      if (q1 != null) {
         q1.p1.x = np.x;
         q1.p1.y = np.y;
         q1.p2.x = np.x + npd.x * (1.0 - t) * 0.5;
         q1.p2.y = np.y + npd.y * (1.0 - t) * 0.5;
         q1.p3.x = this.p3.x;
         q1.p3.y = this.p3.y;
      }

   }

   public void subdivide(Segment s0, Segment s1) {
      Quadradic q0 = null;
      Quadradic q1 = null;
      if (s0 instanceof Quadradic) {
         q0 = (Quadradic)s0;
      }

      if (s1 instanceof Quadradic) {
         q1 = (Quadradic)s1;
      }

      this.subdivide(q0, q1);
   }

   public void subdivide(double t, Segment s0, Segment s1) {
      Quadradic q0 = null;
      Quadradic q1 = null;
      if (s0 instanceof Quadradic) {
         q0 = (Quadradic)s0;
      }

      if (s1 instanceof Quadradic) {
         q1 = (Quadradic)s1;
      }

      this.subdivide(t, q0, q1);
   }

   protected double subLength(double leftLegLen, double rightLegLen, double maxErr) {
      ++count;
      double dx = this.p3.x - this.p1.x;
      double dy = this.p3.y - this.p1.y;
      double cordLen = Math.sqrt(dx * dx + dy * dy);
      double hullLen = leftLegLen + rightLegLen;
      if (hullLen < maxErr) {
         return (hullLen + cordLen) * 0.5;
      } else {
         double err = hullLen - cordLen;
         if (err < maxErr) {
            return (hullLen + cordLen) * 0.5;
         } else {
            Quadradic q = new Quadradic();
            double x = (this.p1.x + 2.0 * this.p2.x + this.p3.x) * 0.25;
            double y = (this.p1.y + 2.0 * this.p2.y + this.p3.y) * 0.25;
            dx = 0.25 * dx;
            dy = 0.25 * dy;
            q.p1.x = this.p1.x;
            q.p1.y = this.p1.y;
            q.p2.x = x - dx;
            q.p2.y = y - dy;
            q.p3.x = x;
            q.p3.y = y;
            double midLen = 0.25 * cordLen;
            double len = q.subLength(leftLegLen * 0.5, midLen, maxErr * 0.5);
            q.p1.x = x;
            q.p1.y = y;
            q.p2.x = x + dx;
            q.p2.y = y + dy;
            q.p3.x = this.p3.x;
            q.p3.y = this.p3.y;
            len += q.subLength(midLen, rightLegLen * 0.5, maxErr * 0.5);
            return len;
         }
      }
   }

   public double getLength() {
      return this.getLength(1.0E-6);
   }

   public double getLength(double maxErr) {
      double dx = this.p2.x - this.p1.x;
      double dy = this.p2.y - this.p1.y;
      double leftLegLen = Math.sqrt(dx * dx + dy * dy);
      dx = this.p3.x - this.p2.x;
      dy = this.p3.y - this.p2.y;
      double rightLegLen = Math.sqrt(dx * dx + dy * dy);
      double eps = maxErr * (leftLegLen + rightLegLen);
      return this.subLength(leftLegLen, rightLegLen, eps);
   }

   public String toString() {
      return "M" + this.p1.x + ',' + this.p1.y + 'Q' + this.p2.x + ',' + this.p2.y + ' ' + this.p3.x + ',' + this.p3.y;
   }
}
