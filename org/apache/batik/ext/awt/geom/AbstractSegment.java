package org.apache.batik.ext.awt.geom;

import java.awt.geom.Point2D;
import java.util.Arrays;

public abstract class AbstractSegment implements Segment {
   static final double eps = 3.552713678800501E-15;
   static final double tol = 1.4210854715202004E-14;

   protected abstract int findRoots(double var1, double[] var3);

   public Segment.SplitResults split(double y) {
      double[] roots = new double[]{0.0, 0.0, 0.0};
      int numSol = this.findRoots(y, roots);
      if (numSol == 0) {
         return null;
      } else {
         Arrays.sort(roots, 0, numSol);
         double[] segs = new double[numSol + 2];
         int numSegments = 0;
         segs[numSegments++] = 0.0;

         double pT;
         for(int i = 0; i < numSol; ++i) {
            pT = roots[i];
            if (!(pT <= 0.0)) {
               if (pT >= 1.0) {
                  break;
               }

               if (segs[numSegments - 1] != pT) {
                  segs[numSegments++] = pT;
               }
            }
         }

         segs[numSegments++] = 1.0;
         if (numSegments == 2) {
            return null;
         } else {
            Segment[] parts = new Segment[numSegments];
            pT = 0.0;
            int pIdx = 0;
            boolean firstAbove = false;
            boolean prevAbove = false;

            for(int i = 1; i < numSegments; ++i) {
               parts[pIdx] = this.getSegment(segs[i - 1], segs[i]);
               Point2D.Double pt = parts[pIdx].eval(0.5);
               if (pIdx == 0) {
                  ++pIdx;
                  firstAbove = prevAbove = pt.y < y;
               } else {
                  boolean above = pt.y < y;
                  if (prevAbove == above) {
                     parts[pIdx - 1] = this.getSegment(pT, segs[i]);
                  } else {
                     ++pIdx;
                     pT = segs[i - 1];
                     prevAbove = above;
                  }
               }
            }

            if (pIdx == 1) {
               return null;
            } else {
               Segment[] below;
               Segment[] above;
               if (firstAbove) {
                  above = new Segment[(pIdx + 1) / 2];
                  below = new Segment[pIdx / 2];
               } else {
                  above = new Segment[pIdx / 2];
                  below = new Segment[(pIdx + 1) / 2];
               }

               int ai = 0;
               int bi = 0;

               for(int i = 0; i < pIdx; ++i) {
                  if (firstAbove) {
                     above[ai++] = parts[i];
                  } else {
                     below[bi++] = parts[i];
                  }

                  firstAbove = !firstAbove;
               }

               return new Segment.SplitResults(below, above);
            }
         }
      }
   }

   public Segment splitBefore(double t) {
      return this.getSegment(0.0, t);
   }

   public Segment splitAfter(double t) {
      return this.getSegment(t, 1.0);
   }

   public static int solveLine(double a, double b, double[] roots) {
      if (a == 0.0) {
         if (b != 0.0) {
            return 0;
         } else {
            roots[0] = 0.0;
            return 1;
         }
      } else {
         roots[0] = -b / a;
         return 1;
      }
   }

   public static int solveQuad(double a, double b, double c, double[] roots) {
      if (a == 0.0) {
         return solveLine(b, c, roots);
      } else {
         double det = b * b - 4.0 * a * c;
         if (Math.abs(det) <= 1.4210854715202004E-14 * b * b) {
            roots[0] = -b / (2.0 * a);
            return 1;
         } else if (det < 0.0) {
            return 0;
         } else {
            det = Math.sqrt(det);
            double w = -(b + matchSign(det, b));
            roots[0] = 2.0 * c / w;
            roots[1] = w / (2.0 * a);
            return 2;
         }
      }
   }

   public static double matchSign(double a, double b) {
      if (b < 0.0) {
         return a < 0.0 ? a : -a;
      } else {
         return a > 0.0 ? a : -a;
      }
   }

   public static int solveCubic(double a3, double a2, double a1, double a0, double[] roots) {
      double[] dRoots = new double[]{0.0, 0.0};
      int dCnt = solveQuad(3.0 * a3, 2.0 * a2, a1, dRoots);
      double[] yVals = new double[]{0.0, 0.0, 0.0, 0.0};
      double[] tVals = new double[]{0.0, 0.0, 0.0, 0.0};
      int yCnt = 0;
      yVals[yCnt] = a0;
      tVals[yCnt++] = 0.0;
      double r;
      switch (dCnt) {
         case 1:
            r = dRoots[0];
            if (r > 0.0 && r < 1.0) {
               yVals[yCnt] = ((a3 * r + a2) * r + a1) * r + a0;
               tVals[yCnt++] = r;
            }
            break;
         case 2:
            if (dRoots[0] > dRoots[1]) {
               double t = dRoots[0];
               dRoots[0] = dRoots[1];
               dRoots[1] = t;
            }

            r = dRoots[0];
            if (r > 0.0 && r < 1.0) {
               yVals[yCnt] = ((a3 * r + a2) * r + a1) * r + a0;
               tVals[yCnt++] = r;
            }

            r = dRoots[1];
            if (r > 0.0 && r < 1.0) {
               yVals[yCnt] = ((a3 * r + a2) * r + a1) * r + a0;
               tVals[yCnt++] = r;
            }
      }

      yVals[yCnt] = a3 + a2 + a1 + a0;
      tVals[yCnt++] = 1.0;
      int ret = 0;

      for(int i = 0; i < yCnt - 1; ++i) {
         double y0 = yVals[i];
         double t0 = tVals[i];
         double y1 = yVals[i + 1];
         double t1 = tVals[i + 1];
         if ((!(y0 < 0.0) || !(y1 < 0.0)) && (!(y0 > 0.0) || !(y1 > 0.0))) {
            double epsZero;
            if (y0 > y1) {
               epsZero = y0;
               y0 = y1;
               y1 = epsZero;
               epsZero = t0;
               t0 = t1;
               t1 = epsZero;
            }

            if (-y0 < 1.4210854715202004E-14 * y1) {
               roots[ret++] = t0;
            } else if (y1 < -1.4210854715202004E-14 * y0) {
               roots[ret++] = t1;
               ++i;
            } else {
               epsZero = 1.4210854715202004E-14 * (y1 - y0);

               int cnt;
               for(cnt = 0; cnt < 20; ++cnt) {
                  double dt = t1 - t0;
                  double dy = y1 - y0;
                  double t = t0 + (Math.abs(y0 / dy) * 99.0 + 0.5) * dt / 100.0;
                  double v = ((a3 * t + a2) * t + a1) * t + a0;
                  if (Math.abs(v) < epsZero) {
                     roots[ret++] = t;
                     break;
                  }

                  if (v < 0.0) {
                     t0 = t;
                     y0 = v;
                  } else {
                     t1 = t;
                     y1 = v;
                  }
               }

               if (cnt == 20) {
                  roots[ret++] = (t0 + t1) / 2.0;
               }
            }
         }
      }

      return ret;
   }
}
