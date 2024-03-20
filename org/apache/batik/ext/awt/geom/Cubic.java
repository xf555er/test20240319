package org.apache.batik.ext.awt.geom;

import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

public class Cubic extends AbstractSegment {
   public Point2D.Double p1;
   public Point2D.Double p2;
   public Point2D.Double p3;
   public Point2D.Double p4;
   private static int count = 0;

   public Cubic() {
      this.p1 = new Point2D.Double();
      this.p2 = new Point2D.Double();
      this.p3 = new Point2D.Double();
      this.p4 = new Point2D.Double();
   }

   public Cubic(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
      this.p1 = new Point2D.Double(x1, y1);
      this.p2 = new Point2D.Double(x2, y2);
      this.p3 = new Point2D.Double(x3, y3);
      this.p4 = new Point2D.Double(x4, y4);
   }

   public Cubic(Point2D.Double p1, Point2D.Double p2, Point2D.Double p3, Point2D.Double p4) {
      this.p1 = p1;
      this.p2 = p2;
      this.p3 = p3;
      this.p4 = p4;
   }

   public Object clone() {
      return new Cubic(new Point2D.Double(this.p1.x, this.p1.y), new Point2D.Double(this.p2.x, this.p2.y), new Point2D.Double(this.p3.x, this.p3.y), new Point2D.Double(this.p4.x, this.p4.y));
   }

   public Segment reverse() {
      return new Cubic(new Point2D.Double(this.p4.x, this.p4.y), new Point2D.Double(this.p3.x, this.p3.y), new Point2D.Double(this.p2.x, this.p2.y), new Point2D.Double(this.p1.x, this.p1.y));
   }

   private void getMinMax(double p1, double p2, double p3, double p4, double[] minMax) {
      if (p4 > p1) {
         minMax[0] = p1;
         minMax[1] = p4;
      } else {
         minMax[0] = p4;
         minMax[1] = p1;
      }

      double c0 = 3.0 * (p2 - p1);
      double c1 = 6.0 * (p3 - p2);
      double c2 = 3.0 * (p4 - p3);
      double[] eqn = new double[]{c0, c1 - 2.0 * c0, c2 - c1 + c0};
      int roots = QuadCurve2D.solveQuadratic(eqn);

      for(int r = 0; r < roots; ++r) {
         double tv = eqn[r];
         if (!(tv <= 0.0) && !(tv >= 1.0)) {
            tv = (1.0 - tv) * (1.0 - tv) * (1.0 - tv) * p1 + 3.0 * tv * (1.0 - tv) * (1.0 - tv) * p2 + 3.0 * tv * tv * (1.0 - tv) * p3 + tv * tv * tv * p4;
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
      this.getMinMax(this.p1.x, this.p2.x, this.p3.x, this.p4.x, minMax);
      return minMax[0];
   }

   public double maxX() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.x, this.p2.x, this.p3.x, this.p4.x, minMax);
      return minMax[1];
   }

   public double minY() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.y, this.p2.y, this.p3.y, this.p4.y, minMax);
      return minMax[0];
   }

   public double maxY() {
      double[] minMax = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.y, this.p2.y, this.p3.y, this.p4.y, minMax);
      return minMax[1];
   }

   public Rectangle2D getBounds2D() {
      double[] minMaxX = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.x, this.p2.x, this.p3.x, this.p4.x, minMaxX);
      double[] minMaxY = new double[]{0.0, 0.0};
      this.getMinMax(this.p1.y, this.p2.y, this.p3.y, this.p4.y, minMaxY);
      return new Rectangle2D.Double(minMaxX[0], minMaxY[0], minMaxX[1] - minMaxX[0], minMaxY[1] - minMaxY[0]);
   }

   protected int findRoots(double y, double[] roots) {
      double[] eqn = new double[]{this.p1.y - y, 3.0 * (this.p2.y - this.p1.y), 3.0 * (this.p1.y - 2.0 * this.p2.y + this.p3.y), 3.0 * this.p2.y - this.p1.y + this.p4.y - 3.0 * this.p3.y};
      return CubicCurve2D.solveCubic(eqn, roots);
   }

   public Point2D.Double evalDt(double t) {
      double x = 3.0 * ((this.p2.x - this.p1.x) * (1.0 - t) * (1.0 - t) + 2.0 * (this.p3.x - this.p2.x) * (1.0 - t) * t + (this.p4.x - this.p3.x) * t * t);
      double y = 3.0 * ((this.p2.y - this.p1.y) * (1.0 - t) * (1.0 - t) + 2.0 * (this.p3.y - this.p2.y) * (1.0 - t) * t + (this.p4.y - this.p3.y) * t * t);
      return new Point2D.Double(x, y);
   }

   public Point2D.Double eval(double t) {
      double x = (1.0 - t) * (1.0 - t) * (1.0 - t) * this.p1.x + 3.0 * (t * (1.0 - t) * (1.0 - t) * this.p2.x + t * t * (1.0 - t) * this.p3.x) + t * t * t * this.p4.x;
      double y = (1.0 - t) * (1.0 - t) * (1.0 - t) * this.p1.y + 3.0 * (t * (1.0 - t) * (1.0 - t) * this.p2.y + t * t * (1.0 - t) * this.p3.y) + t * t * t * this.p4.y;
      return new Point2D.Double(x, y);
   }

   public void subdivide(Segment s0, Segment s1) {
      Cubic c0 = null;
      Cubic c1 = null;
      if (s0 instanceof Cubic) {
         c0 = (Cubic)s0;
      }

      if (s1 instanceof Cubic) {
         c1 = (Cubic)s1;
      }

      this.subdivide(c0, c1);
   }

   public void subdivide(double t, Segment s0, Segment s1) {
      Cubic c0 = null;
      Cubic c1 = null;
      if (s0 instanceof Cubic) {
         c0 = (Cubic)s0;
      }

      if (s1 instanceof Cubic) {
         c1 = (Cubic)s1;
      }

      this.subdivide(t, c0, c1);
   }

   public void subdivide(Cubic c0, Cubic c1) {
      if (c0 != null || c1 != null) {
         double npX = (this.p1.x + 3.0 * (this.p2.x + this.p3.x) + this.p4.x) * 0.125;
         double npY = (this.p1.y + 3.0 * (this.p2.y + this.p3.y) + this.p4.y) * 0.125;
         double npdx = (this.p2.x - this.p1.x + 2.0 * (this.p3.x - this.p2.x) + (this.p4.x - this.p3.x)) * 0.125;
         double npdy = (this.p2.y - this.p1.y + 2.0 * (this.p3.y - this.p2.y) + (this.p4.y - this.p3.y)) * 0.125;
         if (c0 != null) {
            c0.p1.x = this.p1.x;
            c0.p1.y = this.p1.y;
            c0.p2.x = (this.p2.x + this.p1.x) * 0.5;
            c0.p2.y = (this.p2.y + this.p1.y) * 0.5;
            c0.p3.x = npX - npdx;
            c0.p3.y = npY - npdy;
            c0.p4.x = npX;
            c0.p4.y = npY;
         }

         if (c1 != null) {
            c1.p1.x = npX;
            c1.p1.y = npY;
            c1.p2.x = npX + npdx;
            c1.p2.y = npY + npdy;
            c1.p3.x = (this.p4.x + this.p3.x) * 0.5;
            c1.p3.y = (this.p4.y + this.p3.y) * 0.5;
            c1.p4.x = this.p4.x;
            c1.p4.y = this.p4.y;
         }

      }
   }

   public void subdivide(double t, Cubic c0, Cubic c1) {
      if (c0 != null || c1 != null) {
         Point2D.Double np = this.eval(t);
         Point2D.Double npd = this.evalDt(t);
         if (c0 != null) {
            c0.p1.x = this.p1.x;
            c0.p1.y = this.p1.y;
            c0.p2.x = this.p1.x + (this.p2.x - this.p1.x) * t;
            c0.p2.y = this.p1.y + (this.p2.y - this.p1.y) * t;
            c0.p3.x = np.x - npd.x * t / 3.0;
            c0.p3.y = np.y - npd.y * t / 3.0;
            c0.p4.x = np.x;
            c0.p4.y = np.y;
         }

         if (c1 != null) {
            c1.p1.x = np.x;
            c1.p1.y = np.y;
            c1.p2.x = np.x + npd.x * (1.0 - t) / 3.0;
            c1.p2.y = np.y + npd.y * (1.0 - t) / 3.0;
            c1.p3.x = this.p4.x + (this.p3.x - this.p4.x) * (1.0 - t);
            c1.p3.y = this.p4.y + (this.p3.y - this.p4.y) * (1.0 - t);
            c1.p4.x = this.p4.x;
            c1.p4.y = this.p4.y;
         }

      }
   }

   public Segment getSegment(double t0, double t1) {
      double dt = t1 - t0;
      Point2D.Double np1 = this.eval(t0);
      Point2D.Double dp1 = this.evalDt(t0);
      Point2D.Double np2 = new Point2D.Double(np1.x + dt * dp1.x / 3.0, np1.y + dt * dp1.y / 3.0);
      Point2D.Double np4 = this.eval(t1);
      Point2D.Double dp4 = this.evalDt(t1);
      Point2D.Double np3 = new Point2D.Double(np4.x - dt * dp4.x / 3.0, np4.y - dt * dp4.y / 3.0);
      return new Cubic(np1, np2, np3, np4);
   }

   protected double subLength(double leftLegLen, double rightLegLen, double maxErr) {
      ++count;
      double cldx = this.p3.x - this.p2.x;
      double cldy = this.p3.y - this.p2.y;
      double crossLegLen = Math.sqrt(cldx * cldx + cldy * cldy);
      double cdx = this.p4.x - this.p1.x;
      double cdy = this.p4.y - this.p1.y;
      double cordLen = Math.sqrt(cdx * cdx + cdy * cdy);
      double hullLen = leftLegLen + rightLegLen + crossLegLen;
      if (hullLen < maxErr) {
         return (hullLen + cordLen) / 2.0;
      } else {
         double err = hullLen - cordLen;
         if (err < maxErr) {
            return (hullLen + cordLen) / 2.0;
         } else {
            Cubic c = new Cubic();
            double npX = (this.p1.x + 3.0 * (this.p2.x + this.p3.x) + this.p4.x) * 0.125;
            double npY = (this.p1.y + 3.0 * (this.p2.y + this.p3.y) + this.p4.y) * 0.125;
            double npdx = (cldx + cdx) * 0.125;
            double npdy = (cldy + cdy) * 0.125;
            c.p1.x = this.p1.x;
            c.p1.y = this.p1.y;
            c.p2.x = (this.p2.x + this.p1.x) * 0.5;
            c.p2.y = (this.p2.y + this.p1.y) * 0.5;
            c.p3.x = npX - npdx;
            c.p3.y = npY - npdy;
            c.p4.x = npX;
            c.p4.y = npY;
            double midLen = Math.sqrt(npdx * npdx + npdy * npdy);
            double len = c.subLength(leftLegLen / 2.0, midLen, maxErr / 2.0);
            c.p1.x = npX;
            c.p1.y = npY;
            c.p2.x = npX + npdx;
            c.p2.y = npY + npdy;
            c.p3.x = (this.p4.x + this.p3.x) * 0.5;
            c.p3.y = (this.p4.y + this.p3.y) * 0.5;
            c.p4.x = this.p4.x;
            c.p4.y = this.p4.y;
            len += c.subLength(midLen, rightLegLen / 2.0, maxErr / 2.0);
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
      dx = this.p4.x - this.p3.x;
      dy = this.p4.y - this.p3.y;
      double rightLegLen = Math.sqrt(dx * dx + dy * dy);
      dx = this.p3.x - this.p2.x;
      dy = this.p3.y - this.p2.y;
      double crossLegLen = Math.sqrt(dx * dx + dy * dy);
      double eps = maxErr * (leftLegLen + rightLegLen + crossLegLen);
      return this.subLength(leftLegLen, rightLegLen, eps);
   }

   public String toString() {
      return "M" + this.p1.x + ',' + this.p1.y + 'C' + this.p2.x + ',' + this.p2.y + ' ' + this.p3.x + ',' + this.p3.y + ' ' + this.p4.x + ',' + this.p4.y;
   }
}
