package org.apache.batik.ext.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

public class ExtendedGeneralPath implements ExtendedShape, Cloneable {
   protected GeneralPath path;
   int numVals;
   int numSeg;
   float[] values;
   int[] types;
   float mx;
   float my;
   float cx;
   float cy;

   public ExtendedGeneralPath() {
      this.numVals = 0;
      this.numSeg = 0;
      this.values = null;
      this.types = null;
      this.path = new GeneralPath();
   }

   public ExtendedGeneralPath(int rule) {
      this.numVals = 0;
      this.numSeg = 0;
      this.values = null;
      this.types = null;
      this.path = new GeneralPath(rule);
   }

   public ExtendedGeneralPath(int rule, int initialCapacity) {
      this.numVals = 0;
      this.numSeg = 0;
      this.values = null;
      this.types = null;
      this.path = new GeneralPath(rule, initialCapacity);
   }

   public ExtendedGeneralPath(Shape s) {
      this();
      this.append(s, false);
   }

   public synchronized void arcTo(float rx, float ry, float angle, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
      if (rx != 0.0F && ry != 0.0F) {
         this.checkMoveTo();
         double x0 = (double)this.cx;
         double y0 = (double)this.cy;
         if (x0 != (double)x || y0 != (double)y) {
            Arc2D arc = computeArc(x0, y0, (double)rx, (double)ry, (double)angle, largeArcFlag, sweepFlag, (double)x, (double)y);
            if (arc != null) {
               AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians((double)angle), arc.getCenterX(), arc.getCenterY());
               Shape s = t.createTransformedShape(arc);
               this.path.append(s, true);
               this.makeRoom(7);
               this.types[this.numSeg++] = 4321;
               this.values[this.numVals++] = rx;
               this.values[this.numVals++] = ry;
               this.values[this.numVals++] = angle;
               this.values[this.numVals++] = largeArcFlag ? 1.0F : 0.0F;
               this.values[this.numVals++] = sweepFlag ? 1.0F : 0.0F;
               this.cx = this.values[this.numVals++] = x;
               this.cy = this.values[this.numVals++] = y;
            }
         }
      } else {
         this.lineTo(x, y);
      }
   }

   public static Arc2D computeArc(double x0, double y0, double rx, double ry, double angle, boolean largeArcFlag, boolean sweepFlag, double x, double y) {
      double dx2 = (x0 - x) / 2.0;
      double dy2 = (y0 - y) / 2.0;
      angle = Math.toRadians(angle % 360.0);
      double cosAngle = Math.cos(angle);
      double sinAngle = Math.sin(angle);
      double x1 = cosAngle * dx2 + sinAngle * dy2;
      double y1 = -sinAngle * dx2 + cosAngle * dy2;
      rx = Math.abs(rx);
      ry = Math.abs(ry);
      double Prx = rx * rx;
      double Pry = ry * ry;
      double Px1 = x1 * x1;
      double Py1 = y1 * y1;
      double radiiCheck = Px1 / Prx + Py1 / Pry;
      double sign;
      if (radiiCheck > 0.99999) {
         sign = Math.sqrt(radiiCheck) * 1.00001;
         rx = sign * rx;
         ry = sign * ry;
         Prx = rx * rx;
         Pry = ry * ry;
      }

      sign = largeArcFlag == sweepFlag ? -1.0 : 1.0;
      double sq = (Prx * Pry - Prx * Py1 - Pry * Px1) / (Prx * Py1 + Pry * Px1);
      sq = sq < 0.0 ? 0.0 : sq;
      double coef = sign * Math.sqrt(sq);
      double cx1 = coef * (rx * y1 / ry);
      double cy1 = coef * -(ry * x1 / rx);
      double sx2 = (x0 + x) / 2.0;
      double sy2 = (y0 + y) / 2.0;
      double cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
      double cy = sy2 + sinAngle * cx1 + cosAngle * cy1;
      double ux = (x1 - cx1) / rx;
      double uy = (y1 - cy1) / ry;
      double vx = (-x1 - cx1) / rx;
      double vy = (-y1 - cy1) / ry;
      double n = Math.sqrt(ux * ux + uy * uy);
      sign = uy < 0.0 ? -1.0 : 1.0;
      double angleStart = Math.toDegrees(sign * Math.acos(ux / n));
      n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
      double p = ux * vx + uy * vy;
      sign = ux * vy - uy * vx < 0.0 ? -1.0 : 1.0;
      double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
      if (!sweepFlag && angleExtent > 0.0) {
         angleExtent -= 360.0;
      } else if (sweepFlag && angleExtent < 0.0) {
         angleExtent += 360.0;
      }

      angleExtent %= 360.0;
      angleStart %= 360.0;
      Arc2D.Double arc = new Arc2D.Double();
      arc.x = cx - rx;
      arc.y = cy - ry;
      arc.width = rx * 2.0;
      arc.height = ry * 2.0;
      arc.start = -angleStart;
      arc.extent = -angleExtent;
      return arc;
   }

   public synchronized void moveTo(float x, float y) {
      this.makeRoom(2);
      this.types[this.numSeg++] = 0;
      this.cx = this.mx = this.values[this.numVals++] = x;
      this.cy = this.my = this.values[this.numVals++] = y;
   }

   public synchronized void lineTo(float x, float y) {
      this.checkMoveTo();
      this.path.lineTo(x, y);
      this.makeRoom(2);
      this.types[this.numSeg++] = 1;
      this.cx = this.values[this.numVals++] = x;
      this.cy = this.values[this.numVals++] = y;
   }

   public synchronized void quadTo(float x1, float y1, float x2, float y2) {
      this.checkMoveTo();
      this.path.quadTo(x1, y1, x2, y2);
      this.makeRoom(4);
      this.types[this.numSeg++] = 2;
      this.values[this.numVals++] = x1;
      this.values[this.numVals++] = y1;
      this.cx = this.values[this.numVals++] = x2;
      this.cy = this.values[this.numVals++] = y2;
   }

   public synchronized void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
      this.checkMoveTo();
      this.path.curveTo(x1, y1, x2, y2, x3, y3);
      this.makeRoom(6);
      this.types[this.numSeg++] = 3;
      this.values[this.numVals++] = x1;
      this.values[this.numVals++] = y1;
      this.values[this.numVals++] = x2;
      this.values[this.numVals++] = y2;
      this.cx = this.values[this.numVals++] = x3;
      this.cy = this.values[this.numVals++] = y3;
   }

   public synchronized void closePath() {
      if (this.numSeg == 0 || this.types[this.numSeg - 1] != 4) {
         if (this.numSeg != 0 && this.types[this.numSeg - 1] != 0) {
            this.path.closePath();
         }

         this.makeRoom(0);
         this.types[this.numSeg++] = 4;
         this.cx = this.mx;
         this.cy = this.my;
      }
   }

   protected void checkMoveTo() {
      if (this.numSeg != 0) {
         switch (this.types[this.numSeg - 1]) {
            case 0:
               this.path.moveTo(this.values[this.numVals - 2], this.values[this.numVals - 1]);
               break;
            case 4:
               if (this.numSeg == 1) {
                  return;
               }

               if (this.types[this.numSeg - 2] == 0) {
                  this.path.moveTo(this.values[this.numVals - 2], this.values[this.numVals - 1]);
               }
         }

      }
   }

   public void append(Shape s, boolean connect) {
      this.append(s.getPathIterator(new AffineTransform()), connect);
   }

   public void append(PathIterator pi, boolean connect) {
      double[] vals = new double[6];

      while(!pi.isDone()) {
         Arrays.fill(vals, 0.0);
         int type = pi.currentSegment(vals);
         pi.next();
         if (connect && this.numVals != 0) {
            if (type == 0) {
               double x = vals[0];
               double y = vals[1];
               if (x == (double)this.cx && y == (double)this.cy) {
                  if (pi.isDone()) {
                     break;
                  }

                  type = pi.currentSegment(vals);
                  pi.next();
               } else {
                  type = 1;
               }
            }

            connect = false;
         }

         switch (type) {
            case 0:
               this.moveTo((float)vals[0], (float)vals[1]);
               break;
            case 1:
               this.lineTo((float)vals[0], (float)vals[1]);
               break;
            case 2:
               this.quadTo((float)vals[0], (float)vals[1], (float)vals[2], (float)vals[3]);
               break;
            case 3:
               this.curveTo((float)vals[0], (float)vals[1], (float)vals[2], (float)vals[3], (float)vals[4], (float)vals[5]);
               break;
            case 4:
               this.closePath();
         }
      }

   }

   public void append(ExtendedPathIterator epi, boolean connect) {
      float[] vals = new float[7];

      while(!epi.isDone()) {
         Arrays.fill(vals, 0.0F);
         int type = epi.currentSegment(vals);
         epi.next();
         if (connect && this.numVals != 0) {
            if (type == 0) {
               float x = vals[0];
               float y = vals[1];
               if (x == this.cx && y == this.cy) {
                  if (epi.isDone()) {
                     break;
                  }

                  type = epi.currentSegment(vals);
                  epi.next();
               } else {
                  type = 1;
               }
            }

            connect = false;
         }

         switch (type) {
            case 0:
               this.moveTo(vals[0], vals[1]);
               break;
            case 1:
               this.lineTo(vals[0], vals[1]);
               break;
            case 2:
               this.quadTo(vals[0], vals[1], vals[2], vals[3]);
               break;
            case 3:
               this.curveTo(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5]);
               break;
            case 4:
               this.closePath();
               break;
            case 4321:
               this.arcTo(vals[0], vals[1], vals[2], vals[3] != 0.0F, vals[4] != 0.0F, vals[5], vals[6]);
         }
      }

   }

   public synchronized int getWindingRule() {
      return this.path.getWindingRule();
   }

   public void setWindingRule(int rule) {
      this.path.setWindingRule(rule);
   }

   public synchronized Point2D getCurrentPoint() {
      return this.numVals == 0 ? null : new Point2D.Double((double)this.cx, (double)this.cy);
   }

   public synchronized void reset() {
      this.path.reset();
      this.numSeg = 0;
      this.numVals = 0;
      this.values = null;
      this.types = null;
   }

   public void transform(AffineTransform at) {
      if (at.getType() != 0) {
         throw new IllegalArgumentException("ExtendedGeneralPaths can not be transformed");
      }
   }

   public synchronized Shape createTransformedShape(AffineTransform at) {
      return this.path.createTransformedShape(at);
   }

   public synchronized Rectangle getBounds() {
      return this.path.getBounds();
   }

   public synchronized Rectangle2D getBounds2D() {
      return this.path.getBounds2D();
   }

   public boolean contains(double x, double y) {
      return this.path.contains(x, y);
   }

   public boolean contains(Point2D p) {
      return this.path.contains(p);
   }

   public boolean contains(double x, double y, double w, double h) {
      return this.path.contains(x, y, w, h);
   }

   public boolean contains(Rectangle2D r) {
      return this.path.contains(r);
   }

   public boolean intersects(double x, double y, double w, double h) {
      return this.path.intersects(x, y, w, h);
   }

   public boolean intersects(Rectangle2D r) {
      return this.path.intersects(r);
   }

   public PathIterator getPathIterator(AffineTransform at) {
      return this.path.getPathIterator(at);
   }

   public PathIterator getPathIterator(AffineTransform at, double flatness) {
      return this.path.getPathIterator(at, flatness);
   }

   public ExtendedPathIterator getExtendedPathIterator() {
      return new EPI();
   }

   public Object clone() {
      try {
         ExtendedGeneralPath result = (ExtendedGeneralPath)super.clone();
         result.path = (GeneralPath)this.path.clone();
         if (this.values != null) {
            result.values = new float[this.values.length];
            System.arraycopy(this.values, 0, result.values, 0, this.values.length);
         }

         result.numVals = this.numVals;
         if (this.types != null) {
            result.types = new int[this.types.length];
            System.arraycopy(this.types, 0, result.types, 0, this.types.length);
         }

         result.numSeg = this.numSeg;
         return result;
      } catch (CloneNotSupportedException var2) {
         return null;
      }
   }

   private void makeRoom(int numValues) {
      if (this.values == null) {
         this.values = new float[2 * numValues];
         this.types = new int[2];
         this.numVals = 0;
         this.numSeg = 0;
      } else {
         int newSize = this.numVals + numValues;
         if (newSize > this.values.length) {
            int nlen = this.values.length * 2;
            if (nlen < newSize) {
               nlen = newSize;
            }

            float[] nvals = new float[nlen];
            System.arraycopy(this.values, 0, nvals, 0, this.numVals);
            this.values = nvals;
         }

         if (this.numSeg == this.types.length) {
            int[] ntypes = new int[this.types.length * 2];
            System.arraycopy(this.types, 0, ntypes, 0, this.types.length);
            this.types = ntypes;
         }

      }
   }

   class EPI implements ExtendedPathIterator {
      int segNum = 0;
      int valsIdx = 0;

      public int currentSegment() {
         return ExtendedGeneralPath.this.types[this.segNum];
      }

      public int currentSegment(double[] coords) {
         int ret = ExtendedGeneralPath.this.types[this.segNum];
         switch (ret) {
            case 0:
            case 1:
               coords[0] = (double)ExtendedGeneralPath.this.values[this.valsIdx];
               coords[1] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 1];
               break;
            case 2:
               coords[0] = (double)ExtendedGeneralPath.this.values[this.valsIdx];
               coords[1] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 1];
               coords[2] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 2];
               coords[3] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 3];
               break;
            case 3:
               coords[0] = (double)ExtendedGeneralPath.this.values[this.valsIdx];
               coords[1] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 1];
               coords[2] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 2];
               coords[3] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 3];
               coords[4] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 4];
               coords[5] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 5];
            case 4:
            default:
               break;
            case 4321:
               coords[0] = (double)ExtendedGeneralPath.this.values[this.valsIdx];
               coords[1] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 1];
               coords[2] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 2];
               coords[3] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 3];
               coords[4] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 4];
               coords[5] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 5];
               coords[6] = (double)ExtendedGeneralPath.this.values[this.valsIdx + 6];
         }

         return ret;
      }

      public int currentSegment(float[] coords) {
         int ret = ExtendedGeneralPath.this.types[this.segNum];
         switch (ret) {
            case 0:
            case 1:
               coords[0] = ExtendedGeneralPath.this.values[this.valsIdx];
               coords[1] = ExtendedGeneralPath.this.values[this.valsIdx + 1];
               break;
            case 2:
               System.arraycopy(ExtendedGeneralPath.this.values, this.valsIdx, coords, 0, 4);
               break;
            case 3:
               System.arraycopy(ExtendedGeneralPath.this.values, this.valsIdx, coords, 0, 6);
            case 4:
            default:
               break;
            case 4321:
               System.arraycopy(ExtendedGeneralPath.this.values, this.valsIdx, coords, 0, 7);
         }

         return ret;
      }

      public int getWindingRule() {
         return ExtendedGeneralPath.this.path.getWindingRule();
      }

      public boolean isDone() {
         return this.segNum == ExtendedGeneralPath.this.numSeg;
      }

      public void next() {
         int type = ExtendedGeneralPath.this.types[this.segNum++];
         switch (type) {
            case 0:
            case 1:
               this.valsIdx += 2;
               break;
            case 2:
               this.valsIdx += 4;
               break;
            case 3:
               this.valsIdx += 6;
            case 4:
            default:
               break;
            case 4321:
               this.valsIdx += 7;
         }

      }
   }
}
